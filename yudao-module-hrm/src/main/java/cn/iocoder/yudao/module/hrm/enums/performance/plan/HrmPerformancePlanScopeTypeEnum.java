package cn.iocoder.yudao.module.hrm.enums.performance.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效计划考评范围类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformancePlanScopeTypeEnum implements ArrayValuable<Integer> {

    EMPLOYEE_DEPT(1, "员工部门"),
    EMPLOYMENT(2, "聘用形式"),
    EXCLUDED_EMPLOYEE(3, "排除员工");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformancePlanScopeTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmPerformancePlanScopeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
