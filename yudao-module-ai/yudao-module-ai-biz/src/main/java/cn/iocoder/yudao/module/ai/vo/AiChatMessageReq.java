package cn.iocoder.yudao.module.ai.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat message list req
 *
 * @author fansili
 * @time 2024/4/14 16:12
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AiChatMessageReq extends PageParam {

    @Schema(description = "聊天ID，关联到特定的会话或对话")
    @NotNull
    private Long chatConversationId;

}
