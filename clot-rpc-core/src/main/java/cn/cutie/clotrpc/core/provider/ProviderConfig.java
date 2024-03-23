package cn.cutie.clotrpc.core.provider;

import cn.cutie.clotrpc.core.api.RegistryCenter;
import cn.cutie.clotrpc.core.consumer.ConsumerBootstrap;
import cn.cutie.clotrpc.core.registry.ZkRegisterCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@Slf4j
public class ProviderConfig {

    // 把ProviderBootstrap变成一个bean放在Spring里面
    @Bean
    ProviderBootstrap providerBootstrap(){
        return new ProviderBootstrap();
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap){
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap){
        return x ->{
            log.debug("providerBootstrapRunner starting...");
            providerBootstrap.start();
            log.debug("providerBootstrapRunner started...");
        };
    }

    /***
     * stop是在bean销毁的时候执行的
     * 在@PreDestroy之前执行的
     * (initMethod = "start", destroyMethod = "stop")
     * @return
     */
    @Bean
    RegistryCenter providerRc(){
        return new ZkRegisterCenter();
    }
}
