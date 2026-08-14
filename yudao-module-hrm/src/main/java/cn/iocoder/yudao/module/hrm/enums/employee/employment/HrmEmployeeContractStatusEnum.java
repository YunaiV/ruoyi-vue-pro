package cn.iocoder.yudao.module.hrm.enums.employee.employment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工合同状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeContractStatusEnum implements ArrayValuable<Integer> {

    NOT_PERFORMED(0, "未执行"),
    IN_PROGRESS(1, "执行中"),
    EXPIRED(2, "已到期");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeContractStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
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

    public static HrmEmployeeContractStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
