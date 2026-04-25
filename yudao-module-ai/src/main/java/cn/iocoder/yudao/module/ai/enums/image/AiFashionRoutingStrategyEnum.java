package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 服装设计路由策略枚举
 *
 * <p>决定多模型选择时的优先目标：质量优先 / 成本优先 / 速度优先 / 均衡。</p>
 *
 * @author deepay
 */
@AllArgsConstructor
@Getter
public enum AiFashionRoutingStrategyEnum {

    QUALITY_FIRST("QUALITY_FIRST", "质量优先", "最高推理步数，最优采样器，不限时间"),
    COST_FIRST("COST_FIRST", "成本优先", "最少步数，跳过可选阶段，降低 GPU 用量"),
    SPEED_FIRST("SPEED_FIRST", "速度优先", "并行阶段，低步数，快速出图"),
    BALANCED("BALANCED", "均衡", "质量与速度的均衡折中（默认）");

    /** 策略标识 */
    private final String strategy;
    /** 显示名称 */
    private final String name;
    /** 说明 */
    private final String description;

    public static AiFashionRoutingStrategyEnum of(String strategy) {
        if (strategy == null) {
            return BALANCED;
        }
        for (AiFashionRoutingStrategyEnum e : values()) {
            if (e.strategy.equalsIgnoreCase(strategy)) {
                return e;
            }
        }
        return BALANCED;
    }

}
