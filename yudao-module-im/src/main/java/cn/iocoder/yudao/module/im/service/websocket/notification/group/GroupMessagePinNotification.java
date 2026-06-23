package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群消息置顶事件通知
 */
@Data
public class GroupMessagePinNotification extends BaseGroupNotification {

    /**
     * 被置顶的消息编号
     */
    private Long messageId;
    /**
     * 被置顶的消息展示数据
     */
    private PinnedMessage message;

    /**
     * 被置顶的消息展示数据
     */
    @Data
    public static class PinnedMessage {

        /**
         * 消息编号
         */
        private Long id;
        /**
         * 发送人编号
         */
        private Long senderId;
        /**
         * 群编号
         */
        private Long groupId;
        /**
         * 消息类型
         */
        private Integer type;
        /**
         * 消息内容
         */
        private String content;
        /**
         * 发送时间
         */
        private LocalDateTime sendTime;
        /**
         * @ 目标用户编号列表
         */
        private List<Long> atUserIds;
        /**
         * 定向接收用户编号列表
         */
        private List<Long> receiverUserIds;

    }

}
