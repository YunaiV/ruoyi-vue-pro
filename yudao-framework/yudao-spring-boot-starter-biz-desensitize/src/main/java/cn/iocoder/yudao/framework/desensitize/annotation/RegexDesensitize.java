package cn.iocoder.yudao.framework.desensitize.annotation;

import cn.iocoder.yudao.framework.desensitize.handler.RegexDesensitizationHandler;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 正则脱敏注解
 */
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Desensitize(desensitizationHandler = RegexDesensitizationHandler.class)
public @interface RegexDesensitize {
    /**
     * 匹配的正则表达式（默认匹配所有）
     */
    String regex() default "^[\\s\\S]*$";

    /**
     * 替换规则，会将匹配到的字符串全部替换成 replacer
     * 例如：regex=123; replacer=******
     * 原始字符串 123456789
     * 脱敏后字符串 ******456789
     */
    String replacer() default "******";
}
