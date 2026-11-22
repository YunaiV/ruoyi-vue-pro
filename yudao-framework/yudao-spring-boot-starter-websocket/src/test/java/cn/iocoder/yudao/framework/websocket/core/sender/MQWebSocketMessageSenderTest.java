package cn.iocoder.yudao.framework.websocket.core.sender;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.websocket.core.sender.kafka.KafkaWebSocketMessage;
import cn.iocoder.yudao.framework.websocket.core.sender.kafka.KafkaWebSocketMessageConsumer;
import cn.iocoder.yudao.framework.websocket.core.sender.kafka.KafkaWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessage;
import cn.iocoder.yudao.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessageConsumer;
import cn.iocoder.yudao.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.sender.rocketmq.RocketMQWebSocketMessage;
import cn.iocoder.yudao.framework.websocket.core.sender.rocketmq.RocketMQWebSocketMessageConsumer;
import cn.iocoder.yudao.framework.websocket.core.sender.rocketmq.RocketMQWebSocketMessageSender;
import cn.iocoder.yudao.framework.websocket.core.session.WebSocketSessionManagerImpl;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * RocketMQ、Kafka、RabbitMQ 的 {@link WebSocketMessageSender} 单元测试
 *
 * @author 芋道源码
 */
@SuppressWarnings("unchecked")
public class MQWebSocketMessageSenderTest {

    private WebSocketSession session;
    private WebSocketSessionManagerImpl webSocketSessionManager;

    @BeforeEach
    public void setUp() {
        webSocketSessionManager = new WebSocketSessionManagerImpl();
        session = LocalWebSocketMessageSenderTest.mockSession("ws-1", 1L, 1, 100L);
        webSocketSessionManager.addSession(session);
        TenantContextHolder.setTenantId(1L);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testSendAndConsume_rocketMQ() throws Exception {
        // 准备参数
        RocketMQTemplate template = mock(RocketMQTemplate.class);
        RocketMQWebSocketMessageSender sender = new RocketMQWebSocketMessageSender(webSocketSessionManager, template, "topic");
        RocketMQWebSocketMessageConsumer consumer = new RocketMQWebSocketMessageConsumer(sender);

        // 调用：发送只广播，不直接投递
        sender.send(1, 100L, "t", "c");
        // 断言
        ArgumentCaptor<RocketMQWebSocketMessage> captor = ArgumentCaptor.forClass(RocketMQWebSocketMessage.class);
        verify(template).syncSend(eq("topic"), captor.capture());
        RocketMQWebSocketMessage mqMessage = captor.getValue();
        assertNotNull(mqMessage.getEventId());
        assertEquals(1L, mqMessage.getTenantId());
        assertNotNull(mqMessage.getTimestamp());
        assertEquals(100L, mqMessage.getUserId());
        verify(session, never()).sendMessage(any());

        // 调用：消费后投递到本节点
        consumer.onMessage(mqMessage);
        // 断言
        verify(session).sendMessage(any());
    }

    @Test
    public void testSendAndConsume_kafka() throws Exception {
        // 准备参数
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);
        when(template.send(eq("topic"), any())).thenReturn(CompletableFuture.completedFuture(null));
        KafkaWebSocketMessageSender sender = new KafkaWebSocketMessageSender(webSocketSessionManager, template, "topic");
        KafkaWebSocketMessageConsumer consumer = new KafkaWebSocketMessageConsumer(sender);

        // 调用
        sender.publish(new WebSocketPushMessage().setUserType(1).setMessageType("t").setMessageContent("c"));
        // 断言
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(template).send(eq("topic"), captor.capture());
        KafkaWebSocketMessage mqMessage = (KafkaWebSocketMessage) captor.getValue();
        assertEquals(1L, mqMessage.getTenantId());
        assertNotNull(mqMessage.getEventId());
        verify(session, never()).sendMessage(any());

        // 调用
        consumer.onMessage(mqMessage);
        // 断言
        verify(session).sendMessage(any());
    }

    @Test
    public void testSend_kafkaFailed() {
        // 准备参数
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);
        when(template.send(eq("topic"), any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));
        KafkaWebSocketMessageSender sender = new KafkaWebSocketMessageSender(webSocketSessionManager, template, "topic");

        // 调用，并断言：沿用原有行为，只记录日志
        assertDoesNotThrow(() -> sender.send(1, 100L, "t", "c"));
    }

    @Test
    public void testSendAndConsume_rabbitMQ() throws Exception {
        // 准备参数
        RabbitTemplate template = mock(RabbitTemplate.class);
        TopicExchange exchange = new TopicExchange("exchange");
        RabbitMQWebSocketMessageSender sender = new RabbitMQWebSocketMessageSender(webSocketSessionManager, template, exchange);
        RabbitMQWebSocketMessageConsumer consumer = new RabbitMQWebSocketMessageConsumer(sender);

        // 调用
        sender.send("ws-1", "t", "c");
        // 断言
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(eq("exchange"), (String) isNull(), captor.capture());
        RabbitMQWebSocketMessage mqMessage = (RabbitMQWebSocketMessage) captor.getValue();
        assertEquals("ws-1", mqMessage.getSessionId());
        assertNotNull(mqMessage.getEventId());

        // 调用
        consumer.onMessage(mqMessage);
        // 断言
        verify(session).sendMessage(any());
    }

    @Test
    public void testConsume_legacyMessage() throws Exception {
        // 准备参数：旧版本节点发出的消息，没有 eventId、tenantId、timestamp
        RocketMQWebSocketMessageSender sender = new RocketMQWebSocketMessageSender(webSocketSessionManager, mock(RocketMQTemplate.class), "topic");
        RocketMQWebSocketMessageConsumer consumer = new RocketMQWebSocketMessageConsumer(sender);
        RocketMQWebSocketMessage legacy = new RocketMQWebSocketMessage();
        legacy.setUserType(1);
        legacy.setUserId(100L);
        legacy.setMessageType("t");
        legacy.setMessageContent("c");

        // 调用
        consumer.onMessage(legacy);
        // 断言：租户取自 MQ 租户拦截器恢复的上下文
        verify(session).sendMessage(any());
    }

}
