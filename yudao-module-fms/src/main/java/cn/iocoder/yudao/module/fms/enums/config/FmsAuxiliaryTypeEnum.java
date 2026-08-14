package cn.iocoder.yudao.module.fms.enums.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 辅助核算类别枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsAuxiliaryTypeEnum implements ArrayValuable<Integer> {

    CUSTOMER(1, "客户"),
    SUPPLIER(2, "供应商"),
    EMPLOYEE(3, "职员"),
    PROJECT(4, "项目"),
    DEPARTMENT(5, "部门"),
    INVENTORY(6, "存货"),
    CUSTOM(7, "自定义");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsAuxiliaryTypeEnum::getType).toArray(Integer[]::new);

    public static final FmsAuxiliaryTypeEnum[] SYSTEM_PRESET_TYPES = Arrays.stream(values())
            .filter(type -> type != CUSTOM).toArray(FmsAuxiliaryTypeEnum[]::new);

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

    public static FmsAuxiliaryTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
