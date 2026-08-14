package cn.iocoder.yudao.module.fms.enums.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * FMS 预置币别枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsCurrencyPresetEnum implements ArrayValuable<String> {

    RMB("RMB", "人民币", BigDecimal.ONE);

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(FmsCurrencyPresetEnum::getCode).toArray(String[]::new);

    /**
     * 币别编码
     */
    private final String code;
    /**
     * 币别名称
     */
    private final String name;
    /**
     * 汇率
     */
    private final BigDecimal exchangeRate;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static FmsCurrencyPresetEnum valueOfCode(String code) {
        return ArrayUtil.firstMatch(currency -> currency.getCode().equals(code), values());
    }

}
