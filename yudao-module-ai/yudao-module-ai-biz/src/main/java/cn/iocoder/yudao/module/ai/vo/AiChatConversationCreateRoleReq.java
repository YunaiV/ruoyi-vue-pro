package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 聊天对话
 *
 * @author fansili
 * @time 2024/4/18 16:24
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatConversationCreateRoleReq {

    @Schema(description = "chat角色Id")
    @NotNull(message = "聊天角色id不能为空!")
    private Long roleId;

    @Schema(description = "标题(有程序自动生成)")
    @NotNull(message = "标题不能为空!")
    private String title;
}
