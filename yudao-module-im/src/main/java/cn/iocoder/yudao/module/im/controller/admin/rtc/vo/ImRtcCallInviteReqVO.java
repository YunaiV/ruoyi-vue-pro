package cn.iocoder.yudao.module.im.controller.admin.rtc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - 发起通话 Request VO")
@Data
public class ImRtcCallInviteReqVO {

    // TODO @AI：换成 conversationType
    @Schema(description = "会话场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会话场景不能为空")
    private Integer scene; // 参见 ImConversationTypeEnum 枚举类

    @Schema(description = "媒体类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "媒体类型不能为空")
    private Integer mediaType; // 参见 ImCallMediaTypeEnum 枚举类

    // TODO @AI：是不是融合到 inviteeIds 里？
    @Schema(description = "对方用户编号；私聊场景必填", example = "1024")
    private Long peerUserId;

    @Schema(description = "群编号；群聊场景必填", example = "2048")
    private Long groupId;

    @Schema(description = "被邀请的用户编号集合；群聊场景由前端选成员后传入；为空时回退到拉群活跃成员")
    private Set<Long> inviteeIds;

}
