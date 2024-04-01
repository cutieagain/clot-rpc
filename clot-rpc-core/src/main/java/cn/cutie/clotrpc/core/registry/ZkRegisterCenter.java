package cn.cutie.clotrpc.core.registry;

import cn.cutie.clotrpc.core.api.RegistryCenter;
import cn.cutie.clotrpc.core.api.RpcException;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.meta.ServiceMeta;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * zk注册中心
 * 集成ZK：zkClinet和官方推荐的curator
 */
@Slf4j
public class ZkRegisterCenter implements RegistryCenter {

    private CuratorFramework client = null;

    @Value("${clotrpc.zkServer}")
    String servers;

    @Value("${clotrpc.zkRoot}")
    String root;


    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); // 1,2,4,8秒进行重试
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        log.info(" ===> ZkRegisterCenter starting to server[" + servers + "/" + root + "].");
    }

    @Override
    public void stop() {
        client.close();
        log.info(" ===> ZkRegisterCenter stopped.");
        // TODO: 2024/3/20 先关cache，再关client
    }

    @Override
    public void register(ServiceMeta service, InstanceMata instance) {
        // 服务和实例写到zk里面
        // 持久化节点，临时节点
        // 服务创建为持久节点，服务下面的实例为临时节点

        // 服务路径
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());

            log.info(" ===> register to zk:" + instancePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unRegister(ServiceMeta service, InstanceMata instance) {
        String servicePath = "/" + service;
        try {
            // 服务的持久化节点不存在
            if (client.checkExists().forPath(servicePath) == null){
                return;
            }
            // 实例的临时节点删除
            String instancePath = servicePath + "/" + instance.toPath();
            client.delete().quietly().forPath(instancePath);

            log.info(" ===> unRegister to zk:" + instancePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public List<InstanceMata> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> fetchAll from zk:" + servicePath);
            nodes.forEach(System.out::println);
            return mapInstance(nodes);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @NotNull
    private static List<InstanceMata> mapInstance(List<String> nodes) {
        List<InstanceMata> providers = nodes.stream().map(x -> {
            String[] strs = x.split("_");
            return InstanceMata.http(strs[0], Integer.valueOf(strs[1]));
        }).collect(Collectors.toList());
        return providers;
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        // zk树结构节点的监听功能
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        cache.getListenable().addListener(
                (curator, event) -> {
                    // 有任何节点变动，这里会执行
                    log.info("zk subscribe event: " + event);
                    List<InstanceMata> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                }
        );
        cache.start();
    }
}
