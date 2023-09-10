package cn.iocoder.yudao.framework.mybatis.core.enums;

import java.lang.annotation.*;

/**
 * 忽略租户，标记指定实体类不进行租户校验
 *
 * @author lemoncc
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgnoreTenantDB {
}