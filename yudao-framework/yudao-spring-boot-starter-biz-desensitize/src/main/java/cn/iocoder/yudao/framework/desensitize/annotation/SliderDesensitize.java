package cn.iocoder.yudao.framework.desensitize.annotation;

import cn.iocoder.yudao.framework.desensitize.handler.SliderDesensitizationHandler;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 滑动脱敏注解
 */
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Desensitize(desensitizationHandler = SliderDesensitizationHandler.class)
public @interface SliderDesensitize {

    /**
     * 后缀保留长度
     */
    int suffixKeep() default 0;

    /**
     * 替换规则，会将前缀后缀保留后，全部替换成 replacer
     * 例如：prefixKeep = 1; suffixKeep = 2; replacer = "*";
     * 原始字符串  123456
     * 脱敏后     1***56
     */
    String replacer() default "*";

    /**
     * 前缀保留长度
     */
    int prefixKeep() default 0;
}
