package cn.cutie.clotrpc.core.api;

import java.util.List;


/**
 * 随机，轮询。权重 weightedRR，AAWR-自适应
 *
 * 权重
 * 8081,w=100，25次
 * 8082,w=300，75次
 * 0-1，random 0-99,  8081 < 25 <= 8082
 *
 * 自适应
 * 记录响应时间
 * 8081，10ms
 * 8082，100ms
 * 根据平均时间调整他们的权重
 * 平均响应时间 * 0.3 + 最近一次响应时间 * 0.7 = 当前权重
 */
public interface LoadBalance<T> {
    T choose(List<T> providers);

    // 默认是static
    LoadBalance Default = p -> (p == null || p.isEmpty()) ? null : p.get(0);
}
