package cn.cutie.clotrpc.core.filter;

import cn.cutie.clotrpc.core.api.Filter;
import cn.cutie.clotrpc.core.api.RpcContext;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;

import java.util.Map;

/**
 * @Description:
 * @Author: Cutie
 * @CreateDate: 2024/4/5 18:46
 * @Version: 0.0.1
 */
public class ParameterFilter implements Filter {
    @Override
    public RpcResponse preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if(!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response, Object result) {
        // RpcContext.ContextParameters.get().clear();
        return null;
    }
}
