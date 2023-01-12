package cn.iocoder.yudao.framework.desensitize.annotation.constraints;

import cn.iocoder.yudao.framework.desensitize.annotation.RegexDesensitize;
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
@RegexDesensitize(regex = "(^.)[^@]*(@.*$)", replacer ="$1****$2")  // 邮箱;比如：example@gmail.com脱敏之后为e****@gmail.com
public @interface EmailDesensitize {
}
