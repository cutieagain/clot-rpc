package cn.cutie.clotrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务实例元数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMeta {
    // 目前是http
    private String scheme;
    private String host;
    private Integer port;
    // 访问路径上下文
    private String context;
    // 实例状态 online,offline
    private boolean status;
    // 参数：哪个机房之类的
    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }


    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public static InstanceMeta http(String host, Integer port){
        return new InstanceMeta("http", host, port, "clotrpc");
    }

    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }
}
