package cn.iocoder.yudao.framework.common.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.core.EmptyArrayValuable;
import cn.iocoder.yudao.framework.common.util.cache.CaffeineDictCache;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InEnumValidator implements ConstraintValidator<InEnum, Object> {

    private List<?> values;
    private String dictType;
    private String sql;

    @Override
    public void initialize(InEnum annotation) {
        this.dictType = annotation.dictType();
        this.sql = annotation.dictSql();
        Boolean hasValue =
                annotation.value() != null && annotation.value() != EmptyArrayValuable.class;
        Boolean hasDictType = StringUtils.isNotEmpty(dictType);

        if (hasValue && hasDictType) {
            throw new IllegalArgumentException("@InEnum注解不能同时指定value和dictType");
        }

        if (!hasDictType) {
            ArrayValuable<?>[] values = annotation.value().getEnumConstants();
            this.values =
                    values.length == 0 ? Collections.emptyList() : Arrays.asList(values[0].array());
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 为空时，默认不校验，即认为通过
        if (value == null) {
            return true;
        }

        if (StringUtils.isNotEmpty(dictType)) {
            List<String> dbValues = CaffeineDictCache.getDictValues(dictType, sql);
            boolean match =
                    dbValues.stream().anyMatch(v -> CaffeineDictCache.compareValue(v, value));
            if (match) {
                return true;
            }

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate().replaceAll("\\{value}",
                            dbValues.toString()))
                    .addConstraintViolation();
        } else {
            // 校验通过
            if (values.contains(value)) {
                return true;
            }
            // 校验不通过，自定义提示语句
            context.disableDefaultConstraintViolation(); // 禁用默认的 message 的值
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate().replaceAll("\\{value}",
                            values.toString()))
                    .addConstraintViolation(); // 重新添加错误提示语句
        }
        return false;
    }
}

