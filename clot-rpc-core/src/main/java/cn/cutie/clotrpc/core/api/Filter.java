package cn.cutie.clotrpc.core.api;

/**
 * 过滤器
 */
public interface Filter {
    RpcResponse preFilter(RpcRequest request);

    RpcResponse postFilter(RpcRequest request, RpcResponse response);


    // filter链，或者定义filter数组
//    Filter next();

    // lambda表达式只能表示有一个方法的接口
    Filter Default =  new Filter() {
        @Override
        public RpcResponse preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
            return response;
        }
    };
}
