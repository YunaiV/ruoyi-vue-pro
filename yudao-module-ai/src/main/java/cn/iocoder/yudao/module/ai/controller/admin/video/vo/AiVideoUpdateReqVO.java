package cn.iocoder.yudao.module.ai.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 视频修改 Request VO")
@Data
public class AiVideoUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否公开不能为空")
    private Boolean publicStatus;

}
