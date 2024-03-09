package cn.cutie.clotrpc.demo.provider;

import cn.cutie.clotrpc.core.annotation.ClotProvider;
import cn.cutie.clotrpc.demo.api.Order;
import cn.cutie.clotrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

@Component
@ClotProvider
public class OrderServiceImpl implements OrderService {

    @Override
    public Order findById(Integer id) {
        return new Order(id, 15.6F);
    }
}
