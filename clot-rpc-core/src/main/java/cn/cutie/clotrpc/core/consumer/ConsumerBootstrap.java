package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import cn.cutie.clotrpc.core.api.*;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.meta.ServiceMeta;
import cn.cutie.clotrpc.core.registry.ChangedListener;
import cn.cutie.clotrpc.core.registry.Event;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消费端启动类
 */
@Slf4j
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    @Value("${app.id}")
    private String app;
    @Value("${app.namespace}")
    private String namespace;
    @Value("${app.env}")
    private String env;

    public void start(){
        Router<InstanceMata> router = applicationContext.getBean(Router.class);
        LoadBalance<InstanceMata> loadBalance = applicationContext.getBean(LoadBalance.class);
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
        List<Filter> filters = applicationContext.getBeansOfType(Filter.class).values().stream().toList();

        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(router);
        rpcContext.setLoadBalance(loadBalance);
        rpcContext.setFilters(filters);

//        String urls = environment.getProperty("clotrpc.providers");
//        if (urls.isEmpty()){
//            log.info("clotrpc providers is empty. ");
//        }
//        String[] providers = urls.split(",");

        // 创建UserService的代理类，让UserService有值
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            // 初始化成功了，可以获取bean了
            Object bean = applicationContext.getBean(beanName);

            // 获取有consumer注解的field
            List<Field> fields = MethodUtils.findAnnotatedFields(bean.getClass(), ClotConsumer.class);
            fields.stream().forEach( f->{
                log.info(" ===> " + f.getName());
                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null){
                        // todo：动态代理，、、、4种方式
                        consumer = this.createFromRegistry(service, rpcContext, registryCenter);
                        stub.putIfAbsent(serviceName, consumer);
                    }
                    f.setAccessible(true);
                    f.set(bean, consumer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
//                    throw new RuntimeException(e);
                }
            });
        }
    }

    private Object createFromRegistry(Class<?> service, RpcContext rpcContext, RegistryCenter registryCenter) {
        // 处理service和consumer关系的
        String serviceName = service.getCanonicalName();
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .name(serviceName)
                .app(app)
                .namespace(namespace)
                .env(env)
                .build();

        List<InstanceMata> providers = registryCenter.fetchAll(serviceMeta);
        log.info(" ===> map to providers:");
        providers.forEach(System.out::println);

        // 获取订阅数据
        registryCenter.subscribe(serviceMeta, new ChangedListener() {
            @Override
            public void fire(Event event) {
                providers.clear();
                providers.addAll(event.getData());
            }
        });
        return this.createConsumer(service, rpcContext, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<InstanceMata> providers) {
        // 1、动态代理
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new ClotInvocationHandler(service, rpcContext, providers));
    }


}
