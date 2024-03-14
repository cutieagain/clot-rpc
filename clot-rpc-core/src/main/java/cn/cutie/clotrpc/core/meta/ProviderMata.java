package cn.cutie.clotrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class ProviderMata {
    Method method;
    String methodSign;
    Object serviceImpl;
}
