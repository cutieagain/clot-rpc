package cn.cutie.clotrpc.core.filter;

import cn.cutie.clotrpc.core.api.Filter;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import cn.cutie.clotrpc.core.utils.MockUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Description:
 * @Author: Cutie
 * @CreateDate: 2024/3/31 16:22
 * @Version: 0.0.1
 */
public class MockFilter implements Filter {
    @SneakyThrows
    @Override
    public RpcResponse preFilter(RpcRequest request) {
        // mock方法的返回值
        // 获取方法，获取方法的返回值，根据返回值mock返回的对象

        Class service = Class.forName(request.getService());
        Method method = findMethod(service, request.getMethodSign());
        Class clazz = method.getReturnType();
        return MockUtils.mockRpcResp(clazz);
    }

    private Method findMethod(Class service, String methodSign) {
        return Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.checkLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.methodSign(method)))
                .findFirst().orElse(null);
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
        return null;
    }
}
