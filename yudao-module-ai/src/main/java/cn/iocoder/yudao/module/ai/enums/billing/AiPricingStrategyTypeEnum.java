package cn.iocoder.yudao.module.ai.enums.billing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI 计费策略类型枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum AiPricingStrategyTypeEnum {

    DEFAULT("DEFAULT", "默认四档计费");

    /**
     * 策略类型标识
     */
    private final String type;
    /**
     * 策略名称
     */
    private final String name;

}
