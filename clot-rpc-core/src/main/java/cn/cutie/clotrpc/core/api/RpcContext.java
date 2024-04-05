package cn.cutie.clotrpc.core.api;

import cn.cutie.clotrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {
    Router<InstanceMeta> router;
    LoadBalancer<InstanceMeta> loadBalancer;
    List<Filter> filters;
    private Map<String, String> parameters = new HashMap<>();

    public String param(String key) {
        return parameters.get(key);
    }

    public static ThreadLocal<Map<String,String>> ContextParameters = new ThreadLocal<>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    public static void setContextParameter(String key, String value) {
        ContextParameters.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return ContextParameters.get().get(key);
    }

    public static void removeContextParameter(String key) {
        ContextParameters.get().remove(key);
    }
}
