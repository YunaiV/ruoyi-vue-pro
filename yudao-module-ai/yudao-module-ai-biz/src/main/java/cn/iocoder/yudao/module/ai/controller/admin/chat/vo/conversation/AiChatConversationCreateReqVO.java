package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - AI 聊天会话创建 Request VO")
@Data
public class AiChatConversationCreateReqVO {

    @Schema(description = "角色编号", example = "666")
    private Long roleId;

}
