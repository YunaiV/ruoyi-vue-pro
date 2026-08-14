package cn.iocoder.yudao.module.hrm.enums.salary.employee;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 工资表员工异动分类枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryEmployeeChangeTypeEnum implements ArrayValuable<Integer> {

    ALL(0, "计薪人数"),
    ENTRY(1, "新入职"),
    LEAVE(2, "离职"),
    REGULAR(3, "转正"),
    TRANSFER(4, "调岗");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryEmployeeChangeTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmSalaryEmployeeChangeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
