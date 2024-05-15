package cn.iocoder.yudao.framework.signature.core.annotation;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.signature.core.service.SignatureApi;
import cn.iocoder.yudao.framework.signature.core.service.impl.DefaultSignatureApiImpl;

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
     * 使用的 签名算法
     *
     * @see DefaultSignatureApiImpl 默认
     */
    Class<? extends SignatureApi> signatureApi() default DefaultSignatureApiImpl.class;

    /**
     * 同一个请求多长时间内有效 默认10分钟
     */
    long expireTime() default 600000L;

    /**
     * 提示信息，签名失败的提示
     *
     * @see GlobalErrorCodeConstants#SIGNATURE_REQUESTS
     */
    String message() default ""; // 为空时，使用 SIGNATURE_REQUEST 错误提示

    /**
     * 签名字段：appId 应用ID
     */
    String appId() default "appId";

    /**
     * 签名字段：appId 时间戳
     */
    String timestamp() default "timestamp";

    /**
     * 签名字段：nonce 随机数，10位以上
     */
    String nonce() default "nonce";

    /**
     * sign 客户端签名
     */
    String sign() default "sign";

    /**
     * url客户端不需要传递，但是可以用来加签(如：/{id}带有动态参数的url，如果没有动态参数可设置为false，不进行加签)
     */
    boolean urlEnable() default true;
}
