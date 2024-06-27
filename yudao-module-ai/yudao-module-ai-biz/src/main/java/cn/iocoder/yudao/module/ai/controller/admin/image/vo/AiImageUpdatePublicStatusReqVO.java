package cn.iocoder.yudao.module.ai.controller.admin.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 绘画修改发布状态 Request VO")
@Data
public class AiImageUpdatePublicStatusReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15583")
    private Long id;

    @Schema(description = "是否发布", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否发布不能为空")
    private Boolean publicStatus;

}