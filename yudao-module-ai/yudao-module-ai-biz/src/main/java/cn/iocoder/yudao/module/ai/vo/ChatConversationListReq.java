package cn.iocoder.yudao.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 聊天对话 list req
 *
 * @author fansili
 * @time 2024/4/18 16:24
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class ChatConversationListReq {

    @Schema(description = "查询根据title")
    private String search;
}
