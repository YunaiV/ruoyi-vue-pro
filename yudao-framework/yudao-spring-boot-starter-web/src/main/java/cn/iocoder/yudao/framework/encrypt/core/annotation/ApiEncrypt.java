package cn.iocoder.yudao.framework.encrypt.core.annotation;

import java.lang.annotation.*;

/**
 * HTTP API 加解密注解
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEncrypt {

    /**
     * 是否对请求参数进行解密，默认 true
     */
    boolean request() default true;

    /**
     * 是否对响应结果进行加密，默认 true
     */
    boolean response() default true;

}
