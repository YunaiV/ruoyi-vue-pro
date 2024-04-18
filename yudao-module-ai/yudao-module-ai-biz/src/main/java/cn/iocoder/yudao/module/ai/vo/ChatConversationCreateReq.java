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
public class ChatConversationCreateReq {

    @Schema(description = "对话类型(roleChat、userChat)")
    @NotNull(message = "聊天类型不能为空!")
    private String chatType;

}
