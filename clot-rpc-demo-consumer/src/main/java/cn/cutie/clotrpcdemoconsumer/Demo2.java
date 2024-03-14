package cn.cutie.clotrpcdemoconsumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import cn.cutie.clotrpc.demo.api.User;
import cn.cutie.clotrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
public class Demo2 {
    @ClotConsumer
    UserService userService2;

    public void test() {
        User user = userService2.findById(100);
        System.out.println(user);
    }
}
