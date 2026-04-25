package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.enums.image.AiFashionRoutingStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 服装设计模型路由服务实现
 *
 * <p>根据阶段类型 + 质量配置 + 路由策略计算最终调用参数，
 * 并记录各阶段的实测延迟（指数移动平均）用于未来路由优化。</p>
 *
 * <p>端点映射规则：
 * <ul>
 *   <li>CONCEPT / SKETCH → TXT2IMG</li>
 *   <li>SDXL（v1 alias）→ TXT2IMG</li>
 *   <li>DETAIL / TEXTURE / FABRIC / POSE / RENDER → IMG2IMG</li>
 *   <li>UPSAMPLE / UPSCALE → EXTRAS</li>
 *   <li>THREE_D → PLACEHOLDER（不调用 SD）</li>
 * </ul>
 * </p>
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionModelRouterServiceImpl implements AiFashionModelRouterService {

    private static final String TXT2IMG   = "TXT2IMG";
    private static final String IMG2IMG   = "IMG2IMG";
    private static final String EXTRAS    = "EXTRAS";
    private static final String PLACEHOLDER = "PLACEHOLDER";

    /** 各阶段的实测延迟（毫秒，指数移动平均，初始估算） */
    private final Map<String, AtomicLong> latencyEma = new ConcurrentHashMap<>(Map.of(
            "CONCEPT",  new AtomicLong(8_000),
            "SKETCH",   new AtomicLong(12_000),
            "DETAIL",   new AtomicLong(18_000),
            "TEXTURE",  new AtomicLong(15_000),
            "POSE",     new AtomicLong(15_000),
            "RENDER",   new AtomicLong(25_000),
            "UPSAMPLE", new AtomicLong(5_000),
            "THREE_D",  new AtomicLong(1_000)
    ));

    // ===== 端点映射 =====

    private static final Map<String, String> ENDPOINT_MAP = Map.ofEntries(
            Map.entry("CONCEPT",  TXT2IMG),
            Map.entry("SKETCH",   TXT2IMG),
            Map.entry("SDXL",     TXT2IMG),   // v1 compat
            Map.entry("DETAIL",   IMG2IMG),
            Map.entry("TEXTURE",  IMG2IMG),
            Map.entry("FABRIC",   IMG2IMG),   // v1 compat
            Map.entry("POSE",     IMG2IMG),
            Map.entry("RENDER",   IMG2IMG),
            Map.entry("UPSAMPLE", EXTRAS),
            Map.entry("UPSCALE",  EXTRAS),    // v1 compat
            Map.entry("THREE_D",  PLACEHOLDER)
    );

    // ===== 接口实现 =====

    @Override
    public ModelRouteDecision route(String stepType,
                                    AiFashionQualityConfig qualityConfig,
                                    AiFashionRoutingStrategyEnum routingStrategy) {

        String endpoint = ENDPOINT_MAP.getOrDefault(stepType.toUpperCase(), IMG2IMG);
        AiFashionQualityConfig.StageParams base = qualityConfig.stageParams(stepType);

        // 路由策略调整
        int steps = base.getSteps();
        float cfgScale = base.getCfgScale();
        String sampler = base.getSampler();
        float controlNetWeight = base.getControlNetWeight();

        String routingReason = switch (routingStrategy) {
            case SPEED_FIRST -> {
                steps = Math.max(10, (int) (steps * 0.65F)); // 减少 35%
                sampler = "LMS";                             // 快速采样器
                yield "速度优先：步数降至 " + steps + "，采样器改为 LMS";
            }
            case COST_FIRST -> {
                steps = Math.max(10, (int) (steps * 0.5F)); // 减少 50%
                cfgScale = Math.max(5.0F, cfgScale - 1.5F);
                yield "成本优先：步数降至 " + steps;
            }
            case QUALITY_FIRST -> {
                steps = (int) (steps * 1.2F);               // 增加 20%
                cfgScale = Math.min(12.0F, cfgScale + 0.5F);
                yield "质量优先：步数增至 " + steps;
            }
            default -> "均衡策略：使用预设参数（steps=" + steps + ")";
        };

        log.debug("[Router][stage={}][strategy={}] {} endpoint={}",
                stepType, routingStrategy.name(), routingReason, endpoint);

        return ModelRouteDecision.builder()
                .stepType(stepType)
                .sdApiEndpoint(endpoint)
                .steps(steps)
                .cfgScale(cfgScale)
                .sampler(sampler)
                .denoisingStrength(base.getDenoisingStrength())
                .useControlNet(base.isUseControlNet())
                .controlNetModule(base.getControlNetModule())
                .controlNetModel(base.getControlNetModel())
                .controlNetWeight(controlNetWeight)
                .estimatedLatencyMs(estimatedLatency(stepType))
                .routingReason(routingReason)
                .build();
    }

    /**
     * 记录实测延迟，更新 EMA（alpha=0.3）
     *
     * @param stepType    阶段类型
     * @param actualMs    本次实测延迟
     */
    public void recordLatency(String stepType, long actualMs) {
        latencyEma.computeIfAbsent(stepType, k -> new AtomicLong(actualMs))
                .updateAndGet(prev -> (long) (0.7 * prev + 0.3 * actualMs));
    }

    private int estimatedLatency(String stepType) {
        AtomicLong v = latencyEma.get(stepType.toUpperCase());
        return v != null ? (int) v.get() : 15_000;
    }

}
