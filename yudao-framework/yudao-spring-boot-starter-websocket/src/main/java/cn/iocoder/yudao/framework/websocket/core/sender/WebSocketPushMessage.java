package cn.iocoder.yudao.framework.websocket.core.sender;

import lombok.Data;

/**
 * WebSocket 本节点投递的消息
 *
 * 字段与 Redis、RocketMQ、Kafka、RabbitMQ 广播消息保持一致，便于相互转换
 *
 * @author 芋道源码
 */
@Data
public class WebSocketPushMessage {

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

    /**
     * 事件编号，发送端生成一次，集群各节点共用，用于日志排查、前端识别
     *
     * 为空时（旧版本消息），消费端补一个本地编号
     */
    private String eventId;
    /**
     * 租户编号
     *
     * 为空表示系统级消息，不按租户过滤
     */
    private Long tenantId;
    /**
     * 发送时间，单位：毫秒
     */
    private Long timestamp;

}
