package cn.cutie.clotrpc.demo.provider;

import cn.cutie.clotrpc.core.test.TestZKServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {ClotRpcDemoProviderApplication.class})
class ClotRpcDemoProviderApplicationTests {

    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    static void init(){
        zkServer.start();
    }

    @Test
    void contextLoads() {
        System.out.println("===> ClotRpcDemoProviderApplicationTests contextLoads.");
    }

    @AfterAll
    static void destroy(){
        zkServer.stop();
    }

}
