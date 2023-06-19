package cn.iocoder.yudao.module.trade.framework.aftersalelog.core.annotations;

import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.enums.AfterSaleStatusEnum;

import java.lang.annotation.*;

/**
 * 售后日志
 *
 * @author 陈賝
 * @since 2023/6/8 17:04
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterSaleLog {

    /**
     * 售后 ID
     */
    String id();

    /**
     * 操作类型
     */
    AfterSaleStatusEnum operateType() default AfterSaleStatusEnum.APPLY;

    /**
     * 日志内容
     */
    String content() default "";

}
