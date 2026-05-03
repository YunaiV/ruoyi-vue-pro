package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import lombok.Data;

/**
 * 群消息置顶事件通知
 * <p>
 * payload 直接携带被置顶的完整消息对象，前端按 message 直接 push 到 pinnedMessages，避免 GET /im/group/get 命中 @CacheEvict 触发前的旧 cache
 */
@Data
public class GroupMessagePinNotification extends BaseGroupNotification {

    /**
     * 被置顶的消息编号
     */
    private Long messageId;
    /**
     * 被置顶的完整消息对象（避免前端回查群详情）
     */
    private ImGroupMessageDO message;

}
