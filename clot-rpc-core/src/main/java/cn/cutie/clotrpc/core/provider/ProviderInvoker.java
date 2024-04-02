package cn.cutie.clotrpc.core.provider;

import cn.cutie.clotrpc.core.api.RpcException;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.meta.ProviderMata;
import cn.cutie.clotrpc.core.utils.TypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class ProviderInvoker {

    // 方法级别的映射关系
    private MultiValueMap<String, ProviderMata> skeleton;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        RpcResponse<Object> rpcResponse = new RpcResponse<>();
//        Object bean = skeleton.get(request.getService());
        List<ProviderMata> providerMatas = skeleton.get(request.getService());
        try {
            ProviderMata providerMata = findProviderMata(providerMatas, request.getMethodSign());

            // 通过类的getMethod方法获取方法进行反射
//            Method method = bean.getClass().getMethod(request.getMethod());
//            Method method = bean.getClass().getDeclaredMethod(request.getMethod());
//            Method method = findMethod(bean.getClass(), request.getMethodSign());
//            Object result = method.invoke(bean, request.getArgs());
            Method method = providerMata.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object result = method.invoke(providerMata.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
//            rpcResponse.setEx(e);
            // 简化异常信息
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RpcException(e.getMessage()));
        }
        return rpcResponse;
    }

    private ProviderMata findProviderMata(List<ProviderMata> providerMatas, String methodSign) {
        Optional<ProviderMata> optional = providerMatas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign))
                .findFirst();
        return optional.orElse(null);
    }

    // 参数类型转换为签名上的方法
    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            // 类型按照参数列表的类型转换一次
//            actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
            actuals[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actuals;
    }


}
