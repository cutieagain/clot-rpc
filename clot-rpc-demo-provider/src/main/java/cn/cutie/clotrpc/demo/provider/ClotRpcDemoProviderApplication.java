package cn.cutie.clotrpc.demo.provider;

import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.config.ProviderConfig;
import cn.cutie.clotrpc.core.provider.ProviderInvoker;
import cn.cutie.clotrpc.demo.api.User;
import cn.cutie.clotrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
@Slf4j
public class ClotRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClotRpcDemoProviderApplication.class, args);
    }

//    @Autowired
//    ProviderInvoker providerInvoker;
//    @Autowired
//    UserService userService;
//
//    // 使用HTTP + JSON来实现序列化和通信
//    @RequestMapping("/")
//    public RpcResponse<Object> invoke(@RequestBody RpcRequest request){
//         return providerInvoker.invoke(request);
//    }
//
//    @RequestMapping("/setTimeoutPorts")
//    public RpcResponse<String> setTimeoutPorts(@RequestParam("ports") String ports){
//        userService.setTimeoutPorts(ports);
//        RpcResponse<String> rpcResponse = new RpcResponse<>();
//        rpcResponse.setStatus(true);
//        rpcResponse.setData("ok: " + ports);
//        return rpcResponse;
//    }
//
//    // ApplicationRunner 会在spring容器都准备好了之后执行
//    @Bean
//    ApplicationRunner providerRun(){
//        return x -> {
//           testAll();
//        };
//    }
//
//    private void testAll() {
//        //  test 1 parameter method
//        System.out.println("Provider Case 1. >>===[基本测试：1个参数]===");
//        RpcRequest request = new RpcRequest();
//        request.setService("cn.cutie.clotrpc.demo.api.UserService");
//        request.setMethodSign("findById@1_int");
//        request.setArgs(new Object[]{100});
//
//        RpcResponse<Object> rpcResponse = invoke(request);
//        System.out.println("return : "+rpcResponse.getData());
//
//        // test 2 parameters method
//        System.out.println("Provider Case 2. >>===[基本测试：2个参数]===");
//        RpcRequest request1 = new RpcRequest();
//        request1.setService("cn.cutie.clotrpc.demo.api.UserService");
//        request1.setMethodSign("findById@2_int_java.lang.String");
//        request1.setArgs(new Object[]{100, "CC"});
//
//        RpcResponse<Object> rpcResponse1 = invoke(request1);
//        System.out.println("return : "+rpcResponse1.getData());
//
//        // test 3 for List<User> method&parameter
//        System.out.println("Provider Case 3. >>===[复杂测试：参数类型为List<User>]===");
//        RpcRequest request3 = new RpcRequest();
//        request3.setService("cn.cutie.clotrpc.demo.api.UserService");
//        request3.setMethodSign("getList@1_java.util.List");
//        List<User> userList = new ArrayList<>();
//        userList.add(new User(100, "Clot100"));
//        userList.add(new User(101, "Clot101"));
//        request3.setArgs(new Object[]{ userList });
//        RpcResponse<Object> rpcResponse3 = invoke(request3);
//        System.out.println("return : "+rpcResponse3.getData());
//
//        // test 4 for Map<String, User> method&parameter
//        System.out.println("Provider Case 4. >>===[复杂测试：参数类型为Map<String, User>]===");
//        RpcRequest request4 = new RpcRequest();
//        request4.setService("cn.cutie.clotrpc.demo.api.UserService");
//        request4.setMethodSign("getMap@1_java.util.Map");
//        Map<String, User> userMap = new HashMap<>();
//        userMap.put("P100", new User(100, "Clot100"));
//        userMap.put("P101", new User(101, "Clot101"));
//        request4.setArgs(new Object[]{ userMap });
//        RpcResponse<Object> rpcResponse4 = invoke(request4);
//        System.out.println("return : "+rpcResponse4.getData());
//    }

}
