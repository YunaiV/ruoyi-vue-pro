package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理后台 - 群消息置顶 / 取消置顶 Request VO
 */
@Schema(description = "管理后台 - 群消息置顶 / 取消置顶 Request VO")
@Data
public class ImGroupMessagePinReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13279")
    @NotNull(message = "群编号不能为空")
    private Long id;

    @Schema(description = "消息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9527")
    @NotNull(message = "消息编号不能为空")
    private Long messageId;

}
