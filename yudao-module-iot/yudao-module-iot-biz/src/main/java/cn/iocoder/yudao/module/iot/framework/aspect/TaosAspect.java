package cn.iocoder.yudao.module.iot.framework.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Map;

// TODO @haohao：这个还需要的么？
/**
 * TaosAspect 是一个处理 Taos 数据库返回值的切面。
 */
@Aspect
@Component
@Slf4j
public class TaosAspect {

    @Around("execution(java.util.Map<String,Object> cn.iocoder.yudao.module.iot.dal.tdengine.*.*(..))")
    public Object handleType(ProceedingJoinPoint joinPoint) {
        Map<String, Object> result = null;
        try {
            result = (Map<String, Object>) joinPoint.proceed();
            result.replaceAll((key, value) -> {
                if (value instanceof byte[]) {
                    return new String((byte[]) value);
                } else if (value instanceof Timestamp) {
                    return ((Timestamp) value).getTime();
                }
                return value;
            });
        } catch (Throwable e) {
            log.error("TaosAspect handleType error", e);
        }
        return result;
    }
}
