package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.enums.image.AiFashionRoutingStrategyEnum;

/**
 * AI 服装设计模型路由服务接口
 *
 * <p>根据阶段类型 + 质量配置 + 路由策略，决定调用哪个 SD WebUI 端点以及使用哪套参数。</p>
 *
 * @author deepay
 */
public interface AiFashionModelRouterService {

    /**
     * 为指定阶段生成路由决策
     *
     * @param stepType         阶段类型（CONCEPT/SKETCH/DETAIL/TEXTURE/POSE/RENDER/UPSAMPLE/THREE_D）
     * @param qualityConfig    当前任务的质量配置
     * @param routingStrategy  路由策略（QUALITY_FIRST/COST_FIRST/SPEED_FIRST/BALANCED）
     * @return 路由决策，包含端点、步数、采样器、ControlNet 等完整参数
     */
    ModelRouteDecision route(String stepType,
                             AiFashionQualityConfig qualityConfig,
                             AiFashionRoutingStrategyEnum routingStrategy);

}
