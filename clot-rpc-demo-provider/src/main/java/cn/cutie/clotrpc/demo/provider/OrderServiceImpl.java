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
        if (id == 404){
            throw new RuntimeException("404 exception");
        }
        return new Order(id, 15.6F);
    }
}
