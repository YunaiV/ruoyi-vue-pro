package cn.iocoder.yudao.framework.security.core.annotations;

import java.lang.annotation.*;

/**
 * 声明用户需要登录
 *
 * 为什么不使用 {@link org.springframework.security.access.prepost.PreAuthorize} 注解，原因是不通过时，抛出的是认证不通过，而不是未登录
 *
 * @author 芋道源码
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PreAuthenticated {
}
