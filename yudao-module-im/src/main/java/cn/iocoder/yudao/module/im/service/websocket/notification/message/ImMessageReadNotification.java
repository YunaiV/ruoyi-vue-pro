package cn.iocoder.yudao.module.im.service.websocket.notification.message;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IM 消息已读同步通知
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImMessageReadNotification {

    /**
     * 已读位置（最大已读消息编号）
     */
    private Long id;
    /**
     * 发送人编号
     */
    private Long senderId;
    /**
     * 私聊接收人编号
     */
    private Long receiverId;
    /**
     * 群编号
     */
    private Long groupId;
    /**
     * 频道编号
     */
    private Long channelId;
    /**
     * 已读位置（最大已读消息编号）
     */
    private Long readId;

    public static ImMessageReadNotification ofPrivate(Long senderId, Long receiverId, Long readId) {
        return new ImMessageReadNotification()
                .setId(readId).setReadId(readId)
                .setSenderId(senderId).setReceiverId(receiverId);
    }

    public static ImMessageReadNotification ofGroup(Long senderId, Long groupId, Long readId) {
        return new ImMessageReadNotification()
                .setId(readId).setReadId(readId)
                .setSenderId(senderId).setGroupId(groupId);
    }

    public static ImMessageReadNotification ofChannel(Long channelId, Long readId) {
        return new ImMessageReadNotification()
                .setId(readId).setReadId(readId)
                .setChannelId(channelId);
    }

}
