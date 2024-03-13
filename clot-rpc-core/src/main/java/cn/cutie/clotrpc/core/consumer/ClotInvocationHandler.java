package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class ClotInvocationHandler implements InvocationHandler {

    final static MediaType JSONTYPE = okhttp3.MediaType.get("application/json; charset=utf-8");

    Class<?> service;

    public ClotInvocationHandler(Class<?> clazz){
        this.service = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        // 模拟调用provider，模拟http请求进行调用
        RpcRequest rpcRequest = new RpcRequest();
        // 这里没有怎么办，可以在构造函数中塞进来
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethod(method.getName());
        rpcRequest.setArgs(args);

        // rpcRequest 作为http请求
        RpcResponse rpcResponse = this.post(rpcRequest);
        if (rpcResponse.isStatus()){
            // 数据类型是什么？
            // todo：课2作业 处理基本类型
            if(rpcResponse.getData() instanceof JSONObject){
                JSONObject jsonObject = (JSONObject) rpcResponse.getData();
                return jsonObject.toJavaObject(method.getReturnType());
            } else{
                return rpcResponse.getData();
            }
        } else {
            Exception exception = rpcResponse.getEx();
//            exception.printStackTrace();
//            System.out.println("===> " + exception);
            // 服务端的异常传递到客户端中
            throw new RuntimeException(exception);
        }
    }

    OkHttpClient client = new OkHttpClient.Builder()
//            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS)) todo:这里涉及kotlin的 internal fun的问题
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest) {
        // 1、OkHttpClient
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println(" ===> reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        String respJson = null;
        try {
            respJson = client.newCall(request).execute().body().string();
            System.out.println(" ===> respJson = " + respJson);
            RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 2、apache
        // 3、URLConnection
    }
}
