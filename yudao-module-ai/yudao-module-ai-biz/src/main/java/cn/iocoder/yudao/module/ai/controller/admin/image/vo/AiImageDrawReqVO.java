package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;

import java.util.Map;

@Schema(description = "管理后台 - AI 绘画 Request VO")
@Data
public class AiImageDrawReqVO {

    @Schema(description = "模型平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "OpenAI")
    private String platform; // 参见 AiPlatformEnum 枚举

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "画一个长城")
    @NotEmpty(message = "提示词不能为空")
    @Size(max = 1200, message = "提示词最大 1200")
    private String prompt;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "stable-diffusion-v1-6")
    @NotEmpty(message = "模型不能为空")
    private String model;

    /**
     * 1. dall-e-2 模型：256x256、512x512、1024x1024
     * 2. dall-e-3 模型：1024x1024, 1792x1024, 或 1024x1792
     */
    @Schema(description = "图片高度")
    @NotNull(message = "图片高度不能为空")
    private Integer height;

    @Schema(description = "图片宽度")
    @NotNull(message = "图片宽度不能为空")
    private Integer width;

    // ========== 各平台绘画的拓展参数 ==========

    /**
     * 绘制参数，不同 platform 的不同参数
     *
     * 1. {@link OpenAiImageOptions}
     * 2. {@link StabilityAiImageOptions}
     */
    @Schema(description = "绘制参数")
    private Map<String, String> options;

}
