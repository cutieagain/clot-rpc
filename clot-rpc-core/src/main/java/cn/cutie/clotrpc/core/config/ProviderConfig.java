package cn.cutie.clotrpc.core.config;

import cn.cutie.clotrpc.core.api.RegistryCenter;
import cn.cutie.clotrpc.core.config.AppConfigProperties;
import cn.cutie.clotrpc.core.config.ProviderConfigProperties;
import cn.cutie.clotrpc.core.consumer.ConsumerBootstrap;
import cn.cutie.clotrpc.core.provider.ProviderBootstrap;
import cn.cutie.clotrpc.core.provider.ProviderInvoker;
import cn.cutie.clotrpc.core.registry.ZkRegisterCenter;
import cn.cutie.clotrpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@Slf4j
@Import({AppConfigProperties.class, ProviderConfigProperties.class, SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port:8080}")
    private String port;

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ProviderConfigProperties providerConfigProperties;

    // 把ProviderBootstrap变成一个bean放在Spring里面
    @Bean
    ProviderBootstrap providerBootstrap(){
        return new ProviderBootstrap(port, appConfigProperties, providerConfigProperties);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap){
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap){
        return x ->{
            log.info("providerBootstrapRunner starting...");
            providerBootstrap.start();
            log.info("providerBootstrapRunner started...");
        };
    }

    /***
     * stop是在bean销毁的时候执行的
     * 在@PreDestroy之前执行的
     * (initMethod = "start", destroyMethod = "stop")
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    RegistryCenter providerRc(){
        return new ZkRegisterCenter();
    }
}
