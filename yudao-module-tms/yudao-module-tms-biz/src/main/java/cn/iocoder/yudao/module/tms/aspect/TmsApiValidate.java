package cn.iocoder.yudao.module.tms.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: wdy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TmsApiValidate {
    /**
     * 分组校验
     *
     * @return void
     * <p>
     * {@link cn.iocoder.yudao.module.system.api.utils.Validation}
     */
    Class<?>[] groups() default {};

    /**
     * 前缀
     *
     * @return str
     */
    String prefix() default "";
}