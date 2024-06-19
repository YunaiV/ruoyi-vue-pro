package cn.iocoder.yudao.framework.signature.core.annotation;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


/**
 * HTTP API 签名注解
 *
 * @author Zhougang
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSignature {

    /**
     * 同一个请求多长时间内有效 默认 60 秒
     */
    int timeout() default 60;

    /**
     * 时间单位，默认为 SECONDS 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    // ========================== 签名参数 ==========================

    /**
     * 提示信息，签名失败的提示
     *
     * @see GlobalErrorCodeConstants#BAD_REQUEST
     */
    String message() default "签名不正确"; // 为空时，使用 BAD_REQUEST 错误提示

    /**
     * 签名字段：appId 应用ID
     */
    String appId() default "appId";

    /**
     * 签名字段：timestamp 时间戳
     */
    String timestamp() default "timestamp";

    /**
     * 签名字段：nonce 随机数，10 位以上
     */
    String nonce() default "nonce";

    /**
     * sign 客户端签名
     */
    String sign() default "sign";

}
