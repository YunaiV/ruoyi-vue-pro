package cn.iocoder.yudao.module.hrm.enums.employee.info;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 首页人事概况筛选类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeSurveyTypeEnum implements ArrayValuable<Integer> {

    ENTRY(1, "入职"),
    LEAVE(2, "离职"),
    REGULAR(3, "转正"),
    TRANSFER(4, "调岗"),
    PENDING_ENTRY(5, "待入职"),
    PENDING_LEAVE(6, "待离职");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeSurveyTypeEnum::getType).toArray(Integer[]::new);

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

}
