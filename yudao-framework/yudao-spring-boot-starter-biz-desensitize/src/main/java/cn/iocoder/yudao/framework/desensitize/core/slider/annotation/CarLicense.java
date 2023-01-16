package cn.iocoder.yudao.framework.desensitize.core.slider.annotation;

import cn.iocoder.yudao.framework.desensitize.core.base.annotation.Desensitize;
import cn.iocoder.yudao.framework.desensitize.core.slider.handler.CarLicenseDesensitization;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 车牌号
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@Desensitize(desensitizationBy = CarLicenseDesensitization.class) // 车牌号;比如：粤A66666脱敏之后为粤A6***6
public @interface CarLicense {

    /**
     * 前缀保留长度
     */
    int prefixKeep() default 3;

    /**
     * 后缀保留长度
     */
    int suffixKeep() default 1;

    /**
     * 替换规则，会将前缀后缀保留后，全部替换成 replacer
     * 例如：prefixKeep = 1; suffixKeep = 2; replacer = "*";
     * 原始字符串  123456
     * 脱敏后     1***56
     */
    String replacer() default "*";

}
