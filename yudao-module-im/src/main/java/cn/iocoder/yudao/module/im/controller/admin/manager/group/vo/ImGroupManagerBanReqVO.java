package cn.iocoder.yudao.module.im.controller.admin.manager.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - IM 群聊封禁 Request VO")
@Data
public class ImGroupManagerBanReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "群编号不能为空")
    private Long id;

    @Schema(description = "封禁原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "违规内容")
    @NotBlank(message = "封禁原因不能为空")
    @Size(max = 200, message = "封禁原因长度不能超过 200")
    private String reason;

}
