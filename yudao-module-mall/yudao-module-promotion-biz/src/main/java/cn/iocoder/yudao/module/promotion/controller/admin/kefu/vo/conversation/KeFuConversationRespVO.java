package cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客服会话 Response VO")
@Data
public class KeFuConversationRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24988")
    private Long id;

    @Schema(description = "会话所属用户", requiredMode = Schema.RequiredMode.REQUIRED, example = "8300")
    private Long userId;
    @Schema(description = "会话所属用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://yudao.com/images/avatar.jpg")
    private String userAvatar;
    @Schema(description = "会话所属用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String userNickname;

    @Schema(description = "最后聊天时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime lastMessageTime;

    @Schema(description = "最后聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "嗨，您好啊")
    private String lastMessageContent;

    @Schema(description = "最后发送的消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer lastMessageContentType;

    @Schema(description = "管理端置顶", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean adminPinned;

    @Schema(description = "用户是否可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean userDeleted;

    @Schema(description = "管理员是否可见", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean adminDeleted;

    @Schema(description = "管理员未读消息数", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Integer adminUnreadMessageCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}