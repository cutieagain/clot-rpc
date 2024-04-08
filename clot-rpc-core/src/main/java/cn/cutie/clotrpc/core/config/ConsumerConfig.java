package cn.cutie.clotrpc.core.config;

import cn.cutie.clotrpc.core.api.*;
import cn.cutie.clotrpc.core.cluster.GrayRouter;
import cn.cutie.clotrpc.core.cluster.RandomLoadBalancer;
import cn.cutie.clotrpc.core.consumer.ConsumerBootstrap;
import cn.cutie.clotrpc.core.filter.ParameterFilter;
import cn.cutie.clotrpc.core.meta.InstanceMeta;
import cn.cutie.clotrpc.core.registry.ZkRegisterCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

@Slf4j
@Configuration
@Import({AppConfigProperties.class, ConsumerConfigProperties.class})
public class ConsumerConfig {

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ConsumerConfigProperties consumerConfigProperties;

    // 把ConsumerBootstrap变成一个bean放在Spring里面
    @Bean
    ConsumerBootstrap consumerBootstrap(){
        return new ConsumerBootstrap();
    }

    // 已经配置了ConsumerBootstrap这个Bean，直接Autowired注入进来
    // 多个 ApplicationRunner ，先执行最外面的，这里设置一下Order值，值越小优先级越高
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap){
        return x ->{
            log.info("consumerBootstrapRunner starting...");
            consumerBootstrap.start();
            log.info("consumerBootstrapRunner started...");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalance(){
//        return LoadBalance.Default;
        return new RandomLoadBalancer();
//        return new RoundRobinLoadBalancer();
    }

    @Bean
    public Router<InstanceMeta> router(){
//        return Router.Default;
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    /**
     * 需要自动初始化和销毁
     * 与zk交互，连接初始化和停止
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumerRc(){
//        return new RegistryCenter.StaticRegistryCenter(List.of(servers.split(",")));
        return new ZkRegisterCenter();
    }

    @Bean
    public Filter filter(){
//        return Filter.Default;
//        return new CacheFilter();
//        return new MockFilter();
        return new ParameterFilter();
    }

    @Bean
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());
        context.getParameters().put("consumer.retries", String.valueOf(consumerConfigProperties.getRetries()));
        context.getParameters().put("consumer.timeout", String.valueOf(consumerConfigProperties.getTimeout()));
        context.getParameters().put("consumer.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
        context.getParameters().put("consumer.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
        context.getParameters().put("consumer.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));
        return context;
    }

}
