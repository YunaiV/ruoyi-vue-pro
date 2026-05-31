package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 撤回提示消息内容
 */
@Data
@Accessors(chain = true)
public class RecallMessage {

    /**
     * 被撤回的原消息编号
     */
    private Long messageId;

}
