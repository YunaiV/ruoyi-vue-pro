package cn.iocoder.yudao.module.ai.service.image;

import java.util.Map;

/**
 * AI 服装设计 Prompt 增强工具
 *
 * <p>对应 Python 参考代码中的 {@code _optimize_prompt()} 方法。
 * 根据质量预设添加前缀、根据阶段类型添加后缀，提升生成质量与一致性。</p>
 *
 * <p>无状态工具类，所有方法均为静态。</p>
 *
 * @author deepay
 */
public final class AiFashionPromptEnhancer {

    private AiFashionPromptEnhancer() {}

    /**
     * 质量前缀（对应 Python {@code quality_prefixes}）
     */
    private static final Map<String, String> QUALITY_PREFIXES = Map.of(
            "FAST",         "",
            "MEDIUM",       "best quality, ",
            "HIGH",         "masterpiece, best quality, ultra detailed, ",
            "ULTRA",        "masterpiece, best quality, ultra detailed, 8k, photorealistic, "
    );

    /**
     * 阶段后缀（对应 Python {@code stage_prompts}）
     */
    private static final Map<String, String> STAGE_SUFFIXES = Map.of(
            "CONCEPT",  "fashion concept art, rough sketch, idea generation, loose brushwork",
            "SKETCH",   "fashion sketch, line drawing, technical flat, clean lines, garment construction",
            "SDXL",     "fashion sketch, line drawing, technical flat, clean lines",   // v1 compat
            "DETAIL",   "detailed fashion design, intricate details, haute couture, fabric texture, craftsmanship",
            "TEXTURE",  "fashion design with fabric texture, material study, surface detail, weave pattern",
            "FABRIC",   "fashion design with fabric texture, material study, surface detail",   // v1 compat
            "POSE",     "fashion model, professional pose, full-body shot, fashion photography",
            "RENDER",   "professional fashion render, studio lighting, high fashion photography, vogue, editorial",
            "UPSAMPLE", "",
            "THREE_D",  "3D render, fashion design, clothing construction, three-dimensional"
    );

    /**
     * 通用服装设计后缀
     */
    private static final String COMMON_FASHION_SUFFIX =
            "fashion design, clothing, apparel, haute couture, runway, editorial photography";

    /**
     * 增强 prompt
     *
     * @param prompt       原始用户提示词
     * @param qualityPreset 质量预设（FAST/MEDIUM/HIGH/ULTRA）
     * @param stepType     阶段类型（CONCEPT/SKETCH/DETAIL/...）
     * @return 增强后的完整提示词
     */
    public static String enhance(String prompt, String qualityPreset, String stepType) {
        String prefix = QUALITY_PREFIXES.getOrDefault(
                qualityPreset != null ? qualityPreset.toUpperCase() : "MEDIUM",
                "best quality, ");
        String suffix = STAGE_SUFFIXES.getOrDefault(
                stepType != null ? stepType.toUpperCase() : "",
                COMMON_FASHION_SUFFIX);
        if (suffix.isBlank()) {
            return prefix + prompt;
        }
        return prefix + prompt + ", " + suffix;
    }

    /**
     * 生成默认负向提示词（对应 Python {@code _get_negative_prompt()}）
     */
    public static String defaultNegativePrompt() {
        return "ugly, deformed, noisy, blurry, distorted, out of focus, bad anatomy, "
                + "worst quality, low quality, extra fingers, mutated hands, "
                + "poorly drawn hands, poorly drawn face, mutation, disfigured, "
                + "tiling, bad proportions, malformed limbs, extra limbs, "
                + "cloned face, missing arms, missing legs, extra arms, "
                + "extra legs, fused fingers, too many fingers, text, watermark";
    }

}
