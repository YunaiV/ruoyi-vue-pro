package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 绘画 Request VO")
@Data
public class AiImageDrawReqVO {

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "模型编号不能为空")
    private Long modelId;

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "画一个长城")
    @NotEmpty(message = "提示词不能为空")
    @Size(max = 1200, message = "提示词最大 1200")
    private String prompt;

    @Schema(description = "不希望出现的内容", example = "文字、水印")
    @Size(max = 2000, message = "不希望出现的内容最大 2000")
    private String negativePrompt;

    @Schema(description = "参考图地址数组")
    @Size(max = 10, message = "参考图最多 10 张")
    private List<String> referenceImageUrls;

    // ========== 尺寸：固定尺寸、像素宽高使用 width / height；Seedream 档位使用 sizeLevel ==========

    @Schema(description = "图片宽度", example = "1024")
    @Min(value = 1, message = "图片宽度最小 1")
    private Integer width;

    @Schema(description = "图片高度", example = "1024")
    @Min(value = 1, message = "图片高度最小 1")
    private Integer height;

    @Schema(description = "尺寸档位（Seedream）", example = "2K")
    private String sizeLevel;

    @Schema(description = "比例（只用于回填）", example = "1:1")
    private String aspectRatio;

    // ========== 画质、高级参数：前端按模型开放 ==========

    @Schema(description = "画质（OpenAI）", example = "hd")
    private String quality;

    @Schema(description = "随机种子（硅基、Stability）", example = "0")
    @Min(value = 0, message = "随机种子最小 0")
    private Long seed;

    @Schema(description = "推理步数（硅基、Stability）", example = "30")
    @Min(value = 1, message = "推理步数最小 1")
    private Integer steps;

    @Schema(description = "引导系数（硅基 guidance_scale、Stability cfg_scale）", example = "7.5")
    @DecimalMin(value = "0", message = "引导系数最小 0")
    private Double cfgScale;

    @Schema(description = "采样器（Stability）", example = "K_DPMPP_2M")
    private String sampler;

    @Schema(description = "风格预设（Stability）", example = "anime")
    private String stylePreset;

    @Schema(description = "CLIP 引导（Stability）", example = "NONE")
    private String clipGuidancePreset;

}
