package cn.cutie.clotrpc.core.consumer;

import cn.cutie.clotrpc.core.api.LoadBalance;
import cn.cutie.clotrpc.core.api.RegistryCenter;
import cn.cutie.clotrpc.core.api.Router;
import cn.cutie.clotrpc.core.cluster.RandomLoadBalancer;
import cn.cutie.clotrpc.core.cluster.RoundRobinLoadBalancer;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.registry.ZkRegisterCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class ConsumerConfig {

    @Value("${clotrpc.providers}")
    String servers;

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
            System.out.println("consumerBootstrapRunner starting...");
            consumerBootstrap.start();
            System.out.println("consumerBootstrapRunner started...");
        };
    }

    @Bean
    public LoadBalance<InstanceMata> loadBalance(){
//        return LoadBalance.Default;
        return new RandomLoadBalancer();
//        return new RoundRobinLoadBalancer();
    }

    @Bean
    public Router<InstanceMata> router(){
        return Router.Default;
    }

    /**
     * 需要自动初始化和销毁
     * 与zk交互，连接初始化和停止
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    RegistryCenter consumerRc(){
//        return new RegistryCenter.StaticRegistryCenter(List.of(servers.split(",")));
        return new ZkRegisterCenter();
    }

}
