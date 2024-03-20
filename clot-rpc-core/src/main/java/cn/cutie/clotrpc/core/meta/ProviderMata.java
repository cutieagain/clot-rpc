package cn.cutie.clotrpc.core.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 描述provider映射关系
 */
@Data
@Builder
public class ProviderMata {
    Method method;
    String methodSign;
    Object serviceImpl;
}
