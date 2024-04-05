package cn.cutie.clotrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "clotrpc.app")
public class AppConfigProperties {

    // for app instance
    private String id = "app1";

    private String namespace = "public";

    private String env = "dev";

}
