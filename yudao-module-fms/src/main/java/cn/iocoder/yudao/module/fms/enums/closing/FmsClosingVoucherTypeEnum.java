package cn.iocoder.yudao.module.fms.enums.closing;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 结转凭证类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsClosingVoucherTypeEnum implements ArrayValuable<Integer> {

    SEPARATE_GAIN_AND_LOSS(1, "收益和损失分开结转"),
    COMBINED_GAIN_AND_LOSS(2, "收益和损失同时结转");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsClosingVoucherTypeEnum::getType).toArray(Integer[]::new);

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

    public static FmsClosingVoucherTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
