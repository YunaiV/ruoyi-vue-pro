package cn.iocoder.yudao.framework.tenant.core.aop;

import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

/**
 * 忽略多租户的 Aspect，基于 {@link TenantIgnore} 注解实现，用于一些全局的逻辑。
 * 例如说，一个定时任务，读取所有数据，进行处理。
 * 又例如说，读取所有数据，进行缓存。
 *
 * 整体逻辑的实现，和 {@link TenantUtils#executeIgnore(Runnable)} 需要保持一致
 *
 * @author 芋道源码
 */
@Aspect
@Slf4j
public class TenantIgnoreAspect {

    @Around("@within(cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore)"
            + " || @annotation(cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        TenantIgnore tenantIgnore = getTenantIgnoreAnnotation(joinPoint);
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            // 计算条件，满足的情况下，才进行忽略
            Object enable = SpringExpressionUtils.parseExpression(tenantIgnore.enable());
            if (Boolean.TRUE.equals(enable)) {
                TenantContextHolder.setIgnore(true);
            }

            // 执行逻辑
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 获得忽略租户注解，方法级配置优先于类级配置
     */
    static TenantIgnore getTenantIgnoreAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        TenantIgnore annotation = AnnotatedElementUtils.findMergedAnnotation(targetMethod, TenantIgnore.class);
        return annotation != null ? annotation
                : AnnotatedElementUtils.findMergedAnnotation(targetClass, TenantIgnore.class);
    }

}
