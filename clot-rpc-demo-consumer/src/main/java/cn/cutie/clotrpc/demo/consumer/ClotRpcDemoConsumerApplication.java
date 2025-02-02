package cn.cutie.clotrpc.demo.consumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import cn.cutie.clotrpc.core.api.Router;
import cn.cutie.clotrpc.core.api.RpcContext;
import cn.cutie.clotrpc.core.cluster.GrayRouter;
import cn.cutie.clotrpc.core.config.ConsumerConfig;
import cn.cutie.clotrpc.demo.api.OrderService;
import cn.cutie.clotrpc.demo.api.User;
import cn.cutie.clotrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SpringBootApplication
@Import({ConsumerConfig.class})
@Slf4j
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
    public User findById(@RequestParam("id") int id){
        return userService.findById(id);
    }

    @RequestMapping("/find")
    public User find(@RequestParam("timeout") int timeout){
        return userService.find(timeout);
    }

    @Autowired
    Router router;

    @RequestMapping("/gray")
    public String gray(@RequestParam("ratio") int ratio){
        ((GrayRouter)router).setGrayRatio(ratio);
        return "ok: new gray ratio is " + ratio;
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
            test();
        };
    }

    private void test() {
        // 常规int类型，返回User对象
        log.info("Case 1. >>===[常规int类型，返回User对象]===");
        User user = userService.findById(1);
        log.info("RPC result userService.findById(1) = " + user);

        // 测试方法重载，同名方法，参数不同
        log.info("Case 2. >>===[测试方法重载，同名方法，参数不同===");
        User user1 = userService.findById(1, "clot");
        log.info("RPC result userService.findById(1, \"clot\") = " + user1);

        // 测试返回字符串
        log.info("Case 3. >>===[测试返回字符串]===");
        log.info("userService.getName() = " + userService.getName());

        // 测试重载方法返回字符串
        log.info("Case 4. >>===[测试重载方法返回字符串]===");
        log.info("userService.getName(123) = " + userService.getName(123));

        // 测试local toString方法
//            log.info("Case 5. >>===[测试local toString方法]===");
//            log.info("userService.toString() = " + userService.toString());

        // 测试long类型
        log.info("Case 6. >>===[常规int类型，返回User对象]===");
        log.info("userService.getId(10) = " + userService.getId(10));

        // 测试long+float类型
        log.info("Case 7. >>===[测试long+float类型]===");
        log.info("userService.getId(10f) = " + userService.getId(10f));

        // 测试参数是User类型
        log.info("Case 8. >>===[测试参数是User类型]===");
        log.info("userService.getId(new User(100,\"clot\")) = " +
                userService.getId(new User(100,"clot")));


        log.info("Case 9. >>===[测试返回long[]]===");
        log.info(" ===> userService.getLongIds(): ");
        for (long id : userService.getLongIds()) {
            log.info("", id);
        }

        log.info("Case 10. >>===[测试参数和返回值都是long[]]===");
        log.info(" ===> userService.getLongIds(): ");
        for (long id : userService.getIds(new int[]{4,5,6})) {
            log.info("", id);
        }

        // 测试参数和返回值都是List类型
        log.info("Case 11. >>===[测试参数和返回值都是List类型]===");
        List<User> list = userService.getList(List.of(
                new User(100, "clot-100"),
                new User(101, "clot-101")));
        list.forEach(System.out::println);

        // 测试参数和返回值都是Map类型
        log.info("Case 12. >>===[测试参数和返回值都是Map类型]===");
        Map<String, User> map = new HashMap<>();
        map.put("A200", new User(200, "clot-200"));
        map.put("A201", new User(201, "clot-201"));
        userService.getMap(map).forEach(
                (k,v) -> log.info(k + " -> " + v)
        );

        log.info("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
        log.info("userService.getFlag(false) = " + userService.getFlag(false));

        log.info("Case 14. >>===[测试参数和返回值都是User[]类型]===");
        User[] users = new User[]{
                new User(100, "clot-100"),
                new User(101, "clot-101")};
        Arrays.stream(userService.findUsers(users)).forEach(System.out::println);

        log.info(("Case 15. >>===[测试参数为long，返回值是User类型]==="));
        User userLong = userService.findById(10000L);
        log.info(userLong.toString());

        log.info(("Case 16. >>===[测试参数为boolean，返回值都是User类型]==="));
        User user100 = userService.ex(false);
        log.info(user100.toString());

        log.info(("Case 17. >>===[测试服务端抛出一个RuntimeException异常]==="));
        try {
            User userEx = userService.ex(true);
            log.info((userEx.toString()));
        } catch (RuntimeException e) {
            log.info((" ===> exception: " + e.getMessage()));
        }

        log.info("Case 18. >>===[测试服务端抛出一个超时重试后成功的场景]===");
        // 超时设置的【漏斗原则】
        // A 2000 -> B 1500 -> C 1200 -> D 1000
        long start = System.currentTimeMillis();
        userService.find(1100);
        userService.find(1100);
        log.info("userService.find take "
                + (System.currentTimeMillis()-start) + " ms");

        log.info("Case 19. >>===[测试通过Context跨消费者和提供者进行传参]===");
        String Key_Version = "rpc.version";
        String Key_Message = "rpc.message";
        RpcContext.setContextParameter(Key_Version, "v8");
        RpcContext.setContextParameter(Key_Message, "this is a test message");
        String version = userService.echoParameter(Key_Version);
        String message = userService.echoParameter(Key_Message);
        log.info(" ===> echo parameter from c->p->c: " + Key_Version + " -> " + version);
        log.info(" ===> echo parameter from c->p->c: " + Key_Message + " -> " + message);
        RpcContext.ContextParameters.get().clear();
    }

}
