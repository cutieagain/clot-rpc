package cn.cutie.clotrpc.core.api;

/**
 * 过滤器
 */
public interface Filter {
    Object preFilter(RpcRequest request);

    Object postFilter(RpcRequest request, RpcResponse response, Object result);


    // filter链，或者定义filter数组
//    Filter next();

    // lambda表达式只能表示有一个方法的接口
    Filter Default =  new Filter() {
        @Override
        public Object preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }
    };
}
