package cn.iocoder.yudao.module.crm.framework.permission.core.annotations;

import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

/**
 * CRM 数据操作权限校验 AOP 注解
 *
 * @author HUIHUI
 */
@Target({METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrmPermission {

    /**
     * CRM 类型
     */
    CrmBizTypeEnum[] bizType() default {};

    /**
     * CRM 类型扩展，通过 Spring EL 表达式获取到 {@link #bizType()}
     *
     * 目的：用于 CrmPermissionController 团队权限校验
     */
    String bizTypeValue() default "";

    /**
     * 数据编号，通过 Spring EL 表达式获取
     */
    String bizId();

    /**
     * 操作所需权限级别
     */
    CrmPermissionLevelEnum level();

}
