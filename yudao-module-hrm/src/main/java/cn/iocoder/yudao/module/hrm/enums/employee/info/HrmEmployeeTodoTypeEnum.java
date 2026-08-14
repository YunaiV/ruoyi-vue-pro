package cn.iocoder.yudao.module.hrm.enums.employee.info;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工待办筛选类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeTodoTypeEnum implements ArrayValuable<Integer> {

    PENDING_LEAVE(2), // 待离职
    CONTRACT_EXPIRE(3), // 合同到期
    REGULAR(4), // 待转正
    PENDING_ENTRY(5), // 待入职
    BIRTHDAY(6); // 员工生日

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeTodoTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
