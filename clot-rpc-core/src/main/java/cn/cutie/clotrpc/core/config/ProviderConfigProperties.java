package cn.cutie.clotrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "clotrpc.provider")
public class ProviderConfigProperties {

    // for provider

    Map<String, String> metas = new HashMap<>();


}
