package cn.cutie.clotrpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 描述服务实例元数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMata {
    // 目前是http
    private String scheme;
    private String host;
    private Integer port;
    // 访问路径上下文
    private String context;
    // 实例状态 online,offline
    private boolean status;
    // 参数：哪个机房之类的
    private Map<String, String> parameters;

    public InstanceMata(String scheme, String host, Integer port, String context) {
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

    public static InstanceMata http(String host, Integer port){
        return new InstanceMata("http", host, port, "");
    }
}
