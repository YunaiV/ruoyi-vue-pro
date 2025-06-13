package cn.iocoder.yudao.module.tms.tool;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * @author: wdy
 */
public class ParamValidatorUtil {

    public static <T> void validate(Validator validator, T obj, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(obj, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
