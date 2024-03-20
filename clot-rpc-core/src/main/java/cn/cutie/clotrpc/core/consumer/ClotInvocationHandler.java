package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.api.RpcContext;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.consumer.http.HttpInvoker;
import cn.cutie.clotrpc.core.consumer.http.OkHttpInvoker;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import cn.cutie.clotrpc.core.utils.TypeUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端动态代理
 */
public class ClotInvocationHandler implements InvocationHandler {



    Class<?> service;

    RpcContext rpcContext;

    List<String> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

//    public ClotInvocationHandler(Class<?> clazz){
//        this.service = clazz;
//    }

    public ClotInvocationHandler(Class<?> clazz, RpcContext rpcContext, List<String> providers) {
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
        List<String> urls = rpcContext.getRouter().route(this.providers);
        String url = (String) rpcContext.getLoadBalance().choose(urls);
        System.out.println(" ===> loadBalance.choose(urls): " + url);

        // rpcRequest 作为http请求
        RpcResponse rpcResponse = httpInvoker.post(rpcRequest, url);
        if (rpcResponse.isStatus()){
            Object data = rpcResponse.getData();
            return castMethodResult(method, data);
        } else {
            Exception exception = rpcResponse.getEx();
//            exception.printStackTrace();
//            System.out.println("===> " + exception);
            // 服务端的异常传递到客户端中
            throw new RuntimeException(exception);
        }
    }

    @Nullable
    private static Object castMethodResult(Method method, Object data) {
        // TODO: 2024/3/16 这里需要加类型转换，看一下？
        if(data instanceof JSONObject){
            JSONObject jsonObject = (JSONObject) data;
            return jsonObject.toJavaObject(method.getReturnType());
        } else if (data instanceof JSONArray jsonArray){
            Object[] array = jsonArray.toArray();
            // 数组中元素的类型
            Class<?> componentType = method.getReturnType().getComponentType(); // 元素类型
//                Class<?> componentType2 = method.getReturnType().arrayType();// 数组类型
            System.out.println(" ===> componentType:" + componentType);
            // 创建一个这个类型的数组
            Object resultArray = Array.newInstance(componentType, array.length);
            for (int i = 0; i < array.length; i++) {
                Array.set(resultArray, i, array[i]);
            }
            return resultArray;
        } else{
            // 处理基本类型
            return TypeUtils.cast(data, method.getReturnType());
        }
    }
}
