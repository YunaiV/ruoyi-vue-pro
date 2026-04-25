package cn.iocoder.yudao.module.ai.service.image;

import lombok.Builder;
import lombok.Getter;

/**
 * AI 服装设计模型路由决策值对象
 *
 * <p>由 {@link AiFashionModelRouterService#route} 返回，
 * 封装"当前阶段应调用哪个 SD WebUI 端点、使用哪套参数"的完整决策结果。</p>
 *
 * @author deepay
 */
@Getter
@Builder
public final class ModelRouteDecision {

    /**
     * 步骤类型（CONCEPT / SKETCH / DETAIL / TEXTURE / POSE / RENDER / UPSAMPLE / THREE_D）
     */
    private final String stepType;

    /**
     * SD WebUI 端点标识
     * <ul>
     *   <li>{@code TXT2IMG} → {@code /sdapi/v1/txt2img}（CONCEPT/SKETCH/RENDER 首次）</li>
     *   <li>{@code IMG2IMG} → {@code /sdapi/v1/img2img}（DETAIL/TEXTURE/POSE）</li>
     *   <li>{@code EXTRAS}  → {@code /sdapi/v1/extra-single-image}（UPSAMPLE）</li>
     *   <li>{@code PLACEHOLDER} → 3D 阶段占位，暂不调用 SD</li>
     * </ul>
     */
    private final String sdApiEndpoint;

    /**
     * 本次路由决定的推理步数
     */
    private final int steps;

    /**
     * CFG Scale
     */
    private final float cfgScale;

    /**
     * 采样器名称
     */
    private final String sampler;

    /**
     * img2img 去噪强度；txt2img 时为 null
     */
    private final Float denoisingStrength;

    /**
     * 是否使用 ControlNet
     */
    private final boolean useControlNet;

    /**
     * ControlNet 模块名称（如 openpose / lineart_realistic）
     */
    private final String controlNetModule;

    /**
     * ControlNet 模型名称（如 control_openpose-fp16 [9ca67cc5]）
     */
    private final String controlNetModel;

    /**
     * ControlNet 权重
     */
    private final float controlNetWeight;

    /**
     * 预估延迟（毫秒），用于前端进度展示
     */
    private final int estimatedLatencyMs;

    /**
     * 路由决策理由（日志用）
     */
    private final String routingReason;

}
