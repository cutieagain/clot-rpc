package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import cn.cutie.clotrpc.core.api.*;
import cn.cutie.clotrpc.core.meta.InstanceMeta;
import cn.cutie.clotrpc.core.meta.ServiceMeta;
import cn.cutie.clotrpc.core.registry.ChangedListener;
import cn.cutie.clotrpc.core.registry.Event;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * 消费端启动类
 */
@Slf4j
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    public void start(){
//        Router<InstanceMeta> router = applicationContext.getBean(Router.class);
//        LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
//        List<Filter> filters = applicationContext.getBeansOfType(Filter.class).values().stream().toList();
//        RpcContext rpcContext = new RpcContext();
//        rpcContext.setRouter(router);
//        rpcContext.setLoadBalancer(loadBalancer);
//        rpcContext.setFilters(filters);

        RpcContext rpcContext = applicationContext.getBean(RpcContext.class);

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
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .name(service.getCanonicalName())
                .app(rpcContext.param("app.id"))
                .namespace(rpcContext.param("app.namespace"))
                .env(rpcContext.param("app.env"))
                .build();

        List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
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

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
        // 1、动态代理
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new ClotInvocationHandler(service, rpcContext, providers));
    }


}
