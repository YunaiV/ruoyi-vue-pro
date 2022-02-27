package cn.iocoder.yudao.framework.tenant.core.aop;

import java.lang.annotation.*;

/**
 * 忽略租户，标记指定方法不进行租户的自动过滤
 *
 * 注意，只有 DB 的场景会过滤，其它场景暂时不过滤：
 * 1、Redis 场景：因为是基于 Key 实现多租户的能力，所以忽略没有意义，不像 DB 是一个 column 实现的
 * 2、MQ 场景：有点难以抉择，目前可以通过 Consumer 手动在消费的方法上，添加 @TenantIgnore 进行忽略
 *
 * @author 芋道源码
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TenantIgnore {
}
