package cn.cutie.clotrpc.core.api;

import cn.cutie.clotrpc.core.meta.InstanceMata;
import cn.cutie.clotrpc.core.meta.ServiceMeta;
import cn.cutie.clotrpc.core.registry.ChangedListener;

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
    void register(ServiceMeta service, InstanceMata instance);

    // 主动反注册，或者注册中心感知后进行反注册
    void unRegister(ServiceMeta service, InstanceMata instance);

    /*********************************************
     *                  consumer侧
     *********************************************/
    // 获取该服务所有的实例
    List<InstanceMata> fetchAll(ServiceMeta service);

    /**
     * 订阅信息
     * 监听
     * @param service
     * @param listener
     */
    void subscribe(ServiceMeta service, ChangedListener listener);

    // 心跳
//    void heartbeat();


    /**
     * 静态的注册中心
     */
    class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMata> providers;

        public StaticRegistryCenter(List<InstanceMata> providers){
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(ServiceMeta service, InstanceMata instance) {

        }

        @Override
        public void unRegister(ServiceMeta service, InstanceMata instance) {

        }

        @Override
        public List<InstanceMata> fetchAll(ServiceMeta service) {
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {

        }
    }
}
