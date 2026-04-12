package cn.iocoder.yudao.module.im.controller.admin.message.vo.privates;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 私聊已读 Request VO
 */
@Schema(description = "管理后台 - 私聊已读 Request VO")
@Data
public class ImPrivateMessageReadReqVO {

    @Schema(description = "好友用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "好友用户编号不能为空")
    private Long friendId;

}
