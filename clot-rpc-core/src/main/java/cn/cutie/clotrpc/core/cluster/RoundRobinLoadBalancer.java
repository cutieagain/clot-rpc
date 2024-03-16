package cn.cutie.clotrpc.core.cluster;

import cn.cutie.clotrpc.core.api.LoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer<T> implements LoadBalance<T> {

    AtomicInteger index = new AtomicInteger(0);

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        // index.getAndIncrement() & 0x7ffffff 保证永远是正数
        return providers.get((index.getAndIncrement() & 0x7ffffff) % providers.size());
    }
}
