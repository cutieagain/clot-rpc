package cn.cutie.clotrpc.core.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

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

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                m -> System.out.println(methodSign(m))
        );
    }
}
