package cn.iocoder.yudao.module.tms.aspect;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author: wdy
 */
@Aspect
@Component
public class TmsApiValidateAspect {
    @Autowired
    private Validator validator;

    @Around("@annotation(tmsApiValidate)")
    public Object around(ProceedingJoinPoint joinPoint, TmsApiValidate tmsApiValidate) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            Set<ConstraintViolation<Object>> violations = validator.validate(arg, tmsApiValidate.groups());
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
        return joinPoint.proceed();
    }
}
