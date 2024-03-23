package cn.cutie.clotrpcdemoconsumer;

import cn.cutie.clotrpc.core.annotation.ClotConsumer;
import cn.cutie.clotrpc.demo.api.User;
import cn.cutie.clotrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Demo2 {
    @ClotConsumer
    UserService userService2;

    public void test() {
        User user = userService2.findById(100);
        log.debug("", user);
    }
}
