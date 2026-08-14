package cn.iocoder.yudao.module.hrm.enums.employee.employment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * HRM 员工异动原因枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeChangeReasonEnum implements ArrayValuable<Integer> {

    ORGANIZATION_ADJUSTMENT(1, "组织架构调整"),
    PERSONAL_APPLICATION(2, "个人申请"),
    WORK_ARRANGEMENT(3, "工作安排"),
    VIOLATION(4, "违规违纪"),
    UNDERPERFORMANCE(5, "绩效不达标"),
    HEALTH_REASON(6, "个人身体原因"),
    UNSUITABLE_POSITION(7, "不适应当前岗位");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeChangeReasonEnum::getReason).toArray(Integer[]::new);

    /**
     * 调岗、晋升原因
     */
    public static final Set<Integer> TRANSFER_AND_PROMOTION_REASONS = SetUtils.asSet(
            ORGANIZATION_ADJUSTMENT.getReason(), PERSONAL_APPLICATION.getReason(), WORK_ARRANGEMENT.getReason());

    /**
     * 降级原因
     */
    public static final Set<Integer> DEMOTION_REASONS = SetUtils.asSet(
            VIOLATION.getReason(), UNDERPERFORMANCE.getReason(), HEALTH_REASON.getReason(),
            UNSUITABLE_POSITION.getReason());

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

    public static HrmEmployeeChangeReasonEnum valueOf(Integer reason) {
        return ArrayUtil.firstMatch(item -> item.getReason().equals(reason), values());
    }

}
