package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import cn.cutie.clotrpc.core.api.LoadBalance;
import cn.cutie.clotrpc.core.api.RegistryCenter;
import cn.cutie.clotrpc.core.api.Router;
import cn.cutie.clotrpc.core.api.RpcContext;
import cn.cutie.clotrpc.core.registry.ChangedListener;
import cn.cutie.clotrpc.core.registry.Event;
import cn.cutie.clotrpc.core.utils.MethodUtils;
import lombok.Data;
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
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;

    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    public void start(){
        Router router = applicationContext.getBean(Router.class);
        LoadBalance loadBalance = applicationContext.getBean(LoadBalance.class);
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(router);
        rpcContext.setLoadBalance(loadBalance);

//        String urls = environment.getProperty("clotrpc.providers");
//        if (urls.isEmpty()){
//            System.out.println("clotrpc providers is empty. ");
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
                System.out.println(" ===> " + f.getName());
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
        List<String> providers = mapUrls(registryCenter.fetchAll(serviceName));
        System.out.println(" ===> map to providers:");
        providers.forEach(System.out::println);

        // 获取订阅数据
        registryCenter.subscribe(serviceName, new ChangedListener() {
            @Override
            public void fire(Event event) {
                providers.clear();
                providers.addAll(mapUrls(event.getData()));
            }
        });
        return this.createConsumer(service, rpcContext, providers);
    }

    private List<String> mapUrls(List<String> nodes){
       return nodes.stream()
                .map(x -> "http://" + x.replace("_", ":")).collect(Collectors.toList());
    }

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<String> providers) {
        // 1、动态代理
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new ClotInvocationHandler(service, rpcContext, providers));
    }


}
