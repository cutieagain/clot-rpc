package cn.cutie.clotrpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * 描述服务元数据
 */
@Data
@Builder
public class ServiceMeta {
    // 应用
    private String app;
    // 命名空间
    private String namespace;
    // 环境
    private String env;
    // 服务名称
    private String name;
    // 版本号
    private String version;

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }
}
