package cn.iocoder.yudao.module.iot.core.messagebus.core.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * {@link IotRabbitMQMessageBus} 确认发送的单元测试：连接工厂为 mock，不连接 RabbitMQ
 *
 * @author 芋道源码
 */
public class IotRabbitMQMessageBusConfirmTest {

    @Test
    public void testValidateConfirmable_success() {
        // 调用，并断言：已开启 confirm、return，超时时间大于 0
        assertDoesNotThrow(() -> buildMessageBus(mockConnectionFactory(), Duration.ofSeconds(5)).validateConfirmable());
    }

    @Test
    public void testValidateConfirmable_timeoutNotPositive() {
        for (Duration confirmTimeout : new Duration[]{Duration.ZERO, Duration.ofMillis(-1), Duration.ofNanos(1)}) {
            // 准备参数：确认超时时间不大于 0 毫秒
            ConnectionFactory connectionFactory = mockConnectionFactory();
            IotRabbitMQMessageBus messageBus = buildMessageBus(connectionFactory, confirmTimeout);

            // 调用，并断言：不能确认发送，也不发送
            assertThrows(IllegalStateException.class, messageBus::validateConfirmable);
            assertThrows(IllegalStateException.class, () -> messageBus.postConfirmed("test-topic", "message"));
            verify(connectionFactory, never()).createConnection();
        }
    }

    private static ConnectionFactory mockConnectionFactory() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.isPublisherConfirms()).thenReturn(true);
        when(connectionFactory.isPublisherReturns()).thenReturn(true);
        return connectionFactory;
    }

    private static IotRabbitMQMessageBus buildMessageBus(ConnectionFactory connectionFactory, Duration confirmTimeout) {
        return new IotRabbitMQMessageBus(new RabbitTemplate(connectionFactory), mock(RabbitAdmin.class), confirmTimeout);
    }

}
