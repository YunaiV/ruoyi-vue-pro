package cn.iocoder.yudao.module.crm.framework.core.annotations;

import cn.iocoder.yudao.module.crm.framework.enums.CrmEnum;
import cn.iocoder.yudao.module.crm.framework.enums.OperationTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Crm 数据操作权限校验 AOP 注解
 *
 * @author HUIHUI
 */
@Target({METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrmPermission {

    /**
     * crm 类型
     */
    CrmEnum crmType();

    /**
     * 操作类型
     */
    OperationTypeEnum operationType();

}
