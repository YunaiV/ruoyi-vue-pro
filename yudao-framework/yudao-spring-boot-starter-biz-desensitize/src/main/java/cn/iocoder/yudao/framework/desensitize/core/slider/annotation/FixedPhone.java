package cn.iocoder.yudao.framework.desensitize.core.slider.annotation;

import cn.iocoder.yudao.framework.desensitize.core.base.annotation.Desensitize;
import cn.iocoder.yudao.framework.desensitize.core.slider.handler.FixedPhoneDesensitization;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 固定电话
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Desensitize(desensitizationBy = FixedPhoneDesensitization.class) // 固定电话;比如：01086551122脱敏之后为0108*****22
public @interface FixedPhone {

    /**
     * 前缀保留长度
     */
    int prefixKeep() default 4;

    /**
     * 后缀保留长度
     */
    int suffixKeep() default 2;

    /**
     * 替换规则，会将前缀后缀保留后，全部替换成 replacer
     * 例如：prefixKeep = 1; suffixKeep = 2; replacer = "*";
     * 原始字符串  123456
     * 脱敏后     1***56
     */
    String replacer() default "*";

}
