package cn.iocoder.yudao.framework.signature.core.annotation;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;

import java.lang.annotation.*;


/**
 * 签名注解
 *
 * @author Zhougang
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Signature {

    /**
     * 同一个请求多长时间内有效 默认 10分钟
     */
    long expireTime() default 600000L;

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

    /**
     * url 客户端不需要传递，但是可以用来加签(如： /{id} 带有动态参数的 url ，如果没有动态参数可设置为 false 不进行加签)
     */
    boolean urlEnable() default true;
}
