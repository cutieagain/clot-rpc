package cn.cutie.clotrpc.core.annotation;

import cn.cutie.clotrpc.core.config.ConsumerConfig;
import cn.cutie.clotrpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Description: 组合启用Rpc
 * @Author: Cutie
 * @CreateDate: 2024/4/8 19:57
 * @Version: 0.0.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableClotRpc {
}
