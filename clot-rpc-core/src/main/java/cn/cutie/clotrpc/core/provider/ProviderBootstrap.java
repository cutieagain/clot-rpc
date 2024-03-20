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

    private LinkedMultiValueMap<String, ProviderMata> skeleton = new LinkedMultiValueMap<>();

    private String ip;
    @Value("${server.port}")
    private String port;
    private String instance;

    RegistryCenter registryCenter;

    // 方法执行之前，把加了注解的服务提前加载好
    @PostConstruct // 相当于initMethod
    @SneakyThrows
    // PreDestroy  相当于destroyMethod，优雅停机的时候，注册中心服务需要在这里取消
    public void init(){
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ClotProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
        providers.values().forEach(x -> genInterface(x));

        registryCenter = applicationContext.getBean(RegistryCenter.class);
    }

    /**
     * 延迟服务暴露
     */
    @SneakyThrows
    public void start(){
        // ip和端口构造对应实例
        ip = InetAddress.getLoopbackAddress().getHostAddress();
        this.instance = ip + "_" + port;
        registryCenter.start();
        skeleton.keySet().forEach(this::registerService); // 这里zk有了，但是spring还未完成，服务实际是不可用的
    }

    @PreDestroy
    public void stop(){
        System.out.println(" ===> unreg all service.");
        skeleton.keySet().forEach(this::unRegisterService);
        registryCenter.stop();
    }

    private void unRegisterService(String service) {
        registryCenter.unRegister(service, instance);
    }

    /**
     * 注册到注册中心
     * @param service
     */
    private void registerService(String service) {
        registryCenter.register(service, instance);
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
