package cn.cutie.clotrpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

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

}
