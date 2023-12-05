package cn.iocoder.yudao.framework.websocket.core.sender.rabbitmq;

import lombok.Data;

import java.io.Serializable;

/**
 * RabbitMQ 广播 WebSocket 的消息
 *
 * @author 芋道源码
 */
@Data
public class RabbitMQWebSocketMessage implements Serializable {

    /**
     * Session 编号
     */
    private String sessionId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 消息类型
     */
    private String messageType;
    /**
     * 消息内容
     */
    private String messageContent;

}
