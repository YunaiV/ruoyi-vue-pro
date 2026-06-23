package cn.iocoder.yudao.module.im.service.websocket.notification.message;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 群聊消息 WebSocket 统一推送通知
 *
 * @author 芋道源码
 */
@Data
public class ImGroupMessageNotification {

    /**
     * 消息编号
     */
    private Long id;
    /**
     * 客户端消息编号
     */
    private String clientMessageId;
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
     * 消息状态
     */
    private Integer status;
    /**
     * 回执状态
     */
    private Integer receiptStatus;
    /**
     * 已读人数
     */
    private Integer readCount;
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
    /**
     * 构建发送消息通知
     *
     * @param message 群聊消息 DO
     * @return 群聊通知
     */
    public static ImGroupMessageNotification ofSend(ImGroupMessageDO message) {
        return BeanUtils.toBean(message, ImGroupMessageNotification.class)
                .setReadCount(message.getReadCount() != null ? message.getReadCount() : 0);
    }

}
