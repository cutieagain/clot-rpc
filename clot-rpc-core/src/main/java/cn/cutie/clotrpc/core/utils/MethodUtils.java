package cn.cutie.clotrpc.core.utils;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodUtils {

    public static boolean checkLocalMethod(final String method){
        // 本地方法不代理
        if("toString".equals(method) ||
            "hashCode".equals(method) ||
            "notifyAll".equals(method) ||
            "equals".equals(method) ||
            "wait".equals(method) ||
            "getClass".equals(method) ||
            "notify".equals(method) ) {
            return true;
        }
        return false;
    }

    public static boolean checkLocalMethod(Method method){
        // 判断方法是否是被Object所定义的
        return method.getDeclaringClass().equals(Object.class);
    }

    public static String methodSign(Method method){
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append("_").append(c.getCanonicalName())
        );
        return sb.toString();
    }

    public static String methodSign(Method method, Class cls){
       return null;
    }

    // 获取这个类中的fields
    public static List<Field> findAnnotatedFields(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        while(aClass != null){
            // 这里获取到的是spring增强后的（代理过的），所以getDeclaredFields获取不到对应的fields
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
//                if (field.isAnnotationPresent(ClotConsumer.class)){
                if (field.isAnnotationPresent(annotationClass)){
                    result.add(field);
                }
            }
            // 这里是为了获取到真实那个类
            aClass = aClass.getSuperclass();
        }
        return result;
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                m -> System.out.println(methodSign(m))
        );
    }
}
