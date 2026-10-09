package cn.iocoder.yudao.module.ai.enums.billing;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 预算周期枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiBudgetPeriodTypeEnum implements ArrayValuable<String> {

    MONTHLY("MONTHLY", "月度"),
    DAILY("DAILY", "日度");

    /**
     * 周期类型值
     */
    private final String type;
    /**
     * 周期类型名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiBudgetPeriodTypeEnum::getType).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
