package cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 绘画换脸（Midjourney） Request VO")
@Data
public class AiMidjourneySwapFaceReqVO {

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型编号不能为空")
    private Long modelId;

    @Schema(description = "来源脸图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    @NotEmpty(message = "来源脸图片不能为空")
    private String sourceUrl;

    @Schema(description = "目标图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/2.png")
    @NotEmpty(message = "目标图片不能为空")
    private String targetUrl;

}
