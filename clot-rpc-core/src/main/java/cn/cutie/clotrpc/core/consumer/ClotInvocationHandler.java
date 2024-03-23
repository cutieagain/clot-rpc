package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.api.RpcContext;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.consumer.http.OkHttpInvoker;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import cn.cutie.clotrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端动态代理
 */
@Slf4j
public class ClotInvocationHandler implements InvocationHandler {



    Class<?> service;

    RpcContext rpcContext;

    List<InstanceMata> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

//    public ClotInvocationHandler(Class<?> clazz){
//        this.service = clazz;
//    }

    public ClotInvocationHandler(Class<?> clazz, RpcContext rpcContext, List<InstanceMata> providers) {
        this.service = clazz;
        this.rpcContext = rpcContext;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        // 模拟调用provider，模拟http请求进行调用
        RpcRequest rpcRequest = new RpcRequest();
        // 这里没有怎么办，可以在构造函数中塞进来
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        // 负载均衡
        List<InstanceMata> instances = rpcContext.getRouter().route(this.providers);
        InstanceMata instance = rpcContext.getLoadBalance().choose(instances);
        log.debug(" ===> loadBalance.choose(instances): " + instance);

        // rpcRequest 作为http请求
        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
        if (rpcResponse.isStatus()){
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            Exception exception = rpcResponse.getEx();
//            exception.printStackTrace();
//            log.debug("===> " + exception);
            // 服务端的异常传递到客户端中
            throw new RuntimeException(exception);
        }
    }

}
