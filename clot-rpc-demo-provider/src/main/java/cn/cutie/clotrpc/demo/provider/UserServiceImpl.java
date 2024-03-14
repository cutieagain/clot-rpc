package cn.cutie.clotrpc.demo.provider;

import cn.cutie.clotrpc.core.annotation.ClotProvider;
import cn.cutie.clotrpc.demo.api.User;
import cn.cutie.clotrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
@ClotProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        return new User(id, "Clot-" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "Clot-" + name + System.currentTimeMillis());
    }

    @Override
    public int getId(int id) {
        return id;
    }

    @Override
    public String getName() {
        return "Clot";
    }

    @Override
    public String getName(int id) {
        return "Clot-" + id;
    }
}
