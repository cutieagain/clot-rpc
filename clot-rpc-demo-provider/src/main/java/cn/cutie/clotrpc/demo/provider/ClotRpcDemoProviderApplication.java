package cn.cutie.clotrpc.demo.provider;

import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.provider.ProviderBootstrap;
import cn.cutie.clotrpc.core.provider.ProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class ClotRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClotRpcDemoProviderApplication.class, args);
    }

    @Autowired
    ProviderBootstrap providerBootstrap;

    // 使用HTTP + JSON来实现序列化和通信
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request){
         return providerBootstrap.invokeRequest(request);
    }

    // ApplicationRunner 会在spring容器都准备好了之后执行
    @Bean
    ApplicationRunner providerRun(){
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("cn.cutie.clotrpc.demo.api.UserService");
            request.setMethod("findById");
            request.setArgs(new Object[]{100});

            RpcResponse rpcResponse = this.invoke(request);
            System.out.println("return : " + rpcResponse.getData());
        };
    }

}
