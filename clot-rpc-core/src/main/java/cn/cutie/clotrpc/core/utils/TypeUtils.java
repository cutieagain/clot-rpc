package cn.cutie.clotrpc.core.utils;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;

public class TypeUtils {
    public static Object cast(Object originObj, Class<?> type){
        if (originObj == null) return null;
        Class<?> aClass = originObj.getClass();
        // 如果originObj是要返回类型的子类型，直接返回originObj即可
        if (type.isAssignableFrom(aClass)){
            return originObj;
        }

        // 解决对象传值问题：例如User对象，序列化接收后为LinkedHashMap
        if (originObj instanceof HashMap map){
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }

        if (type.isArray()){
            if (originObj instanceof List list){
                originObj = list.toArray();
            }
            // 数组中元素的类型
            Class<?> componentType = type.getComponentType(); // 元素类型
            System.out.println(" ===> componentType:" + componentType);
            // 创建一个这个类型的数组
            int length = Array.getLength(originObj);
            Object resultArray = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(resultArray, i, Array.get(originObj, i));
            }
            return resultArray;
        }

        // 解决传值Float结果转换为Double的问题
        if (type.equals(Long.class) || type.equals(Long.TYPE)){
            return Long.valueOf(originObj.toString());
        } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)){
            return Integer.valueOf(originObj.toString());
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)){
            return Double.valueOf(originObj.toString());
        } else if (type.equals(Float.class) || type.equals(Float.TYPE)){
            return Float.valueOf(originObj.toString());
        } else if (type.equals(Byte.class) || type.equals(Byte.TYPE)){
            return Byte.valueOf(originObj.toString());
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)){
            return Boolean.valueOf(originObj.toString());
        } else if (type.equals(Short.class) || type.equals(Short.TYPE)){
            return Short.valueOf(originObj.toString());
        } else if (type.equals(Character.class) || type.equals(Character.TYPE)){
            return Character.valueOf(originObj.toString().charAt(0));
        }
        return null;
    }
}
