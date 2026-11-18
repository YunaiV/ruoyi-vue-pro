package cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 绘画局部重绘（Midjourney） Request VO")
@Data
public class AiMidjourneyModalReqVO {

    @Schema(description = "来源图片编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源图片编号不能为空")
    private Long id;

    @Schema(description = "重绘提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "换成一顶红色的帽子")
    @NotEmpty(message = "重绘提示词不能为空")
    @Size(max = 1200, message = "重绘提示词最大 1200")
    private String prompt;

    @Schema(description = "蒙版图片地址（黑底，白色为重绘区域）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://www.iocoder.cn/mask.png")
    @NotEmpty(message = "蒙版不能为空")
    private String maskUrl;

}
