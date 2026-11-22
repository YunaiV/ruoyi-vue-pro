package cn.iocoder.yudao.framework.sse.core.sender;

import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.session.SseConnection;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.codec.SseEventEncoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SSE 消息的发送器，只在 yudao.sse.enable=true 时存在
 *
 * 1. 发送：补全 eventId、租户、时间后，调用 {@link #doPublish(SseMessage)}；
 *    local 直接本节点投递，Redis、RocketMQ、Kafka、RabbitMQ 先广播，各节点消费后调用 {@link #deliver(SseMessage)}
 * 2. 本节点投递：只投递到 {@link SseEmitterSessionManager} 的连接
 *    定位：connectionId（以 sse: 开头）、tenantId + userType + userId、tenantId + userType 广播；
 *    隔离：消息租户不为空时，只投递给同租户的连接；
 *    失败：发送失败立即移除连接，等待发送锁超时则跳过本条，连续失败达到上限后断开
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class SseMessageSender {

    private final SseEmitterSessionManager sessionManager;

    /**
     * 等待发送锁的超时时间，单位：毫秒
     */
    private final long sendTimeoutMillis;
    /**
     * 连续发送失败多少次后断开
     */
    private final int maxSendFailures;

    public SseMessageSender(SseEmitterSessionManager sessionManager, long sendTimeoutMillis, int maxSendFailures) {
        this.sessionManager = sessionManager;
        this.sendTimeoutMillis = sendTimeoutMillis;
        this.maxSendFailures = maxSendFailures;
    }

    /**
     * 发送消息给指定用户
     *
     * @param userType 用户类型
     * @param userId 用户编号
     * @param messageType 消息类型，作为 SSE 的 event 名
     * @param messageContent 消息内容，JSON 格式
     */
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        send(null, userType, userId, messageType, messageContent);
    }

    /**
     * 发送消息给指定用户类型
     *
     * @param userType 用户类型
     * @param messageType 消息类型，作为 SSE 的 event 名
     * @param messageContent 消息内容，JSON 格式
     */
    public void send(Integer userType, String messageType, String messageContent) {
        send(null, userType, null, messageType, messageContent);
    }

    /**
     * 发送消息给指定 SSE 连接
     *
     * @param connectionId SSE 连接编号，以 sse: 开头
     * @param messageType 消息类型，作为 SSE 的 event 名
     * @param messageContent 消息内容，JSON 格式
     */
    public void send(String connectionId, String messageType, String messageContent) {
        send(connectionId, null, null, messageType, messageContent);
    }

    public void sendObject(Integer userType, Long userId, String messageType, Object messageContent) {
        send(userType, userId, messageType, JsonUtils.toJsonString(messageContent));
    }

    public void sendObject(Integer userType, String messageType, Object messageContent) {
        send(userType, messageType, JsonUtils.toJsonString(messageContent));
    }

    public void sendObject(String connectionId, String messageType, Object messageContent) {
        send(connectionId, messageType, JsonUtils.toJsonString(messageContent));
    }

    /**
     * 发送消息
     *
     * @param connectionId SSE 连接编号，可为空
     * @param userType 用户类型
     * @param userId 用户编号，为空时发送给该用户类型的全部连接
     * @param messageType 消息类型
     * @param messageContent 消息内容，JSON 格式
     * @return 事件编号
     */
    public String send(String connectionId, Integer userType, Long userId,
                       String messageType, String messageContent) {
        SseMessage message = new SseMessage().setConnectionId(connectionId).setUserType(userType).setUserId(userId)
                .setMessageType(messageType).setMessageContent(messageContent)
                .setEventId(IdUtil.fastSimpleUUID()).setTenantId(TenantContextHolder.getTenantId())
                .setTimestamp(System.currentTimeMillis());
        doPublish(message);
        return message.getEventId();
    }

    /**
     * 执行发布：local 直接调用 {@link #deliver(SseMessage)}；Redis、RocketMQ、Kafka、RabbitMQ 广播到集群
     *
     * @param message 消息
     */
    protected abstract void doPublish(SseMessage message);

    // ==================== 本节点投递 ====================

    /**
     * 投递消息到本节点的 SSE 连接
     *
     * local 直接调用；Redis、RocketMQ、Kafka、RabbitMQ 的 Consumer 消费广播消息后调用
     *
     * @param message 消息
     */
    public void deliver(SseMessage message) {
        // 1. 获得连接列表
        List<SseConnection> connections = getConnectionList(message);
        if (CollUtil.isEmpty(connections)) {
            if (log.isDebugEnabled()) {
                log.debug("[deliver][connectionId({}) tenantId({}) userType({}) userId({}) messageType({}) 未匹配到 SSE 连接]",
                        message.getConnectionId(), message.getTenantId(), message.getUserType(), message.getUserId(),
                        message.getMessageType());
            }
            return;
        }
        // 2. 逐个发送
        for (SseConnection connection : connections) {
            doSend(connection, message);
        }
    }

    private void doSend(SseConnection connection, SseMessage message) {
        SseConnection.SendResult result = connection.send(SseEventEncoder.encodeMessage(message), sendTimeoutMillis);
        if (result == SseConnection.SendResult.SUCCESS) {
            log.info("[doSend][连接({}) 发送消息成功，eventId({}) messageType({})]",
                    connection.getConnectionId(), message.getEventId(), message.getMessageType());
            return;
        }
        if (result == SseConnection.SendResult.FAILED) {
            log.warn("[doSend][连接({}) 发送消息失败，移除连接，eventId({})]", connection.getConnectionId(), message.getEventId());
            sessionManager.abort(connection, null);
            return;
        }
        // 慢客户端：跳过本条，连续失败达到上限后断开
        log.warn("[doSend][连接({}) 等待发送锁超时，跳过 eventId({})，连续失败 {} 次]",
                connection.getConnectionId(), message.getEventId(), connection.getSendFailures());
        if (connection.getSendFailures() >= maxSendFailures) {
            sessionManager.abort(connection, null);
        }
    }

    private List<SseConnection> getConnectionList(SseMessage message) {
        Long tenantId = message.getTenantId();
        // 情况一：指定连接
        if (StrUtil.isNotEmpty(message.getConnectionId())) {
            SseConnection connection = sessionManager.getConnection(message.getConnectionId());
            if (connection == null) {
                return Collections.emptyList();
            }
            if (tenantId != null && !Objects.equals(tenantId, connection.getTenantId())) {
                log.warn("[getConnectionList][连接({}) 租户({}) 与消息租户({}) 不匹配，忽略投递]",
                        connection.getConnectionId(), connection.getTenantId(), tenantId);
                return Collections.emptyList();
            }
            return Collections.singletonList(connection);
        }
        // 情况二：指定用户
        if (message.getUserType() != null && message.getUserId() != null) {
            return sessionManager.getConnectionList(tenantId, message.getUserType(), message.getUserId());
        }
        // 情况三：指定用户类型广播；租户为空时为系统级广播
        if (message.getUserType() != null) {
            return sessionManager.getConnectionList(tenantId, message.getUserType());
        }
        return Collections.emptyList();
    }

}
