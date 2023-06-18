package cn.iocoder.yudao.framework.trade.core.annotations;

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

    // TODO @陈賝：是不是改成一个操作的枚举？
    /**
     * 操作类型
     */
    String operateType() default "";

    /**
     * 日志内容
     */
    String content() default "";

}
