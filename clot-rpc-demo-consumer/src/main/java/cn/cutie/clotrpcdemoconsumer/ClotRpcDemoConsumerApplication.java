package cn.cutie.clotrpcdemoconsumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.consumer.ConsumerConfig;
import cn.cutie.clotrpc.demo.api.Order;
import cn.cutie.clotrpc.demo.api.OrderService;
import cn.cutie.clotrpc.demo.api.User;
import cn.cutie.clotrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@SpringBootApplication
@Import({ConsumerConfig.class})
public class ClotRpcDemoConsumerApplication {

    // 1、可以在Spring上下文初始化完毕后扫描
    // 2、显示创建一个bean，使用ApplicationRunner，它是在所有都执行完毕后执行
    // 3、implements InstantiationAwareBeanPostProcessor，是用来处理bean里面的属性的。可以把userService看做
    // ClotRpcDemoConsumerApplication里面的一个属性
    @ClotConsumer
    UserService userService;

    @ClotConsumer
    OrderService orderService;

    @Autowired
    Demo2 demo2;

    @RequestMapping("/findById")
    public User findById(int id){
        return userService.findById(id);
    }

    public static void main(String[] args) {
        SpringApplication.run(ClotRpcDemoConsumerApplication.class, args);
    }

    @Bean
    // 多个 ApplicationRunner ，先执行外面的
    // ApplicationRunner的问题，如果执行失败了，后面的ApplicationRunner就不执行了，
    // 即cn.cutie.clotrpc.core.consumer.ConsumerConfig.consumerBootstrapRunner中的不执行了
    public ApplicationRunner consumerRunner(){
        return x ->{
//            User user = userService.findById(1);
//            System.out.println("RPC result userService.findById(1) = " + user);
//
//            // 正常逻辑
//            Order order = orderService.findById(1);
//            System.out.println("RPC result orderService.findById(1) = " + order);
//
//            // 异常逻辑
//            Order order404 = orderService.findById(404);
//            System.out.println("RPC result orderService.findById(404) = " + order404);
//
//            // 在其他component里面进行调用测试
//            demo2.test();
//
//            // findById 重载的方法
//            user = userService.findById(1, "cutie");
//            System.out.println("RPC result userService.findById(1, \"cutie\") = " + user);
//
//            String name1 = userService.getName();
//            String name2 = userService.getName(123);
//            System.out.println("name1 :" + name1);
//            System.out.println("name2 :" + name2);
//
//            System.out.println(userService.getId(1L));
//
//            System.out.println(userService.getId(new User(100, "clot")));
//            System.out.println(userService.getId(10f));

//            System.out.println(Arrays.toString(userService.getIds()));
//            System.out.println(" ===> userService.getIds()");
//            for (int id : userService.getIds()) {
//                System.out.println(id);
//            }

//            System.out.println(" ===> userService.getLongIds()");
//            long[] longs = userService.getLongIds();
//            for (long id : longs) {
//                System.out.println(id);
//            }

            System.out.println(" ===> userService.getLongIds()");
            int[] ids = userService.getIds(new int[]{111,222,333});
            for (long id : ids) {
                System.out.println(id);
            }

        };
    }

}
