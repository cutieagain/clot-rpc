package cn.cutie.clotrpc.core.api;

import lombok.Data;

import java.util.List;

@Data
public class RpcContext {
    Router router;
    LoadBalance loadBalance;
    List<Filter> filters; // TODO: 2024/3/16
}
