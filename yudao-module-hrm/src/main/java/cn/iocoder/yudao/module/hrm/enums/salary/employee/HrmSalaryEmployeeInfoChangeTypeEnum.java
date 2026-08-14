package cn.iocoder.yudao.module.hrm.enums.salary.employee;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工薪资信息变更类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryEmployeeInfoChangeTypeEnum implements ArrayValuable<Integer> {

    UNSET(0, "未定薪"),
    SET(1, "已定薪"),
    ADJUSTED(2, "已调薪");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryEmployeeInfoChangeTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmSalaryEmployeeInfoChangeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
