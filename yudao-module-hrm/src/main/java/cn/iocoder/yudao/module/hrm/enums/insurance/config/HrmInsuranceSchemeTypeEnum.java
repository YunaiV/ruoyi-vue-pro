package cn.iocoder.yudao.module.hrm.enums.insurance.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 社保方案类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmInsuranceSchemeTypeEnum implements ArrayValuable<Integer> {

    PROPORTION(1, "比例"),
    AMOUNT(2, "金额");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmInsuranceSchemeTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmInsuranceSchemeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
