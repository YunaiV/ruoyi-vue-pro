package cn.iocoder.yudao.module.hrm.enums.employee.employment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工离职原因枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeQuitReasonEnum implements ArrayValuable<Integer> {

    FAMILY(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 1, "家庭原因"),
    HEALTH(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 2, "身体原因"),
    SALARY(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 3, "薪资原因"),
    INCONVENIENT_TRAFFIC(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 4, "交通不便"),
    WORK_PRESSURE(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 5, "工作压力"),
    MANAGEMENT_ISSUE(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 6, "管理问题"),
    NO_PROMOTION_OPPORTUNITY(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 7, "无晋升机会"),
    CAREER_PLANNING(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 8, "职业规划"),
    GIVE_UP_RENEWAL(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 9, "合同到期放弃续签"),
    OTHER_PERSONAL_REASON(HrmEmployeeQuitTypeEnum.VOLUNTARY.getType(), 10, "其他个人原因"),
    TRIAL_PERIOD_DISMISSAL(HrmEmployeeQuitTypeEnum.INVOLUNTARY.getType(), 11, "试用期内辞退"),
    VIOLATION(HrmEmployeeQuitTypeEnum.INVOLUNTARY.getType(), 12, "违反公司条例"),
    LAYOFF(HrmEmployeeQuitTypeEnum.INVOLUNTARY.getType(), 13, "组织调整/裁员"),
    UNDERPERFORMANCE(HrmEmployeeQuitTypeEnum.INVOLUNTARY.getType(), 14, "绩效不达标辞退"),
    CONTRACT_NOT_RENEWED(HrmEmployeeQuitTypeEnum.INVOLUNTARY.getType(), 15, "合同到期不续签"),
    OTHER_INVOLUNTARY_REASON(HrmEmployeeQuitTypeEnum.INVOLUNTARY.getType(), 16, "其他原因");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeQuitReasonEnum::getReason).toArray(Integer[]::new);

    /**
     * 离职类型
     *
     * 枚举 {@link HrmEmployeeQuitTypeEnum}
     */
    private final Integer quitType;
    /**
     * 原因
     */
    private final Integer reason;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmEmployeeQuitReasonEnum valueOf(Integer reason) {
        return ArrayUtil.firstMatch(item -> item.getReason().equals(reason), values());
    }

}
