package cn.iocoder.yudao.framework.desensitize.annotation.constraints;

import cn.iocoder.yudao.framework.desensitize.annotation.SliderDesensitize;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 手机号
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@SliderDesensitize(prefixKeep = 3, suffixKeep = 4, replacer = "*") // 手机号;比如：13248765917脱敏之后为132****5917
public @interface PhoneNumberDesensitize {
}
