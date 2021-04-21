package cn.iocoder.dashboard.framework.tracer.core.annotation;

import java.lang.annotation.*;

/**
 * 打印业务编号 / 业务类型注解
 *
 * @author 麻薯
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface BizTracing {

    /**
     * 业务编号 tag 名
     */
    String ID_TAG = "biz.id";
    /**
     * 业务类型 tag 名
     */
    String TYPE_TAG = "biz.type";

    /**
     * @return 业务编号
     */
    String id();

    /**
     * @return 业务类型
     */
    String type();

}
