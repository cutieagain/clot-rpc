package cn.cutie.clotrpc.demo.provider;

import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.provider.ProviderBootstrap;
import cn.cutie.clotrpc.core.provider.ProviderConfig;
import cn.cutie.clotrpc.core.provider.ProviderInvoker;
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

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
@Slf4j
public class ClotRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClotRpcDemoProviderApplication.class, args);
    }

    @Autowired
    ProviderInvoker providerInvoker;
    @Autowired
    UserService userService;

    // 使用HTTP + JSON来实现序列化和通信
    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request){
         return providerInvoker.invoke(request);
    }

    @RequestMapping("/setTimeoutPorts")
    public RpcResponse<String> setTimeoutPorts(@RequestParam("ports") String ports){
        userService.setTimeoutPorts(ports);
        RpcResponse<String> rpcResponse = new RpcResponse<>();
        rpcResponse.setStatus(true);
        rpcResponse.setData("ok: " + ports);
        return rpcResponse;
    }

    // ApplicationRunner 会在spring容器都准备好了之后执行
    @Bean
    ApplicationRunner providerRun(){
        return x -> {
            // 测试1个参数的方法
            RpcRequest request = new RpcRequest();
            request.setService("cn.cutie.clotrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});

            RpcResponse rpcResponse = this.invoke(request);
            log.info("return : " + rpcResponse.getData());

            // 测试2个参数的方法
            request = new RpcRequest();
            request.setService("cn.cutie.clotrpc.demo.api.UserService");
            request.setMethodSign("findById@2_int_java.lang.String");
            request.setArgs(new Object[]{100, "Sleep-"});

            rpcResponse = this.invoke(request);
            log.info("return : " + rpcResponse.getData());
        };
    }

}
