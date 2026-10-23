package cn.iocoder.yudao.module.ai.service.billing.pricing;

/**
 * AI 计费策略接口
 *
 * @author 芋道源码
 */
public interface AiPricingStrategy {

    /**
     * 获取策略类型标识
     *
     * @return 策略类型，对应 {@link cn.iocoder.yudao.module.ai.enums.billing.AiPricingStrategyTypeEnum#getType()}
     */
    String getType();

    /**
     * 计算调用费用（微元）
     *
     * @param ctx 计费上下文
     * @return 费用（微元）
     */
    long calculateCost(AiPricingContext ctx);

}
