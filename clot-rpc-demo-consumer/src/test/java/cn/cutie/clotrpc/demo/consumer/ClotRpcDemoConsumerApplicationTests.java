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
    static ApplicationContext context1;
    static ApplicationContext context2;

    static TestZKServer zkServer = new TestZKServer();

    /**
     * 执行启动provider的逻辑
     */
    @BeforeAll
    static void init(){
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============     ZK2182    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        zkServer.start();
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8094    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context1 = SpringApplication.run(ClotRpcDemoProviderApplication.class,
                        "--server.port=8094",
                        "--clotrpc.zk.server=localhost:2182",
                        "--clotrpc.app.env=test",
                        "--logging.level.cn.cutie.clotrpc=info",
                        "--clotrpc.provider.metas.dc=bj",
                        "--clotrpc.provider.metas.gray=false",
                        "--clotrpc.provider.metas.unit=B001");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8095    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context2 = SpringApplication.run(ClotRpcDemoProviderApplication.class,
                "--server.port=8095",
                "--kkrpc.zk.server=localhost:2182",
                "--kkrpc.app.env=test",
                "--logging.level.cn.cutie.clotrpc=info",
                "--kkrpc.provider.metas.dc=bj",
                "--kkrpc.provider.metas.gray=false",
                "--kkrpc.provider.metas.unit=B002"
        );
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
        SpringApplication.exit(context1, () -> 1);
        SpringApplication.exit(context2, () -> 1);
        zkServer.stop();
    }

}
