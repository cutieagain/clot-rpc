package cn.cutie.clotrpc.core.api;

import cn.cutie.clotrpc.core.registry.ChangedListener;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

/**
 * 注册中心
 */
public interface RegistryCenter {

    /*********************************************
     *          provider侧 + consumer侧
     *********************************************/
    void start();

    void stop();

    /*********************************************
     *                  provider侧
     *********************************************/
    // 自己能提供的服务注册到注册中心上
    void register(String service, String instance);

    // 主动反注册，或者注册中心感知后进行反注册
    void unRegister(String service, String instance);

    /*********************************************
     *                  consumer侧
     *********************************************/
    // 获取该服务所有的实例
    List<String> fetchAll(String service);

    /**
     * 订阅信息
     * 监听
     * @param service
     * @param listener
     */
    void subscribe(String service, ChangedListener listener);

    // 心跳
//    void heartbeat();


    /**
     * 静态的注册中心
     */
    class StaticRegistryCenter implements RegistryCenter {

        List<String> providers;

        public StaticRegistryCenter(List<String> providers){
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(String service, String instance) {

        }

        @Override
        public void unRegister(String service, String instance) {

        }

        @Override
        public List<String> fetchAll(String service) {
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener listener) {

        }
    }
}
