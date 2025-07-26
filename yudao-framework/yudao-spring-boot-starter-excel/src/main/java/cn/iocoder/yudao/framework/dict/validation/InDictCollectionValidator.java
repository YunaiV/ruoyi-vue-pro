package cn.iocoder.yudao.framework.dict.validation;

import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.List;

public class InDictCollectionValidator implements ConstraintValidator<InDict, Collection<?>> {

    private String dictType;

    @Override
    public void initialize(InDict annotation) {
        this.dictType = annotation.type();
    }

    @Override
    public boolean isValid(Collection<?> list, ConstraintValidatorContext context) {
        if (list == null) {
            return true;
        }
        List<String> dbValues = DictFrameworkUtils.getDictDataValueList(dictType);
        boolean match = list.stream().allMatch(v -> dbValues.stream()
                .anyMatch(dbValue -> dbValue.equalsIgnoreCase(v.toString())));
        if (match) {
            return true;
        }
        // 校验不通过，自定义提示语句
        context.disableDefaultConstraintViolation(); // 禁用默认的 message 的值
        context.buildConstraintViolationWithTemplate(
                context.getDefaultConstraintMessageTemplate().replaceAll("\\{value}",dbValues.toString())
        ).addConstraintViolation(); // 重新添加错误提示语句
        return false;
    }

}

