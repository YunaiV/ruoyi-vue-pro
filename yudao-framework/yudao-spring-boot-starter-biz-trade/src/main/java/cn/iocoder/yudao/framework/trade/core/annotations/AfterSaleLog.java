package cn.iocoder.yudao.framework.trade.core.annotations;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 售后日志
 *
 * @author 陈賝
 * @date 2023/6/8 17:04
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterSaleLog {

    /**
     * 售后ID
     */
    String id();

    /**
     * 操作类型
     */
    String operateType() default "";

    /**
     * 日志内容
     */
    String content() default "";

}
