package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.enums.image.AiFashionQualityPresetEnum;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 服装设计质量配置值对象
 *
 * <p>对应 Python 参考代码中的 {@code quality_configs} 字典，
 * 定义每个质量预设下所有阶段的推理参数：步数、CFG、采样器、去噪强度、ControlNet 权重等。</p>
 *
 * <p>使用 {@link #ofPreset(String)} 工厂方法创建，不可变。</p>
 *
 * @author deepay
 */
@Getter
public final class AiFashionQualityConfig {

    /** 质量预设标识 */
    private final String preset;
    /** 全局 CFG Scale */
    private final float cfgScale;
    /** 全局采样器名称 */
    private final String sampler;
    /** 超分辨率放大倍数 */
    private final int upscaleFactor;
    /** 超分模型 */
    private final String upscalerModel;
    /** ControlNet 全局权重 */
    private final float controlnetStrength;
    /** 是否在 RENDER 后额外细节增强（ULTRA/HIGH） */
    private final boolean enhanceDetails;
    /** 是否启用风格迁移（ULTRA） */
    private final boolean styleTransfer;
    /** 是否启用多轮迭代精炼（ULTRA PROFESSIONAL） */
    private final boolean multiPass;

    /** 各阶段独立参数表 key = stepType */
    private final Map<String, StageParams> stageParamsMap;

    // ===== 内嵌阶段参数 =====

    /**
     * 单阶段推理参数
     */
    @Getter
    @Builder
    public static class StageParams {
        /** 推理步数 */
        private final int steps;
        /** CFG Scale */
        private final float cfgScale;
        /** 采样器 */
        private final String sampler;
        /**
         * img2img 去噪强度；{@code null} 表示该阶段走 txt2img（无初始图）
         */
        private final Float denoisingStrength;
        /** 是否启用 ControlNet */
        private final boolean useControlNet;
        /** ControlNet 模块名，如 openpose / lineart / depth */
        private final String controlNetModule;
        /** ControlNet 模型名 */
        private final String controlNetModel;
        /** ControlNet 权重 */
        private final float controlNetWeight;
    }

    // ===== 私有构造 =====

    private AiFashionQualityConfig(String preset, float cfgScale, String sampler,
            int upscaleFactor, String upscalerModel, float controlnetStrength,
            boolean enhanceDetails, boolean styleTransfer, boolean multiPass,
            Map<String, StageParams> stageParamsMap) {
        this.preset = preset;
        this.cfgScale = cfgScale;
        this.sampler = sampler;
        this.upscaleFactor = upscaleFactor;
        this.upscalerModel = upscalerModel;
        this.controlnetStrength = controlnetStrength;
        this.enhanceDetails = enhanceDetails;
        this.styleTransfer = styleTransfer;
        this.multiPass = multiPass;
        this.stageParamsMap = stageParamsMap;
    }

    // ===== 公开查询方法 =====

    /**
     * 获取指定阶段的推理参数；找不到时返回通用默认值
     */
    public StageParams stageParams(String stepType) {
        return stageParamsMap.getOrDefault(stepType, defaultStageParams());
    }

    private StageParams defaultStageParams() {
        return StageParams.builder()
                .steps(25).cfgScale(cfgScale).sampler(sampler)
                .controlNetWeight(controlnetStrength).build();
    }

    // ===== 工厂方法 =====

    /**
     * 根据质量预设字符串创建配置，找不到返回 MEDIUM
     */
    public static AiFashionQualityConfig ofPreset(String preset) {
        return switch (AiFashionQualityPresetEnum.of(preset)) {
            case FAST  -> buildFast();
            case HIGH  -> buildHigh();
            case ULTRA -> buildUltra();
            default    -> buildMedium(); // MEDIUM
        };
    }

    // ===== FAST =====
    private static AiFashionQualityConfig buildFast() {
        Map<String, StageParams> m = new HashMap<>();
        m.put("CONCEPT", txt2img(10, 5.0F, "LMS"));
        m.put("SKETCH",  txt2img(15, 5.0F, "LMS"));
        m.put("SDXL",    txt2img(15, 5.0F, "LMS"));   // v1 compat
        m.put("DETAIL",  img2img(15, 5.5F, "LMS",  0.5F, false, null, null, 0.5F));
        m.put("TEXTURE", img2img(15, 5.5F, "LMS",  0.65F, false, null, null, 0.5F));
        m.put("FABRIC",  m.get("TEXTURE")); // v1 compat
        m.put("POSE",    img2img(15, 5.5F, "LMS",  0.5F, true,
                "openpose", "control_openpose-fp16 [9ca67cc5]", 0.6F));
        m.put("RENDER",  img2img(20, 6.0F, "Euler a", 0.3F, false, null, null, 0.5F));
        m.put("UPSAMPLE", noop()); m.put("UPSCALE", noop()); // v1 compat
        m.put("THREE_D", noop());
        return new AiFashionQualityConfig("FAST", 5.0F, "LMS",
                2, "R-ESRGAN 4x+", 0.6F, false, false, false, m);
    }

    // ===== MEDIUM =====
    private static AiFashionQualityConfig buildMedium() {
        Map<String, StageParams> m = new HashMap<>();
        m.put("CONCEPT", txt2img(15, 7.0F, "Euler a"));
        m.put("SKETCH",  txt2img(20, 7.0F, "Euler a"));
        m.put("SDXL",    txt2img(20, 7.0F, "Euler a"));
        m.put("DETAIL",  img2img(20, 7.0F, "Euler a", 0.5F, false, null, null, 0.6F));
        m.put("TEXTURE", img2img(20, 7.0F, "Euler a", 0.65F, false, null, null, 0.6F));
        m.put("FABRIC",  m.get("TEXTURE"));
        m.put("POSE",    img2img(20, 7.0F, "Euler a", 0.5F, true,
                "openpose", "control_openpose-fp16 [9ca67cc5]", 0.7F));
        m.put("RENDER",  img2img(25, 7.5F, "DPM++ 2M", 0.3F, false, null, null, 0.6F));
        m.put("UPSAMPLE", noop()); m.put("UPSCALE", noop());
        m.put("THREE_D", noop());
        return new AiFashionQualityConfig("MEDIUM", 7.0F, "Euler a",
                2, "R-ESRGAN 4x+", 0.7F, false, false, false, m);
    }

    // ===== HIGH =====
    private static AiFashionQualityConfig buildHigh() {
        Map<String, StageParams> m = new HashMap<>();
        m.put("CONCEPT", txt2img(20, 8.0F, "DPM++ 2M Karras"));
        m.put("SKETCH",  txt2img(30, 8.0F, "DPM++ 2M Karras"));
        m.put("SDXL",    txt2img(30, 8.0F, "DPM++ 2M Karras"));
        m.put("DETAIL",  img2img(30, 8.0F, "DPM++ 2M Karras", 0.5F, true,
                "lineart_realistic", "control_v11p_sd15_lineart [43d4be0d]", 0.75F));
        m.put("TEXTURE", img2img(25, 8.0F, "DPM++ 2M Karras", 0.7F, false, null, null, 0.7F));
        m.put("FABRIC",  m.get("TEXTURE"));
        m.put("POSE",    img2img(25, 8.0F, "DPM++ 2M Karras", 0.5F, true,
                "openpose", "control_openpose-fp16 [9ca67cc5]", 0.8F));
        m.put("RENDER",  img2img(40, 8.5F, "DPM++ 2M Karras", 0.3F, false, null, null, 0.7F));
        m.put("UPSAMPLE", noop()); m.put("UPSCALE", noop());
        m.put("THREE_D", noop());
        return new AiFashionQualityConfig("HIGH", 8.0F, "DPM++ 2M Karras",
                2, "R-ESRGAN 4x+", 0.8F, true, false, false, m);
    }

    // ===== ULTRA =====
    private static AiFashionQualityConfig buildUltra() {
        Map<String, StageParams> m = new HashMap<>();
        m.put("CONCEPT", txt2img(25, 8.5F, "DPM++ 3M Karras"));
        m.put("SKETCH",  txt2img(35, 8.5F, "DPM++ 3M Karras"));
        m.put("SDXL",    txt2img(35, 8.5F, "DPM++ 3M Karras"));
        m.put("DETAIL",  img2img(40, 8.5F, "DPM++ 3M Karras", 0.45F, true,
                "lineart_realistic", "control_v11p_sd15_lineart [43d4be0d]", 0.9F));
        m.put("TEXTURE", img2img(35, 8.5F, "DPM++ 3M Karras", 0.7F, false, null, null, 0.85F));
        m.put("FABRIC",  m.get("TEXTURE"));
        m.put("POSE",    img2img(35, 8.5F, "DPM++ 3M Karras", 0.5F, true,
                "openpose", "control_openpose-fp16 [9ca67cc5]", 0.9F));
        m.put("RENDER",  img2img(50, 9.0F, "DPM++ 3M Karras", 0.25F, false, null, null, 0.85F));
        m.put("UPSAMPLE", noop()); m.put("UPSCALE", noop());
        m.put("THREE_D", noop());
        return new AiFashionQualityConfig("ULTRA", 8.5F, "DPM++ 3M Karras",
                4, "R-ESRGAN 4x+", 0.9F, true, true, true, m);
    }

    // ===== 建造辅助 =====

    private static StageParams txt2img(int steps, float cfg, String sampler) {
        return StageParams.builder()
                .steps(steps).cfgScale(cfg).sampler(sampler)
                .denoisingStrength(null) // txt2img
                .useControlNet(false).controlNetWeight(0F).build();
    }

    private static StageParams img2img(int steps, float cfg, String sampler,
            float denoising, boolean cn, String cnModule, String cnModel, float cnWeight) {
        return StageParams.builder()
                .steps(steps).cfgScale(cfg).sampler(sampler)
                .denoisingStrength(denoising)
                .useControlNet(cn).controlNetModule(cnModule).controlNetModel(cnModel)
                .controlNetWeight(cnWeight).build();
    }

    private static StageParams noop() {
        return StageParams.builder().steps(0).cfgScale(0F).sampler("").controlNetWeight(0F).build();
    }

}
