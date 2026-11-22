package cn.iocoder.yudao.framework.sse.core.message;

import lombok.Data;

/**
 * SSE 的消息
 *
 * 本节点投递，以及 RocketMQ、Kafka、RabbitMQ 广播时使用；Redis 广播时包装为 RedisSseMessage
 *
 * @author 芋道源码
 */
@Data
public class SseMessage {

    /**
     * SSE 连接编号，以 sse: 开头
     */
    private String connectionId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 消息类型，作为 SSE 的 event 名
     */
    private String messageType;
    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 事件编号，发送端生成一次，集群各节点共用，用于日志排查、前端识别
     */
    private String eventId;
    /**
     * 租户编号，为空表示系统级消息，不按租户过滤
     */
    private Long tenantId;
    /**
     * 发送时间，单位：毫秒
     */
    private Long timestamp;

}
