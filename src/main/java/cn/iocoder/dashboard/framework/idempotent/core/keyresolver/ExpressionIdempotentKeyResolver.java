package cn.iocoder.dashboard.framework.idempotent.core.keyresolver;

import cn.iocoder.dashboard.framework.idempotent.core.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;

/**
 * 基于 Spring EL 表达式，
 *
 * @author 芋道源码
 */
public class ExpressionIdempotentKeyResolver implements IdempotentKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        // TODO 稍后实现
        return null;
    }

}
