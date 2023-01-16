package cn.iocoder.yudao.framework.desensitize.core.regex.annotation;

import cn.iocoder.yudao.framework.desensitize.core.base.annotation.Desensitize;
import cn.iocoder.yudao.framework.desensitize.core.regex.handler.EmailDesensitizationHandler;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 邮箱
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Desensitize(desensitizationBy = EmailDesensitizationHandler.class) // 邮箱;比如：example@gmail.com脱敏之后为e****@gmail.com
public @interface EmailDesensitize {
    /**
     * 匹配的正则表达式（默认匹配所有）
     */
    String regex() default  "(^.)[^@]*(@.*$)";

    /**
     * 替换规则，会将匹配到的字符串全部替换成 replacer
     * 例如：regex=123; replacer=******
     * 原始字符串 123456789
     * 脱敏后字符串 ******456789
     */
    String replacer() default "$1****$2";
}
