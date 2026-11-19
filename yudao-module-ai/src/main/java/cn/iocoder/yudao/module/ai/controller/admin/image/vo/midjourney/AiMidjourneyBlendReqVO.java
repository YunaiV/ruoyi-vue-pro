package cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 绘画融图（Midjourney） Request VO")
@Data
public class AiMidjourneyBlendReqVO {

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型编号不能为空")
    private Long modelId;

    @Schema(description = "融图图片地址数组（2-5 张）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "融图图片不能为空")
    @Size(min = 2, max = 5, message = "融图需要 2-5 张图片")
    private List<String> imageUrls;

    @Schema(description = "融图尺寸：PORTRAIT、SQUARE、LANDSCAPE", requiredMode = Schema.RequiredMode.REQUIRED, example = "SQUARE")
    @NotEmpty(message = "融图尺寸不能为空")
    @Pattern(regexp = "^(PORTRAIT|SQUARE|LANDSCAPE)$", message = "融图尺寸必须是 PORTRAIT、SQUARE、LANDSCAPE")
    private String dimensions;

}
