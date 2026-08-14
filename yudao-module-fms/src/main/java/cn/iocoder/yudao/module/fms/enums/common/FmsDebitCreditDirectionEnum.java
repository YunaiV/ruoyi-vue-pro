package cn.iocoder.yudao.module.fms.enums.common;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 方向枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsDebitCreditDirectionEnum implements ArrayValuable<Integer> {

    DEBIT(1, "借"),
    CREDIT(2, "贷");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsDebitCreditDirectionEnum::getType).toArray(Integer[]::new);

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

    public static FmsDebitCreditDirectionEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    public static FmsDebitCreditDirectionEnum valueOfName(String name) {
        return ArrayUtil.firstMatch(item -> item.getName().equals(name), values());
    }

}
