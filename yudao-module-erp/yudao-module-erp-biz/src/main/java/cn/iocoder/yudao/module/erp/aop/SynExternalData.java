package cn.iocoder.yudao.module.erp.aop;

import java.lang.annotation.*;

/**
 * @author Administrator
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SynExternalData {

    //指定的表名，必填
    String table();
    //入参类名
    Class<?> inClazz() default Object.class;
}
