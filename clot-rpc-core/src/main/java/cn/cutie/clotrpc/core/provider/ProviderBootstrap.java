package cn.cutie.clotrpc.core.provider;

import cn.cutie.clotrpc.core.annotation.ClotProvider;
import cn.cutie.clotrpc.core.api.RegistryCenter;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.meta.ProviderMata;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import cn.cutie.clotrpc.core.utils.TypeUtils;
import com.sun.jdi.InvocationException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;

@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    // 方法级别的映射关系
    private LinkedMultiValueMap<String, ProviderMata> skeleton = new LinkedMultiValueMap<>();

    private String ip;
    @Value("${server.port}")
    private String port;
    private String instance;

    // 方法执行之前，把加了注解的服务提前加载好
    @PostConstruct // 相当于initMethod
    @SneakyThrows
    // PreDestroy  相当于destroyMethod，优雅停机的时候，注册中心服务需要在这里取消
    public void init(){
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ClotProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
        providers.values().forEach(x -> genInterface(x));


    }

    /**
     * 延迟服务暴露
     */
    @SneakyThrows
    public void start(){
        // ip和端口构造对应实例
        ip = InetAddress.getLoopbackAddress().getHostAddress();
        this.instance = ip + "_" + port;

        skeleton.keySet().forEach(this::registerService); // 这里zk有了，但是spring还未完成，服务实际是不可用的
    }

    @PreDestroy
    public void stop(){
        skeleton.keySet().forEach(this::unRegisterService);
    }

    private void unRegisterService(String service) {
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
        registryCenter.unRegister(service, instance);
    }

    /**
     * 注册到注册中心
     * @param service
     */
    private void registerService(String service) {
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
        registryCenter.register(service, instance);
    }

    public RpcResponse invoke(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
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
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(providerMata.getServiceImpl(), args);
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

    // 参数类型转换为签名上的方法
    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            // 类型按照参数列表的类型转换一次
            actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return actuals;
    }

    private ProviderMata findProviderMata(List<ProviderMata> providerMatas, String methodSign) {
        Optional<ProviderMata> optional = providerMatas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign))
                .findFirst();
        return optional.orElse(null);
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }

    // 全限定名为key放进skeleton，好一点的判断getInterfaces是否有多个接口
    private void genInterface(Object x) {
        // 获取一个接口，有可能实现多个接口
        Arrays.stream(x.getClass().getInterfaces()).forEach(
            vInterface ->{
                // 获取接口中所有的方法
                Method[] methods = vInterface.getMethods();
                for (Method method : methods) {
                    if (MethodUtils.checkLocalMethod(method)){
                        continue;
                    }
                    // 如果是符合方法解析要求的，则创建provider
                    createProvider(vInterface, x, method);
                }
            }
        );
    }

    private void createProvider(Class<?> vInterface, Object x, Method method) {
        ProviderMata providerMata = new ProviderMata();
        providerMata.setMethod(method);
        providerMata.setServiceImpl(x);
        providerMata.setMethodSign(MethodUtils.methodSign(method));
        System.out.println("create a provider: " + providerMata);
        // 接口全限定名:providerMata
        skeleton.add(vInterface.getCanonicalName(), providerMata);
    }
}
