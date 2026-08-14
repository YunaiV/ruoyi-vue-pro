package cn.iocoder.yudao.module.fms.enums.ledger;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 账簿余额方向模式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsLedgerBalanceModeEnum implements ArrayValuable<Integer> {

    SAME_AS_SUBJECT(1, "与科目方向相同"),
    OPPOSITE_TO_SUBJECT(2, "与科目方向相反");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsLedgerBalanceModeEnum::getMode).toArray(Integer[]::new);

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static FmsLedgerBalanceModeEnum valueOf(Integer mode) {
        return ArrayUtil.firstMatch(item -> item.getMode().equals(mode), values());
    }

}
