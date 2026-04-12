package cn.iocoder.yudao.module.im.controller.admin.message.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 群聊已读 Request VO
 */
@Schema(description = "管理后台 - 群聊已读 Request VO")
@Data
public class ImGroupMessageReadReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "群编号不能为空")
    private Long groupId;

}
