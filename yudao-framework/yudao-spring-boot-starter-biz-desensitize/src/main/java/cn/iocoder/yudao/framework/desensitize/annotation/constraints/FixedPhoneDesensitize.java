package cn.iocoder.yudao.framework.desensitize.annotation.constraints;

import cn.iocoder.yudao.framework.desensitize.annotation.SliderDesensitize;
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
@SliderDesensitize(prefixKeep = 4, suffixKeep = 2, replacer = "*") // 固定电话;比如：01086551122脱敏之后为0108*****22
public @interface FixedPhoneDesensitize {
}
