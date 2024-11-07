package cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 绘画生成（Midjourney） Request VO")
@Data
public class AiMidjourneyImagineReqVO {

    @Schema(description = "提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "中国神龙")
    @NotEmpty(message = "提示词不能为空!")
    private String prompt;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "midjourney")
    @NotEmpty(message = "模型不能为空")
    private String model; // 参考 MidjourneyApi.ModelEnum

    @Schema(description = "图片宽度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "图片宽度不能为空")
    private Integer width;

    @Schema(description = "图片高度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "图片高度不能为空")
    private Integer height;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6.0")
    @NotEmpty(message = "版本号不能为空")
    private String version;

    @Schema(description = "参考图", example = "https://www.iocoder.cn/x.png")
    private String referImageUrl;

}
