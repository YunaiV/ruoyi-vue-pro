package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 服装设计质量预设枚举
 *
 * <p>对应 Python 参考代码中的 {@code quality_preset} 字段。
 * 每个预设定义了全套模型推理参数（步数、CFG、采样器等）。</p>
 *
 * <pre>
 * FAST       → 15步 / Euler a        / 快速预览
 * MEDIUM     → 25步 / DPM++ 2M       / 标准质量（默认）
 * HIGH       → 40步 / DPM++ 2M Karras/ 高质量
 * ULTRA      → 50步 / DPM++ 3M Karras/ 极致质量（最慢）
 * </pre>
 *
 * @author deepay
 */
@AllArgsConstructor
@Getter
public enum AiFashionQualityPresetEnum {

    FAST("FAST", "快速预览",
            15, 5.0F, "Euler a",
            512, 768),

    MEDIUM("MEDIUM", "标准质量",
            25, 7.0F, "Euler a",
            768, 1024),

    HIGH("HIGH", "高质量",
            40, 8.0F, "DPM++ 2M Karras",
            768, 1024),

    ULTRA("ULTRA", "极致质量",
            50, 8.5F, "DPM++ 3M Karras",
            1024, 1360);

    /** 预设标识 */
    private final String preset;
    /** 显示名称 */
    private final String name;
    /** 推理步数 */
    private final int steps;
    /** CFG Scale */
    private final float cfgScale;
    /** 采样器名称 */
    private final String sampler;
    /** 默认宽度 */
    private final int defaultWidth;
    /** 默认高度 */
    private final int defaultHeight;

    /**
     * 按标识查找，找不到返回 MEDIUM
     */
    public static AiFashionQualityPresetEnum of(String preset) {
        if (preset == null) {
            return MEDIUM;
        }
        for (AiFashionQualityPresetEnum e : values()) {
            if (e.preset.equalsIgnoreCase(preset)) {
                return e;
            }
        }
        return MEDIUM;
    }

}
