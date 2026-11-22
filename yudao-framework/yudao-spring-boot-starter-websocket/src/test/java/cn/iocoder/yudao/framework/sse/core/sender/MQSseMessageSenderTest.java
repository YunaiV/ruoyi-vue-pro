package cn.iocoder.yudao.framework.sse.core.sender;

import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.sse.core.message.SseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.kafka.KafkaSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.kafka.KafkaSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.sender.rabbitmq.RabbitMQSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.rabbitmq.RabbitMQSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.sender.redis.RedisSseMessage;
import cn.iocoder.yudao.framework.sse.core.sender.redis.RedisSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.redis.RedisSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.sender.rocketmq.RocketMQSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.rocketmq.RocketMQSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.session.RecordingSseEmitter;
import cn.iocoder.yudao.framework.sse.core.session.SseConnection;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.websocket.core.sender.redis.RedisWebSocketMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.AsyncResult;

import static cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManagerTest.newConnection;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Redis、RocketMQ、Kafka、RabbitMQ 的 {@link SseMessageSender} 单元测试
 *
 * @author 芋道源码
 */
@SuppressWarnings("unchecked")
public class MQSseMessageSenderTest {

    private SseEmitterSessionManager sessionManager;
    private SseConnection connection;

    @BeforeEach
    public void setUp() {
        sessionManager = new SseEmitterSessionManager(50L);
        connection = newConnection(1L, 2, 100L, null);
        sessionManager.register(connection);
        TenantContextHolder.setTenantId(1L);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testSendAndConsume_redis() {
        // 准备参数
        RedisMQTemplate template = mock(RedisMQTemplate.class);
        RedisSseMessageSender sender = new RedisSseMessageSender(sessionManager, 50L, 2, template);
        RedisSseMessageConsumer consumer = new RedisSseMessageConsumer(sender);

        // 调用：发送只广播，不直接投递
        String eventId = sender.send(null, 2, 100L, "t", "c");
        // 断言
        ArgumentCaptor<RedisSseMessage> captor = ArgumentCaptor.forClass(RedisSseMessage.class);
        verify(template).send(captor.capture());
        SseMessage message = captor.getValue().getMessage();
        assertEquals(eventId, message.getEventId());
        assertEquals(1L, message.getTenantId());
        assertEquals(2, message.getUserType());
        assertEquals(100L, message.getUserId());
        assertNotNull(message.getTimestamp());
        assertEquals(0, frameCount());

        // 调用：消费后投递到本节点
        consumer.onMessage(captor.getValue());
        // 断言
        assertEquals(1, frameCount());
    }

    @Test
    public void testGetChannel_redis() {
        // 调用，并断言：SSE 与 WebSocket 使用不同的 Redis Channel
        assertNotEquals(new RedisWebSocketMessage().getChannel(), new RedisSseMessage().getChannel());
    }

    @Test
    public void testSendAndConsume_rocketMQ() {
        // 准备参数
        RocketMQTemplate template = mock(RocketMQTemplate.class);
        RocketMQSseMessageSender sender = new RocketMQSseMessageSender(sessionManager, 50L, 2, template, "topic");
        RocketMQSseMessageConsumer consumer = new RocketMQSseMessageConsumer(sender);

        // 调用
        String eventId = sender.send(null, 2, 100L, "t", "c");
        // 断言
        ArgumentCaptor<SseMessage> captor = ArgumentCaptor.forClass(SseMessage.class);
        verify(template).syncSend(eq("topic"), captor.capture());
        assertEquals(eventId, captor.getValue().getEventId());
        assertEquals(1L, captor.getValue().getTenantId());
        assertEquals(0, frameCount());

        // 调用
        consumer.onMessage(captor.getValue());
        // 断言
        assertEquals(1, frameCount());
    }

    @Test
    public void testSendAndConsume_kafka() {
        // 准备参数
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);
        when(template.send(eq("topic"), any())).thenReturn(new AsyncResult<>(null));
        KafkaSseMessageSender sender = new KafkaSseMessageSender(sessionManager, 50L, 2, template, "topic");
        KafkaSseMessageConsumer consumer = new KafkaSseMessageConsumer(sender);

        // 调用
        String eventId = sender.send(null, 2, 100L, "t", "c");
        // 断言
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(template).send(eq("topic"), captor.capture());
        SseMessage message = (SseMessage) captor.getValue();
        assertEquals(eventId, message.getEventId());
        assertEquals(1L, message.getTenantId());

        // 调用
        consumer.onMessage(message);
        // 断言
        assertEquals(1, frameCount());
    }

    @Test
    public void testSend_kafkaFailed() {
        // 准备参数
        KafkaTemplate<Object, Object> template = mock(KafkaTemplate.class);
        when(template.send(eq("topic"), any())).thenReturn(AsyncResult.forExecutionException(new IllegalStateException("down")));
        KafkaSseMessageSender sender = new KafkaSseMessageSender(sessionManager, 50L, 2, template, "topic");

        // 调用，并断言：与 WebSocket 一致，只记录日志
        assertDoesNotThrow(() -> sender.send(2, 100L, "t", "c"));
    }

    @Test
    public void testSendAndConsume_rabbitMQ() {
        // 准备参数
        RabbitTemplate template = mock(RabbitTemplate.class);
        RabbitMQSseMessageSender sender = new RabbitMQSseMessageSender(sessionManager, 50L, 2, template,
                new TopicExchange("exchange"));
        RabbitMQSseMessageConsumer consumer = new RabbitMQSseMessageConsumer(sender);

        // 调用
        sender.send(connection.getConnectionId(), "t", "c");
        // 断言
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(eq("exchange"), (String) isNull(), captor.capture());
        SseMessage message = (SseMessage) captor.getValue();
        assertEquals(connection.getConnectionId(), message.getConnectionId());

        // 调用
        consumer.onMessage(message);
        // 断言
        assertEquals(1, frameCount());
    }

    private int frameCount() {
        return ((RecordingSseEmitter) connection.getEmitter()).frames.size();
    }

}
