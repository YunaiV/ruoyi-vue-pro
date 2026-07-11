package cn.iocoder.yudao.module.crm.enums.performance;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 业绩目标对象类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum CrmPerformanceConfigObjectTypeEnum implements ArrayValuable<Integer> {

    DEPT(2, "部门"),
    USER(3, "员工");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(CrmPerformanceConfigObjectTypeEnum::getObjectType).toArray(Integer[]::new);

    /**
     * 对象类型
     */
    private final Integer objectType;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
