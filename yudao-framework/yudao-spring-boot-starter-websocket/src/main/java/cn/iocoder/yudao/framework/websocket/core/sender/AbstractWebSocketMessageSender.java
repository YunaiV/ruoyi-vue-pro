package cn.iocoder.yudao.framework.websocket.core.sender;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.framework.websocket.core.message.JsonWebSocketMessage;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManager;
import cn.iocoder.yudao.framework.websocket.core.util.WebSocketFrameworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * WebSocketMessageSender 实现类
 *
 * local 直接本节点投递；Redis、RocketMQ、Kafka、RabbitMQ 先广播，各节点消费后调用 {@link #send(WebSocketPushMessage)} 投递
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebSocketMessageSender implements WebSocketMessageSender {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        send(null, userType, userId, messageType, messageContent);
    }

    @Override
    public void send(Integer userType, String messageType, String messageContent) {
        send(null, userType, null, messageType, messageContent);
    }

    @Override
    public void send(String sessionId, String messageType, String messageContent) {
        send(sessionId, null, null, messageType, messageContent);
    }

    /**
     * 发送 WebSocket 消息
     *
     * @param sessionId Session 编号，可为空
     * @param userType 用户类型
     * @param userId 用户编号，为空时发送给该用户类型的全部连接
     * @param messageType 消息类型
     * @param messageContent 消息内容，JSON 格式
     * @return 事件编号
     */
    public String send(String sessionId, Integer userType, Long userId,
                       String messageType, String messageContent) {
        WebSocketPushMessage message = new WebSocketPushMessage().setSessionId(sessionId).setUserType(userType).setUserId(userId)
                .setMessageType(messageType).setMessageContent(messageContent);
        publish(message);
        return message.getEventId();
    }

    /**
     * 发布消息：补全 eventId、租户、时间后，调用 {@link #doPublish(WebSocketPushMessage)}
     *
     * @param message 消息
     */
    public void publish(WebSocketPushMessage message) {
        if (StrUtil.isEmpty(message.getEventId())) {
            message.setEventId(IdUtil.fastSimpleUUID());
        }
        if (message.getTenantId() == null) {
            message.setTenantId(TenantContextHolder.getTenantId());
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis());
        }
        doPublish(message);
    }

    /**
     * 执行发布，默认本节点投递；Redis、RocketMQ、Kafka、RabbitMQ 重写为集群广播
     *
     * @param message 消息
     */
    protected void doPublish(WebSocketPushMessage message) {
        send(message);
    }

    /**
     * 发送消息到本节点的连接
     *
     * local 直接调用；Redis、RocketMQ、Kafka、RabbitMQ 的 Consumer 消费广播消息后调用
     *
     * @param message 消息
     */
    public void send(WebSocketPushMessage message) {
        // 1. 兼容旧版本消息：未携带 tenantId 时，使用 MQ 租户拦截器恢复的上下文租户；未携带 eventId 时，补本地编号
        if (message.getTenantId() == null) {
            message.setTenantId(TenantContextHolder.getTenantId());
        }
        if (StrUtil.isEmpty(message.getEventId())) {
            message.setEventId(IdUtil.fastSimpleUUID());
        }
        // 2. 投递到本节点的 WebSocket 连接
        doSend(getSessionList(message), message);
    }

    private List<WebSocketSession> getSessionList(WebSocketPushMessage message) {
        Long tenantId = message.getTenantId();
        List<WebSocketSession> sessions = Collections.emptyList();
        // 情况一：指定 Session。校验租户，避免伪造 sessionId 跨租户投递
        if (StrUtil.isNotEmpty(message.getSessionId())) {
            WebSocketSession session = sessionManager.getSession(message.getSessionId());
            if (session != null) {
                sessions = filterByTenant(Collections.singletonList(session), tenantId);
            }
        // 情况二：指定用户
        } else if (message.getUserType() != null && message.getUserId() != null) {
            sessions = filterByTenant(sessionManager.getSessionList(message.getUserType(), message.getUserId()), tenantId);
        // 情况三：指定用户类型。WebSocketSessionManager 按上下文租户过滤，这里切换为消息的租户；租户为空时为系统级广播
        } else if (message.getUserType() != null) {
            sessions = filterByTenant(TenantUtils.execute(tenantId,
                    () -> sessionManager.getSessionList(message.getUserType())), tenantId);
        }
        if (CollUtil.isEmpty(sessions) && log.isDebugEnabled()) {
            log.debug("[send][sessionId({}) userType({}) userId({}) messageType({}) messageContent({}) 未匹配到会话]",
                    message.getSessionId(), message.getUserType(), message.getUserId(),
                    message.getMessageType(), message.getMessageContent());
        }
        return sessions;
    }

    private static List<WebSocketSession> filterByTenant(Collection<WebSocketSession> sessions, Long tenantId) {
        if (CollUtil.isEmpty(sessions)) {
            return Collections.emptyList();
        }
        List<WebSocketSession> result = new ArrayList<>(sessions.size());
        for (WebSocketSession session : sessions) {
            if (tenantId == null || Objects.equals(tenantId, WebSocketFrameworkUtils.getTenantId(session))) {
                result.add(session);
            }
        }
        return result;
    }

    /**
     * 发送消息的具体实现
     *
     * @param sessions Session 列表
     * @param message 消息
     */
    public void doSend(Collection<WebSocketSession> sessions, WebSocketPushMessage message) {
        JsonWebSocketMessage jsonMessage = new JsonWebSocketMessage().setType(message.getMessageType())
                .setContent(message.getMessageContent());
        String payload = JsonUtils.toJsonString(jsonMessage); // 关键，使用 JSON 序列化
        sessions.forEach(session -> {
            // 1. 各种校验，保证 Session 可以被发送
            if (session == null) {
                log.error("[doSend][session 为空, message({})]", jsonMessage);
                return;
            }
            if (!session.isOpen()) {
                log.error("[doSend][session({}) 已关闭, message({})]", session.getId(), jsonMessage);
                return;
            }
            // 2. 执行发送。并发由 ConcurrentWebSocketSessionDecorator 保证
            try {
                session.sendMessage(new TextMessage(payload));
                log.info("[doSend][session({}) 发送消息成功，message({})]", session.getId(), jsonMessage);
            } catch (IOException ex) {
                log.error("[doSend][session({}) 发送消息失败，message({})]", session.getId(), jsonMessage, ex);
            }
        });
    }

}
