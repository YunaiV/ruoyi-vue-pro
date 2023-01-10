package cn.iocoder.yudao.framework.desensitize.annotation;

import cn.iocoder.yudao.framework.desensitize.enums.DesensitizationStrategyEnum;
import cn.iocoder.yudao.framework.desensitize.handler.DesensitizationHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Desensitize 注解配置会覆盖 DesensitizationStrategyEnum 配置
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desensitize {

    /**
     * 脱敏策略
     */
    DesensitizationStrategyEnum strategy();

    /**
     * 脱敏替换字符
     */
    String replacer();

    /**
     * 正则表达式
     */
    String regex();

    /**
     * 前缀保留长度
     */
    int preKeep();

    /**
     * 后缀保留长度
     */
    int suffixKeep();

    /**
     * 脱敏处理器
     */
    Class<? extends DesensitizationHandler> handler();
}
