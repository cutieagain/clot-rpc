package cn.cutie.clotrpc.core.api;

import cn.cutie.clotrpc.core.meta.InstanceMata;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {
    Router<InstanceMata> router;
    LoadBalance<InstanceMata> loadBalance;
    List<Filter> filters;
    private Map<String, String> parameters = new HashMap<>();
}
