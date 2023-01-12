package cn.iocoder.yudao.framework.desensitize.annotation.constraints;

import cn.iocoder.yudao.framework.desensitize.annotation.SliderDesensitize;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 银行卡号
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@SliderDesensitize(prefixKeep = 6, suffixKeep = 2, replacer = "*") // 银行卡号;比如：9988002866797031脱敏之后为998800********31
public @interface BankCardDesensitize {
}
