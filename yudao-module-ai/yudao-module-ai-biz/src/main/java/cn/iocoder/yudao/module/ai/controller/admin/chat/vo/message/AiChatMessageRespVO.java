package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 聊天消息 Response VO")
@Data
public class AiChatMessageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long conversationId;

    @Schema(description = "回复消息编号", example = "1024")
    private Long replyId;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "role")
    private String type; // 参见 MessageType 枚举类

    @Schema(description = "用户编号", example = "4096")
    private Long userId;

    @Schema(description = "角色编号", example = "888")
    private Long roleId;

    @Schema(description = "模型标志", requiredMode = Schema.RequiredMode.REQUIRED, example = "gpt-3.5-turbo")
    private String model;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long modelId;

    @Schema(description = "聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，你好啊")
    private String content;

    @Schema(description = "是否携带上下文", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean useContext;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-05-12 12:51")
    private LocalDateTime createTime;

    // ========== 仅在【对话管理】时加载 ==========

    @Schema(description = "角色名字", example = "小黄")
    private String roleName;

}
