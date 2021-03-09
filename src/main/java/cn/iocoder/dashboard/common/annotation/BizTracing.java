package cn.iocoder.dashboard.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited

/**
 * 打印业务流水号/业务类型注解
 *
 * @author 麻薯
 */
public @interface BizTracing {

    String BIZ_ID = "bizId";
    String BIZ_TYPE = "bizType";

    String bizId() default "NULL_ID";

    String bizType() default "NULL_TYPE";

}
