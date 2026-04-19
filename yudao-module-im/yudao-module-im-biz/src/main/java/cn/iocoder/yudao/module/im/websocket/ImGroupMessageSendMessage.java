package cn.iocoder.yudao.module.im.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 群聊消息 WebSocket 推送消息
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImGroupMessageSendMessage {

    public static final String TYPE = "im-group-message-send";

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
     * 发送时间
     */
    private LocalDateTime sendTime;
    /**
     * @ 的用户编号列表
     */
    private List<Long> atUserIds;
    /**
     * 定向接收用户编号列表
     */
    private List<Long> receiverUserIds;
    /**
     * 群回执状态
     */
    private Integer receiptStatus;

}
