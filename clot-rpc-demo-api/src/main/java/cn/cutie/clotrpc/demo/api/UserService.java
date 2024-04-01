package cn.cutie.clotrpc.demo.api;

import java.util.List;
import java.util.Map;

public interface UserService {
    User findById(int id);

    // 方法重载，一般不支持传参相同，返回值不同，只对传参做处理即可
    User findById(int id, String name);

    long getId(long id);

    long getId(User user);

    long getId(Float id);

    String getName();

    String getName(int id);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    List<User> getList(List<User> users);

    Map<String, User> getMap(Map<String, User> map);

    Boolean getFlag(boolean b);

    User[] findUsers(User[] users);

    User findById(long id);

    User ex(boolean flag);

    User find(int timeout);
}
