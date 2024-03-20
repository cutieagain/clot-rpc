package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;

public interface HttpInvoker {
     RpcResponse<?> post(RpcRequest rpcRequest, String url);
}
