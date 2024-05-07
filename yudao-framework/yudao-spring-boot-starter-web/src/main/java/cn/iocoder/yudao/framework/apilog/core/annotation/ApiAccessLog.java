package cn.iocoder.yudao.framework.apilog.core.annotation;

import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 访问日志注解
 *
 * @author 芋道源码
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiAccessLog {

    // ========== 开关字段 ==========

    /**
     * 是否记录访问日志
     */
    boolean enable() default true;
    /**
     * 是否记录请求参数
     *
     * 默认记录，主要考虑请求数据一般不大。可手动设置为 false 进行关闭
     */
    boolean requestEnable() default true;
    /**
     * 是否记录响应结果
     *
     * 默认不记录，主要考虑响应数据可能比较大。可手动设置为 true 进行打开
     */
    boolean responseEnable() default false;
    /**
     * 敏感参数数组
     *
     * 添加后，请求参数、响应结果不会记录该参数
     */
    String[] sanitizeKeys() default {};

    // ========== 模块字段 ==========

    /**
     * 操作模块
     *
     * 为空时，会尝试读取 {@link io.swagger.v3.oas.annotations.tags.Tag#name()} 属性
     */
    String operateModule() default "";
    /**
     * 操作名
     *
     * 为空时，会尝试读取 {@link io.swagger.v3.oas.annotations.Operation#summary()} 属性
     */
    String operateName() default "";
    /**
     * 操作分类
     *
     * 实际并不是数组，因为枚举不能设置 null 作为默认值
     */
    OperateTypeEnum[] operateType() default {};

}
