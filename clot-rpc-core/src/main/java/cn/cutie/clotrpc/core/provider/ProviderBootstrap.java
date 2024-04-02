package cn.cutie.clotrpc.core.provider;

import cn.cutie.clotrpc.core.annotation.ClotProvider;
import cn.cutie.clotrpc.core.api.RegistryCenter;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.meta.ProviderMata;
import cn.cutie.clotrpc.core.meta.ServiceMeta;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import cn.cutie.clotrpc.core.utils.TypeUtils;
import com.sun.jdi.InvocationException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.LinkedMultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;

@Slf4j
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private LinkedMultiValueMap<String, ProviderMata> skeleton = new LinkedMultiValueMap<>();

    private String ip;
    @Value("${server.port}")
    private String port;
    private InstanceMata instance;

    RegistryCenter registryCenter;

    @Value("${app.id}")
    private String app;
    @Value("${app.namespace}")
    private String namespace;
    @Value("${app.env}")
    private String env;
    @Value("#{${app.metas}}") // # 表示spel
    private Map<String, String> metas;

    // 方法执行之前，把加了注解的服务提前加载好
    @PostConstruct // 相当于initMethod
    @SneakyThrows
    // PreDestroy  相当于destroyMethod，优雅停机的时候，注册中心服务需要在这里取消
    public void init(){
        // TODO: 2024/3/21 试下 findAnnotation
//        AnnotationUtils.findAnnotation(ClotProvider.class);
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(ClotProvider.class);
        providers.forEach((x, y) -> log.info(x));
        providers.values().forEach(this::genInterface);

        registryCenter = applicationContext.getBean(RegistryCenter.class);
    }

    /**
     * 延迟服务暴露
     */
    @SneakyThrows
    public void start(){
        // ip和端口构造对应实例
        ip = InetAddress.getLoopbackAddress().getHostAddress();
        instance = InstanceMata.http(ip, Integer.valueOf(port));
        // 启动的时候添加实例信息：{dc: 'bj', gray:'false', unit:'B001'}
        log.info(" ===> current instance params:{}", metas);
        instance.getParameters().putAll(metas);
        registryCenter.start();
        skeleton.keySet().forEach(this::registerService); // 这里zk有了，但是spring还未完成，服务实际是不可用的
    }

    @PreDestroy
    public void stop(){
        log.info(" ===> unreg all service.");
        skeleton.keySet().forEach(this::unRegisterService);
        registryCenter.stop();
    }

    private void unRegisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .name(service)
                .app(app)
                .namespace(namespace)
                .env(env)
                .build();
        registryCenter.unRegister(serviceMeta, instance);
    }

    /**
     * 注册到注册中心
     * @param service
     */
    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .name(service)
                .app(app)
                .namespace(namespace)
                .env(env)
                .build();
        registryCenter.register(serviceMeta, instance);
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
    private void genInterface(Object impl) {
        // 获取一个接口，有可能实现多个接口
        Arrays.stream(impl.getClass().getInterfaces()).forEach(
            service ->{
//                // 获取接口中所有的方法
//                Method[] methods = service.getMethods();
//                for (Method method : methods) {
//                    if (MethodUtils.checkLocalMethod(method)){
//                        continue;
//                    }
//                    // 如果是符合方法解析要求的，则创建provider
//                    createProvider(service, impl, method);
//                }
                Arrays.stream(service.getMethods())
                        .filter(method -> !MethodUtils.checkLocalMethod(method))
                        .forEach(method -> createProvider(service, impl, method));
            }
        );
    }

    private void createProvider(Class<?> vInterface, Object impl, Method method) {
        ProviderMata providerMata = ProviderMata.builder()
                .method(method)
                .serviceImpl(impl)
                .methodSign(MethodUtils.methodSign(method))
                .build();
        log.info("create a provider: " + providerMata);
        // 接口全限定名:providerMata
        skeleton.add(vInterface.getCanonicalName(), providerMata);
    }
}
