package cn.iocoder.yudao.module.ai.service.billing.pricing;

import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import lombok.Builder;
import lombok.Data;

/**
 * AI 计费上下文
 *
 * @author 芋道源码
 */
@Data
@Builder
public class AiPricingContext {

    /**
     * 输入 token 总量（含缓存命中部分），可为 null
     */
    private Integer promptTokens;
    /**
     * 输出 token 总量（含推理部分），可为 null
     */
    private Integer completionTokens;
    /**
     * 缓存命中 token 数，可为 null
     */
    private Integer cachedTokens;
    /**
     * 推理/思考 token 数，可为 null
     */
    private Integer reasoningTokens;
    /**
     * 计费配置
     */
    private AiModelPricingDO pricing;
    /**
     * 平台标识，供自定义策略判断
     */
    private String platform;
    /**
     * 模型标识，供自定义策略判断
     */
    private String model;

}
