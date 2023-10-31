package cn.iocoder.yudao.module.crm.framework.core.annotations;

import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;

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
    CrmBizTypeEnum bizType();

    /**
     * 数据编号获取来源类，确保数据 id 编号在此类中，不能在父类中。
     * 例：如果在 baseVO 中需要把 id 弄到 updateVO 中。
     */
    Class<?>[] getIdFor() default {};

    /**
     * 操作类型
     */
    CrmPermissionLevelEnum permissionLevel();

}
