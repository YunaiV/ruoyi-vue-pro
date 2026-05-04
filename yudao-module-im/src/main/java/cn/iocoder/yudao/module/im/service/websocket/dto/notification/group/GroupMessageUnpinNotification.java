package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;

/**
 * 群消息取消置顶事件通知
 */
@Data
public class GroupMessageUnpinNotification extends BaseGroupNotification {

    /**
     * 被取消置顶的消息编号
     */
    private Long messageId;

}
