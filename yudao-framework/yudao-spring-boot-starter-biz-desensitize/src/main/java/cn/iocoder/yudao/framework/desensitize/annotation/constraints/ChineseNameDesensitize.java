package cn.iocoder.yudao.framework.desensitize.annotation.constraints;

import cn.iocoder.yudao.framework.desensitize.annotation.SliderDesensitize;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 中文名
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@SliderDesensitize(prefixKeep = 1, suffixKeep = 0, replacer = "*") // 中文名;比如：刘子豪脱敏之后为刘**
public @interface ChineseNameDesensitize {
}
