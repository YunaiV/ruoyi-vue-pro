package cn.iocoder.yudao.module.iot.core.messagebus.core.kafka;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageRejectedException;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * {@link IotKafkaMessageBus} 确认发送的单元测试：KafkaTemplate 为 mock，不连接 Kafka
 *
 * @author 芋道源码
 */
public class IotKafkaMessageBusTest {

    private static final String TOPIC = "test-topic";

    @Test
    public void testPostConfirmed_recordTooLarge() {
        // 准备参数：与实测一致，send 同步抛出 KafkaException("Send failed")，原因是 RecordTooLargeException
        KafkaTemplate<String, String> kafkaTemplate = mockKafkaTemplate();
        when(kafkaTemplate.send(anyString(), anyString())).thenThrow(new KafkaException("Send failed",
                new RecordTooLargeException("The message is 1100291 bytes when serialized which is larger than 1048576")));
        IotKafkaMessageBus messageBus = buildMessageBus(new KafkaProperties(), kafkaTemplate);

        // 调用，并断言：永久拒绝
        IotMessageRejectedException ex = assertThrows(IotMessageRejectedException.class,
                () -> messageBus.postConfirmed(TOPIC, "message"));
        assertInstanceOf(KafkaException.class, ex.getCause());
    }

    @Test
    public void testPostConfirmed_recordTooLargeAsync() {
        // 准备参数：broker 拒绝（message.max.bytes），send 的结果异常完成
        KafkaTemplate<String, String> kafkaTemplate = mockKafkaTemplate();
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.completeExceptionally(new KafkaException("Send failed", new RecordTooLargeException("too large")));
        doReturn(future).when(kafkaTemplate).send(anyString(), anyString());
        IotKafkaMessageBus messageBus = buildMessageBus(new KafkaProperties(), kafkaTemplate);

        // 调用，并断言：原因链中有 RecordTooLargeException，同样永久拒绝
        assertThrows(IotMessageRejectedException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
    }

    @Test
    public void testPostConfirmed_otherFailure() {
        // 准备参数：超时等其它失败
        KafkaTemplate<String, String> kafkaTemplate = mockKafkaTemplate();
        when(kafkaTemplate.send(anyString(), anyString())).thenThrow(new KafkaException("Send failed",
                new TimeoutException("Topic not present in metadata after 5000 ms")));
        IotKafkaMessageBus messageBus = buildMessageBus(new KafkaProperties(), kafkaTemplate);

        // 调用，并断言：按未确认抛出，不是永久拒绝
        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
        assertFalse(ex instanceof IotMessageRejectedException);
    }

    @Test
    public void testValidateConfirmable_acksZero() {
        // 准备参数：acks=0
        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.getProducer().setAcks("0");
        KafkaTemplate<String, String> kafkaTemplate = mockKafkaTemplate();
        IotKafkaMessageBus messageBus = buildMessageBus(kafkaProperties, kafkaTemplate);

        // 调用，并断言：不能确认发送，也不发送
        assertThrows(IllegalStateException.class, messageBus::validateConfirmable);
        assertThrows(IllegalStateException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    public void testValidateConfirmable_acksDefault() {
        // 调用，并断言：未配置时使用 kafka-clients 的默认值，可以确认发送
        assertDoesNotThrow(() -> buildMessageBus(new KafkaProperties(), mockKafkaTemplate()).validateConfirmable());
    }

    @SuppressWarnings("unchecked")
    private static KafkaTemplate<String, String> mockKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    private static IotKafkaMessageBus buildMessageBus(KafkaProperties kafkaProperties,
                                                      KafkaTemplate<String, String> kafkaTemplate) {
        // 构造时只创建生产者工厂，不连接 Kafka；替换为 mock 的 KafkaTemplate
        IotKafkaMessageBus messageBus = new IotKafkaMessageBus(kafkaProperties);
        ReflectUtil.setFieldValue(messageBus, "kafkaTemplate", kafkaTemplate);
        return messageBus;
    }

}
