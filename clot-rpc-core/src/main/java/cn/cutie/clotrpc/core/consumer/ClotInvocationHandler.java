package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.api.*;
import cn.cutie.clotrpc.core.consumer.http.OkHttpInvoker;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import cn.cutie.clotrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * 消费端动态代理
 */
@Slf4j
public class ClotInvocationHandler implements InvocationHandler {

    Class<?> service;

    RpcContext rpcContext;

    List<InstanceMata> providers;

    HttpInvoker httpInvoker;

//    public ClotInvocationHandler(Class<?> clazz){
//        this.service = clazz;
//    }

    public ClotInvocationHandler(Class<?> clazz, RpcContext rpcContext, List<InstanceMata> providers) {
        this.service = clazz;
        this.rpcContext = rpcContext;
        this.providers = providers;
        int timeout = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("app.timeout", "1000"));
        httpInvoker = new OkHttpInvoker(timeout);
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

                // 负载均衡
                List<InstanceMata> instances = rpcContext.getRouter().route(this.providers);
                InstanceMata instance = rpcContext.getLoadBalance().choose(instances);
                log.debug(" ===> loadBalance.choose(instances): " + instance);

                // rpcRequest 作为http请求
                RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());

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
