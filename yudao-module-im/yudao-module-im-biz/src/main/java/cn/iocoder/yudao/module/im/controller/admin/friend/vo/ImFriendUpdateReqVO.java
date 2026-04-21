package cn.iocoder.yudao.module.im.controller.admin.friend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IM 好友更新 Request VO")
@Data
public class ImFriendUpdateReqVO {

    @Schema(description = "好友的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "好友用户编号不能为空")
    private Long friendUserId;

    @Schema(description = "是否免打扰", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "免打扰状态不能为空")
    private Boolean muted;

}
