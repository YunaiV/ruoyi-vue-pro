package cn.iocoder.yudao.module.iot.framework.questdb.core.annotation;

import com.baomidou.dynamic.datasource.annotation.DS;

import java.lang.annotation.*;

/**
 * QuestDB 数据源
 *
 * 对应 spring.datasource.dynamic.datasource.questdb 条目
 *
 * @author 芋道源码
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DS("questdb")
public @interface QuestDBDS {
}
