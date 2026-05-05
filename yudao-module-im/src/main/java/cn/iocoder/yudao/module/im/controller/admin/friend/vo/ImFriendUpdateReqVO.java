package cn.iocoder.yudao.module.im.controller.admin.friend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - IM 好友更新 Request VO")
@Data
public class ImFriendUpdateReqVO {

    @Schema(description = "好友的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "好友用户编号不能为空")
    private Long friendUserId;

    @Schema(description = "是否免打扰；不传表示不修改", example = "true")
    private Boolean silent;

    @Schema(description = "好友展示备注（仅自己可见）；不传表示不修改，传空串表示清空", example = "老张")
    @Size(max = 16, message = "好友备注最多 16 个字符")
    private String displayName;

    @Schema(description = "是否置顶联系人；不传表示不修改", example = "true")
    private Boolean pinned;

}
