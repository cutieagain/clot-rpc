package cn.cutie.clotrpc.core.cluster;

import cn.cutie.clotrpc.core.api.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer<T> implements LoadBalance<T> {

    Random random = new Random();

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return providers.get(random.nextInt(providers.size()));
    }
}
