package cn.cutie.clotrpc.core.api;

import java.util.List;

/**
 * 路由
 * @param <T>
 */
public interface Router<T> {
    List<T> route(List<T> providers);

    Router Default = p -> p;
}
