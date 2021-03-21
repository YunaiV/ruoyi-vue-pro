package cn.iocoder.dashboard.framework.tracer.annotation;

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

    /**
     * 交易流水tag名
     */
    String BIZ_ID_TAG = "bizId";
    /**
     * 交易类型tag名
     */
    String BIZ_TYPE_TAG = "bizType";

    String bizId();

    String bizType();

}
