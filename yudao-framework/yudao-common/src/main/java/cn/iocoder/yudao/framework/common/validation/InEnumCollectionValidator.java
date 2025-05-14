package cn.iocoder.yudao.framework.common.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.core.EmptyArrayValuable;
import cn.iocoder.yudao.framework.common.util.cache.CaffeineDictCache;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InEnumCollectionValidator implements ConstraintValidator<InEnum, Collection<?>> {

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
    public boolean isValid(Collection<?> list, ConstraintValidatorContext context) {
        if (list == null) {
            return true;
        }

        if (StringUtils.isNotEmpty(dictType)) {
            List<String> dbValues = CaffeineDictCache.getDictValues(dictType, sql);
            boolean match = list.stream().allMatch(v -> dbValues.stream()
                    .anyMatch(dbValue -> CaffeineDictCache.compareValue(dbValue, v)));
            if (match) {
                return true;
            }
            // 校验不通过，自定义提示语句
            context.disableDefaultConstraintViolation(); // 禁用默认的 message 的值
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate().replaceAll("\\{value}",
                            dbValues.toString()))
                    .addConstraintViolation(); // 重新添加错误提示语句
        } else {
            // 校验通过
            if (CollUtil.containsAll(values, list)) {
                return true;
            }
            // 校验不通过，自定义提示语句
            context.disableDefaultConstraintViolation(); // 禁用默认的 message 的值
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate().replaceAll("\\{value}",
                            CollUtil.join(values, ",")))
                    .addConstraintViolation(); // 重新添加错误提示语句
        }

        return false;
    }
}

