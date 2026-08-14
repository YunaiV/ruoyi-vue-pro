package cn.iocoder.yudao.module.hrm.enums.employee.info;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工状态页签枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeStatusTabEnum implements ArrayValuable<Integer> {

    REGULAR(HrmEmployeeStatusEnum.REGULAR.getStatus(), "正式"),
    PROBATION(HrmEmployeeStatusEnum.PROBATION.getStatus(), "试用"),
    INTERN(HrmEmployeeStatusEnum.INTERN.getStatus(), "实习"),
    PART_TIME(HrmEmployeeStatusEnum.PART_TIME.getStatus(), "兼职"),
    LABOR(HrmEmployeeStatusEnum.LABOR.getStatus(), "劳务"),
    CONSULTANT(HrmEmployeeStatusEnum.CONSULTANT.getStatus(), "顾问"),
    REHIRE(HrmEmployeeStatusEnum.REHIRE.getStatus(), "返聘"),
    OUTSOURCE(HrmEmployeeStatusEnum.OUTSOURCE.getStatus(), "外包"),
    ACTIVE(11, "在职"),
    FULL_TIME(12, "全职"),
    PENDING_ENTRY(13, "待入职"),
    PENDING_LEAVE(14, "待离职"),
    LEFT(15, "已离职");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeStatusTabEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态页签
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmEmployeeStatusTabEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
