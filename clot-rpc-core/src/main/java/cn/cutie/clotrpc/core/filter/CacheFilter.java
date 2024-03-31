package cn.cutie.clotrpc.core.filter;

import cn.cutie.clotrpc.core.api.Filter;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 如果之前调用过了，直接返回已经调用过的结果的过滤器
 * @Author: Cutie
 * @CreateDate: 2024/3/31 15:29
 * @Version: 0.0.1
 */
public class CacheFilter implements Filter {

    // TODO: 2024/3/31 map替换为guava里面的cache，加cache的容量，过期时间，淘汰策略
    static Map<String, RpcResponse> cache = new ConcurrentHashMap();

    @Override
    public RpcResponse preFilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
        cache.putIfAbsent(request.toString(), response);
        return response;
    }
}
