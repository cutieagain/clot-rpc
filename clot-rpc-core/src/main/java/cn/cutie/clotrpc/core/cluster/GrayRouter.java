package cn.cutie.clotrpc.core.cluster;

import cn.cutie.clotrpc.core.api.Router;
import cn.cutie.clotrpc.core.meta.InstanceMata;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Description: 灰度路由
 * @Author: Cutie
 * @CreateDate: 2024/4/2 15:55
 * @Version: 0.0.1
 */
@Slf4j
@Data
public class GrayRouter implements Router<InstanceMata> {

    private int grayRatio;

    private final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }


    @Override
    public List<InstanceMata> route(List<InstanceMata> providers) {
        // 如果只有一个或者为null，直接返回
        if (providers == null || providers.size() <= 1){
            return providers;
        }
        List<InstanceMata> normalNodes = new ArrayList<>();
        List<InstanceMata> grayNodes = new ArrayList<>();
        providers.forEach( p->{
            if ("true".equals(p.getParameters().get("gray"))){
                grayNodes.add(p);
            } else{
                normalNodes.add(p);
            }
        });

        log.debug(" grayRouter grayNodes/normalNodes:{}, grayRatio:{}", grayNodes.size()/normalNodes.size(), grayRatio);

        // 正常节点为空，返回灰度的节点，灰度也是
        if (normalNodes.isEmpty() || grayNodes.isEmpty()) return providers;
        // 获取为0，返回正常节点
        if (grayRatio <= 0) {
            return normalNodes;
        } else if (grayRatio >= 100){
            return grayNodes;
        }

        // grayRatio == 10，10次一次灰度
        // 1、返回集合，gray节点比整体节点为1：9，不够的话可以扩为虚拟节点
        // 2、更简单的方法：在a情况下，返回normal节点，
        // b的情况下返回gray节点
        if (random.nextInt(100) < grayRatio){
            log.debug(" grayRouter grayNodes ===> {}", grayNodes);
            return grayNodes;
        } else{
            log.debug(" grayRouter normalNodes ===> {}", normalNodes);
            return normalNodes;
        }

    }
}
