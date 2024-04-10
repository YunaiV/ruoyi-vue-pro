package cn.iocoder.yudao.framework.idempotent.core.annotation;

import cn.iocoder.yudao.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import cn.iocoder.yudao.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import cn.iocoder.yudao.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import cn.iocoder.yudao.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解
 *
 * @author 芋道源码
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等的超时时间，默认为 1 秒
     *
     * 注意，如果执行时间超过它，请求还是会进来
     */
    int timeout() default 1;
    /**
     * 时间单位，默认为 SECONDS 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息，正在执行中的提示
     */
    String message() default "重复请求，请稍后重试";

    /**
     * 使用的 Key 解析器
     *
     * @see DefaultIdempotentKeyResolver 全局级别
     * @see UserIdempotentKeyResolver 用户级别
     * @see ExpressionIdempotentKeyResolver 自定义表达式，通过 {@link #keyArg()} 计算
     */
    Class<? extends IdempotentKeyResolver> keyResolver() default DefaultIdempotentKeyResolver.class;
    /**
     * 使用的 Key 参数
     */
    String keyArg() default "";

    /**
     * 删除 Key，当发生异常时候
     *
     * 问题：为什么发生异常时，需要删除 Key 呢？
     * 回答：发生异常时，说明业务发生错误，此时需要删除 Key，避免下次请求无法正常执行。
     *
     * 问题：为什么不搞 deleteWhenSuccess 执行成功时，需要删除 Key 呢？
     * 回答：这种情况下，本质上是分布式锁，推荐使用 @Lock4j 注解
     */
    boolean deleteKeyWhenException() default true;

}
