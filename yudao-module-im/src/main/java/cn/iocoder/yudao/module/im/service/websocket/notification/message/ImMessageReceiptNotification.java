package cn.iocoder.yudao.module.im.service.websocket.notification.message;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IM 消息回执通知
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImMessageReceiptNotification {

    /**
     * 消息编号
     */
    private Long id;
    /**
     * 已读方的用户编号
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
     * 已读人数
     */
    private Integer readCount;
    /**
     * 回执状态
     */
    private Integer receiptStatus;

    public static ImMessageReceiptNotification ofPrivate(Long senderId, Long receiverId, Long readId) {
        return new ImMessageReceiptNotification()
                .setId(readId).setSenderId(senderId).setReceiverId(receiverId);
    }

    public static ImMessageReceiptNotification ofGroup(Long messageId, Long groupId,
                                                       Integer readCount, Integer receiptStatus) {
        return new ImMessageReceiptNotification()
                .setId(messageId).setGroupId(groupId)
                .setReadCount(readCount).setReceiptStatus(receiptStatus);
    }

}
