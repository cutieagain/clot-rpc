package cn.cutie.clotrpc.core.registry;

import cn.cutie.clotrpc.core.api.RegistryCenter;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * zk注册中心
 * 集成ZK：zkClinet和官方推荐的curator
 */
public class ZkRegisterCenter implements RegistryCenter {

    private CuratorFramework client = null;


    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); // 1,2,4,8秒进行重试
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("clot-rpc")
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        System.out.println(" ===> ZkRegisterCenter started");
    }

    @Override
    public void stop() {
        client.close();
        System.out.println(" ===> ZkRegisterCenter stoped");
        // TODO: 2024/3/20 先关cache，再关client
    }

    @Override
    public void register(String service, String instance) {
        // 服务和实例写到zk里面
        // 持久化节点，临时节点
        // 服务创建为持久节点，服务下面的实例为临时节点

        // 服务路径
        String servicePath = "/" + service;
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());

            System.out.println(" ===> register to zk:" + instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unRegister(String service, String instance) {
        String servicePath = "/" + service;
        try {
            // 服务的持久化节点不存在
            if (client.checkExists().forPath(servicePath) == null){
                return;
            }
            // 实例的临时节点删除
            String instancePath = servicePath + "/" + instance;
            client.delete().quietly().forPath(instancePath);

            System.out.println(" ===> unRegister to zk:" + instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> fetchAll(String service) {
        String servicePath = "/" + service;
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println(" ===> fetchAll from zk:" + servicePath);
            nodes.forEach(System.out::println);
            return nodes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void subscribe(String service, ChangedListener listener) {
        // zk树结构节点的监听功能
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service)
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        cache.getListenable().addListener(
                (curator, event) -> {
                    // 有任何节点变动，这里会执行
                    System.out.println("zk subscribe event: " + event);
                    List<String> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                }
        );
        cache.start();
    }
}
