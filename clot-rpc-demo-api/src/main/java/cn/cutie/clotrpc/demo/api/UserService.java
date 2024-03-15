package cn.cutie.clotrpc.demo.api;

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
}
