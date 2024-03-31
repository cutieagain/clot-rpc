package cn.cutie.clotrpc.core.api;

import cn.cutie.clotrpc.core.meta.InstanceMata;
import lombok.Data;

import java.util.List;

@Data
public class RpcContext {
    Router<InstanceMata> router;
    LoadBalance<InstanceMata> loadBalance;
    List<Filter> filters;
}
