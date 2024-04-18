package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 聊天对话 res
 *
 * @author fansili
 * @time 2024/4/18 16:24
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class ChatConversationRes {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "chat角色Id")
    private Long chatRoleId;

    @Schema(description = "chat角色名称")
    private String chatRoleName;

    @Schema(description = "标题(有程序自动生成)")
    private String title;

    @Schema(description = "对话类型(roleChat、userChat)")
    private String type;

    @Schema(description = "聊天次数(有程序自动生成)")
    private Integer chatCount;
}
