package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ConsumerBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> stub = new HashMap<>();

    public void start(){
        // 创建UserService的代理类，让UserService有值
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            // 初始化成功了，可以获取bean了
            Object bean = applicationContext.getBean(beanName);

//            if (!beanName.contains("clotRpcDemoConsumerApplication")){ clotrpcdemoconsumer.
//            if (!beanName.contains("ClotRpcDemoConsumerApplication")){
//                return;
//            }

            // 获取有consumer注解的field
            List<Field> fields = findAnnotatedFields(bean.getClass());
            fields.stream().forEach( f->{
                System.out.println(" ===> " + f.getName());
                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null){
                        // todo：动态代理，、、、4种方式
                        consumer = this.createConsumer(service);
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

    private Object createConsumer(Class<?> service) {
        // 1、动态代理
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new ClotInvocationHandler(service));
    }

    // 获取这个类中的fields
    private List<Field> findAnnotatedFields(Class<?> aClass) {
        List<Field> result = new ArrayList<>();
        while(aClass != null){
            // 这里获取到的是spring增强后的（代理过的），所以getDeclaredFields获取不到对应的fields
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(ClotConsumer.class)){
                    result.add(field);
                }
            }
            // 这里是为了获取到真实那个类
            aClass = aClass.getSuperclass();
        }
        return result;
    }

}
