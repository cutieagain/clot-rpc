package cn.cutie.clotrpc.core.api;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
@Data
public class RpcRequest {
    // 调用的服务的信息
    private String service; // 接口 cn.cutie.clotrpc.demo.api.UserService
//    private String method; // 方法 findById
    private String methodSign; // 方法 findById@int， findById@long
    private Object[] args; // 参数 100

    // 跨调用方需要传递的参数
    private Map<String,String> params = new HashMap<>();
}
