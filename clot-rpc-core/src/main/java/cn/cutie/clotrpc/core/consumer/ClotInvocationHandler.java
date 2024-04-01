package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.api.*;
import cn.cutie.clotrpc.core.consumer.http.OkHttpInvoker;
import cn.cutie.clotrpc.core.governance.SlidingTimeWindow;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import cn.cutie.clotrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消费端动态代理
 */
@Slf4j
public class ClotInvocationHandler implements InvocationHandler {

    Class<?> service;

    RpcContext rpcContext;

    final List<InstanceMata> providers;

    // 隔离的provider
    List<InstanceMata> isolatedProviders = new ArrayList<>();
    // 半开的provider
    final List<InstanceMata> halfOpenProviders = new ArrayList<>();

    HttpInvoker httpInvoker;

    // 单位时间内出现了n次故障算失败，滑动时间窗口，e.g.30s内出现10次
    Map<String, SlidingTimeWindow> windows = new HashMap<>();

    ScheduledExecutorService executor;

//    public ClotInvocationHandler(Class<?> clazz){
//        this.service = clazz;
//    }

    public ClotInvocationHandler(Class<?> clazz, RpcContext rpcContext, List<InstanceMata> providers) {
        this.service = clazz;
        this.rpcContext = rpcContext;
        this.providers = providers;
        int timeout = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
        this.executor = Executors.newScheduledThreadPool(1);
        this.executor.scheduleWithFixedDelay(this::halfOpen, 10, 60, TimeUnit.SECONDS);

    }

    private void halfOpen() {
        log.debug(" ===> half open isolatedProviders: {}", isolatedProviders);
        // 需要清除，不然会重复
        this.halfOpenProviders.clear();
        this.halfOpenProviders.addAll(isolatedProviders);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        // 本地方法不进行代理
//        if (MethodUtils.checkLocalMethod(method.getName())){
//            return null;
//        }

        // 模拟调用provider，模拟http请求进行调用
        RpcRequest rpcRequest = new RpcRequest();
        // 这里没有怎么办，可以在构造函数中塞进来
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        // 重试次数
        int retries = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("app.retries", "1"));
        while (retries -- > 0){
            log.debug(" ===> consumer invoke retries: " + retries);
            try{
                for (Filter filter : this.rpcContext.getFilters()) {
                    // 过滤
                    // 可以抛出异常同一处理，或者返回一个空对象
                    RpcResponse<?> preResponse = filter.preFilter(rpcRequest);
                    if (preResponse != null) {
                        log.debug(filter.getClass().getName() + " ==> preFilter : " + preResponse);
                        return castReturnResult(method, preResponse);
                    }
                }

                InstanceMata instance;
                synchronized (halfOpenProviders){
                    // 如果半开为空，走原来的逻辑
                    if (halfOpenProviders.isEmpty()){
                        // 负载均衡
                        List<InstanceMata> instances = rpcContext.getRouter().route(this.providers);
                        instance = rpcContext.getLoadBalance().choose(instances);
                        log.debug(" ===> loadBalance.choose(instances): " + instance);
                    } else {
                        // 半开需要探活
                        instance = halfOpenProviders.remove(0);
                        log.debug(" ===> check alive instance: {}", instance);
                    }
                }

                RpcResponse<?> rpcResponse;
                String url = instance.toUrl();
                try{
                    // rpcRequest 作为http请求
                    rpcResponse = httpInvoker.post(rpcRequest, url);
                } catch (Exception e){
                    // 故障规则统计和隔离
                    // 每一次异常记录依次，统计30s内的异常数
                    SlidingTimeWindow window = windows.get(url);
                    if (window == null){
                        window = new SlidingTimeWindow();
                        windows.put(url, window);
                    }
                    window.record(System.currentTimeMillis());
                    log.debug(" ===> instance {} in window with {}", url, window.getSum());
                    // 发生了10次就进行故障隔离
                    if (window.getSum() >= 10){
                        isolate(instance);
                    }
                    throw e;
                }

                synchronized (providers){
                    // 如果这次是探活的请求，即正常的provider列表中不包含当前请求的provider实例
                    if (!providers.contains(instance)){
                        log.debug(" ===> instance {} is recovered, isolatedProviders: {}, providers: {}",
                                instance, isolatedProviders, providers);
                        isolatedProviders.remove(instance);
                        providers.add(instance);
                    }
                }

                // TODO: 2024/3/31 这里拿到的可能不是最终值，需要对cache进行排序，放最后执行
                for (Filter filter : this.rpcContext.getFilters()) {
                    // 每次处理都要使用上一次处理过的rpcResponse
                    rpcResponse = filter.postFilter(rpcRequest, rpcResponse);
                }

                return castReturnResult(method, rpcResponse);
            } catch (Exception ex){
                if (!(ex.getCause() instanceof SocketTimeoutException)){
                    throw ex;
                }
            }
        }
        return null;
    }

    /**
     * 隔离实例
     * @param instance
     */
    private void isolate(InstanceMata instance) {
        log.debug(" ===> isolate instance: {}", instance);
        // 从provider中移除
        providers.remove(instance);
        log.debug(" ===> providers: {}", providers);
        // 并添加进被隔离的provider列表，下次恢复使用
        isolatedProviders.add(instance);
        log.debug(" ===> isolatedProviders: {}", isolatedProviders);
    }

    @Nullable
    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()){
            return TypeUtils.castMethodResult(method, rpcResponse.getData());
        } else {
            Exception exception = rpcResponse.getEx();
            if (exception instanceof RpcException ex){
                throw ex;
            }
            // 换成自定义异常
            throw new RpcException(exception, RpcException.UnknownEx);
        }
    }

}
