package cn.iocoder.yudao.module.im.dal.dataobject.message.content;

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
