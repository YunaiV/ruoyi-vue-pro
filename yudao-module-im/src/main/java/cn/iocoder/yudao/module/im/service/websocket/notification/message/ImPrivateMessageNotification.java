package cn.iocoder.yudao.module.im.service.websocket.notification.message;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * IM 私聊消息 WebSocket 统一推送通知
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImPrivateMessageNotification {

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
     * 接收人编号
     */
    private Long receiverId;
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
     * 发送时间
     */
    private LocalDateTime sendTime;

    // ========== 静态工厂方法 ==========

    /**
     * 构建发送消息通知
     *
     * @param message 私聊消息 DO
     * @return 私聊通知
     */
    public static ImPrivateMessageNotification ofSend(ImPrivateMessageDO message) {
        return BeanUtils.toBean(message, ImPrivateMessageNotification.class);
    }

}
