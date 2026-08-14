package cn.iocoder.yudao.module.hrm.enums.employee.employment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工合同类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeContractTypeEnum implements ArrayValuable<Integer> {

    FIXED_TERM_LABOR_CONTRACT(1, "固定期限劳动合同"),
    NON_FIXED_TERM_LABOR_CONTRACT(2, "无固定期限劳动合同"),
    WORK_TASK_LABOR_CONTRACT(3, "以完成一定工作任务为期限的劳动合同"),
    INTERNSHIP_AGREEMENT(4, "实习协议"),
    LABOR_SERVICE_CONTRACT(5, "劳务合同"),
    REEMPLOYMENT_AGREEMENT(6, "返聘协议"),
    LABOR_DISPATCH_CONTRACT(7, "劳务派遣合同"),
    SECONDMENT_CONTRACT(8, "借调合同"),
    OTHER(9, "其他");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeContractTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmEmployeeContractTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
