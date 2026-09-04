package cn.iocoder.yudao.module.ai.enums.billing;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 预算事件类型枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiBudgetEventTypeEnum implements ArrayValuable<String> {

    THRESHOLD_ALERT("THRESHOLD_ALERT", "阈值告警"),
    OVER_LIMIT_BLOCK("OVER_LIMIT_BLOCK", "超限拦截"),
    MANUAL_ADJUST("MANUAL_ADJUST", "手动调整");

    /**
     * 事件类型值
     */
    private final String type;
    /**
     * 事件类型名
     */
    private final String name;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiBudgetEventTypeEnum::getType).toArray(String[]::new);

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
