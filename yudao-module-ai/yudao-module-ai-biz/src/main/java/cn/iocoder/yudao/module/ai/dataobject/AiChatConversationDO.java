package cn.iocoder.yudao.module.ai.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat 交谈
 *
 * @author fansili
 * @time 2024/4/14 17:35
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@TableName("ai_chat_conversation")
public class AiChatConversationDO extends BaseDO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "chat角色Id")
    private Long chatRoleId;

    @Schema(description = "chat角色名称")
    private String chatRoleName;

    @Schema(description = "聊天标题(有程序自动生成)")
    private String chatTitle;

    @Schema(description = "聊天次数(有程序自动生成)")
    private Integer chatCount;

    @Schema(description = "对话类型(roleChat、userChat)")
    private String chatType;

}
