package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * AI 服装设计工作流模式枚举
 *
 * <p>对应 Python 参考代码中的 {@code workflow} 字段，定义激活哪些阶段。</p>
 *
 * <pre>
 * BASIC        → [SKETCH, UPSAMPLE]                                       2 阶段
 * STANDARD     → [SKETCH, TEXTURE*, POSE*, UPSAMPLE]                     2~4 阶段（*按需启用）
 * PROFESSIONAL → [CONCEPT, SKETCH, DETAIL, TEXTURE, POSE, RENDER, UPSAMPLE] 7 阶段
 * FULL         → PROFESSIONAL + [THREE_D]                                  8 阶段
 * </pre>
 *
 * @author deepay
 */
@AllArgsConstructor
@Getter
public enum AiFashionWorkflowModeEnum {

    BASIC("BASIC", "基础模式",
            List.of("SKETCH", "UPSAMPLE")),

    STANDARD("STANDARD", "标准模式",
            List.of("SKETCH", "TEXTURE", "POSE", "UPSAMPLE")),

    PROFESSIONAL("PROFESSIONAL", "专业模式",
            List.of("CONCEPT", "SKETCH", "DETAIL", "TEXTURE", "POSE", "RENDER", "UPSAMPLE")),

    FULL("FULL", "完整模式（含 3D）",
            List.of("CONCEPT", "SKETCH", "DETAIL", "TEXTURE", "POSE", "RENDER", "UPSAMPLE", "THREE_D"));

    /** 模式标识 */
    private final String mode;
    /** 显示名称 */
    private final String name;
    /**
     * 默认阶段顺序
     * 注意：STANDARD 模式中 TEXTURE/POSE 在运行时按需筛选（无参考图则跳过）
     */
    private final List<String> defaultStages;

    public static AiFashionWorkflowModeEnum of(String mode) {
        if (mode == null) {
            return STANDARD;
        }
        for (AiFashionWorkflowModeEnum e : values()) {
            if (e.mode.equalsIgnoreCase(mode)) {
                return e;
            }
        }
        return STANDARD;
    }

}
