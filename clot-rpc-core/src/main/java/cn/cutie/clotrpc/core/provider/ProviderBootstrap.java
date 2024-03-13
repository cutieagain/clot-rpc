package cn.cutie.clotrpc.core.provider;

import cn.cutie.clotrpc.core.annotation.ClotProvider;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import com.sun.jdi.InvocationException;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    public RpcResponse invokeRequest(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
        Object bean = skeleton.get(request.getService());
        try {
            // 通过类的getMethod方法获取方法进行反射
//            Method method = bean.getClass().getMethod(request.getMethod());
//            Method method = bean.getClass().getDeclaredMethod(request.getMethod());
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
//            rpcResponse.setEx(e);
            // 简化异常信息
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }

    // 方法执行之前，把加了注解的服务提前加载好
    @PostConstruct // 相当于initMethod
    // PreDestroy  相当于destroyMethod，优雅停机的时候，注册中心服务需要在这里取消
    public void buildProviders(){
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ClotProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
        providers.values().forEach(x -> genInterface(x));
    }

    // 全限定名为key放进skeleton，好一点的判断getInterfaces是否有多个接口
    private void genInterface(Object x) {
        Class<?> vInterface = x.getClass().getInterfaces()[0];
        skeleton.put(vInterface.getCanonicalName(), x);
    }
}
