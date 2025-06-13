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
        String prefix = tmsApiValidate.prefix();
        for (Object arg : joinPoint.getArgs()) {
            Set<ConstraintViolation<Object>> violations = validator.validate(arg, tmsApiValidate.groups());
            if (!violations.isEmpty()) {
                // 拼接前缀到所有message
                StringBuilder sb = new StringBuilder();
                for (ConstraintViolation<Object> violation : violations) {
                    if (!prefix.isEmpty()) {
                        sb.append(prefix).append(": ");
                    }
//                    sb.append(violation.getPropertyPath()).append(" ");
                    sb.append(violation.getMessage()).append("; ");
                }
                throw new ConstraintViolationException(sb.toString(), violations);
            }
        }
        return joinPoint.proceed();
    }
}
