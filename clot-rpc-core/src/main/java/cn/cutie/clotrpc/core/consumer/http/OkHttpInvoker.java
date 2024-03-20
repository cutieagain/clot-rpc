package cn.cutie.clotrpc.core.consumer.http;

import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSONTYPE = okhttp3.MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker() {
        client = new OkHttpClient.Builder()
//            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS)) // todo:这里涉及kotlin的 internal fun的问题
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        // 1、OkHttpClient
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println(" ===> reqJson = " + reqJson);
        Request request = new Request.Builder()
//                .url("http://localhost:8080/")
                .url(url)
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
