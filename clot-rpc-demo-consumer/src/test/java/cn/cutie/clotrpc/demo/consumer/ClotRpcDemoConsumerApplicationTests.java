package cn.cutie.clotrpc.demo.consumer;

import cn.cutie.clotrpc.core.test.TestZKServer;
import cn.cutie.clotrpc.demo.provider.ClotRpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = {ClotRpcDemoConsumerApplication.class})
class ClotRpcDemoConsumerApplicationTests {
    static ApplicationContext applicationContext;

    static TestZKServer zkServer = new TestZKServer();

    /**
     * 执行启动provider的逻辑
     */
    @BeforeAll
    static void init(){
        zkServer.start();
        applicationContext = SpringApplication.run(ClotRpcDemoProviderApplication.class,
                "--server.port=8084",
                "--logging.level.cn.cutie=debug",
                "--clotrpc.zkServer=localhost:2182");
    }

    @Test
    void contextLoads() {
        System.out.println("===> ClotRpcDemoConsumerApplicationTests contextLoads.");
    }

    /**
     * 执行关闭provider的逻辑
     */
    @AfterAll
    static void destroy(){
        SpringApplication.exit(applicationContext, () -> 1);
        zkServer.stop();
    }

}
