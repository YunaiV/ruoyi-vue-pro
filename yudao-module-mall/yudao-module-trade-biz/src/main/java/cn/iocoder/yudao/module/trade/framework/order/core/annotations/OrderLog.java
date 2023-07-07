package cn.iocoder.yudao.module.trade.framework.order.core.annotations;

import cn.iocoder.yudao.module.trade.enums.aftersale.OrderOperateTypeEnum;

import java.lang.annotation.*;

/**
 * 订单日志AOP注解
 *
 * @author 陈賝
 * @since 2023/7/6 15:37
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrderLog {

    /**
     * 日志内容
     */
    String content();

    /**
     * 订单编号
     */
    String id();

    /**
     * 操作类型
     */
    OrderOperateTypeEnum operateType();

}
