package cn.iocoder.yudao.module.hrm.enums.employee.info;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * HRM 员工状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeStatusEnum implements ArrayValuable<Integer> {

    REGULAR(1, HrmEmployeeTypeEnum.FORMAL.getType(), "正式"),
    PROBATION(2, HrmEmployeeTypeEnum.FORMAL.getType(), "试用"),
    INTERN(3, HrmEmployeeTypeEnum.INFORMAL.getType(), "实习"),
    PART_TIME(4, HrmEmployeeTypeEnum.INFORMAL.getType(), "兼职"),
    LABOR(5, HrmEmployeeTypeEnum.INFORMAL.getType(), "劳务"),
    CONSULTANT(6, HrmEmployeeTypeEnum.INFORMAL.getType(), "顾问"),
    REHIRE(7, HrmEmployeeTypeEnum.INFORMAL.getType(), "返聘"),
    OUTSOURCE(8, HrmEmployeeTypeEnum.INFORMAL.getType(), "外包");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 正式员工状态集合
     */
    public static final Set<Integer> FULL_TIME_STATUSES = SetUtils.asSet(
            REGULAR.getStatus(), PROBATION.getStatus());

    /**
     * 非正式员工状态集合
     */
    public static final Set<Integer> INFORMAL_STATUSES = SetUtils.asSet(
            INTERN.getStatus(), PART_TIME.getStatus(), LABOR.getStatus(),
            CONSULTANT.getStatus(), REHIRE.getStatus(), OUTSOURCE.getStatus());

    /**
     * 可转为全职的员工状态集合
     */
    public static final Set<Integer> CONVERTIBLE_TO_FULL_TIME_STATUSES = SetUtils.asSet(
            INTERN.getStatus(), PART_TIME.getStatus());

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 聘用形式
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

    public static HrmEmployeeStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
