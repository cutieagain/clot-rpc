package cn.cutie.clotrpc.core.utils;

import cn.cutie.clotrpc.core.api.RpcResponse;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * @Description:
 * @Author: Cutie
 * @CreateDate: 2024/3/31 16:35
 * @Version: 0.0.1
 */
public class MockUtils {

    public static RpcResponse mockRpcResp(Class type){
        RpcResponse response = new RpcResponse();
        response.setStatus(true);
        response.setData(mock(type));
        return response;
    }

    public static Object mock(Class type) {
        if(type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        } else if(type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 10000L;
        }
        if(Number.class.isAssignableFrom(type)) {
            return 1;
        }
        if(type.equals(String.class)) {
            return "this_is_a_mock_string";
        }
        return  mockPojo(type);
    }

    @SneakyThrows
    private static Object mockPojo(Class type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            Class<?> fType = f.getType();
            Object fValue = mock(fType);
            f.set(result, fValue);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(mock(UserDto.class));
    }

    public static class UserDto{
        private int a;
        private String b;

        @Override
        public String toString() {
            return a + "," + b;
        }
    }
}
