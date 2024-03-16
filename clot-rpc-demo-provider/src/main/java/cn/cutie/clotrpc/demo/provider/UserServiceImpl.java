package cn.cutie.clotrpc.demo.provider;

import cn.cutie.clotrpc.core.annotation.ClotProvider;
import cn.cutie.clotrpc.demo.api.User;
import cn.cutie.clotrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ClotProvider
public class UserServiceImpl implements UserService {

    @Autowired
    Environment environment;

    @Override
    public User findById(int id) {
        return new User(id, "Clot-" + environment.getProperty("server.port") + "_" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "Clot-" + name + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(Float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return "Clot";
    }

    @Override
    public String getName(int id) {
        return "Clot-" + id;
    }

    @Override
    public int[] getIds() {
        return new int[]{1, 2, 3};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{11,22,33};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }
}
