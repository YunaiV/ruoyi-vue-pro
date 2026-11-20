package cn.iocoder.yudao.module.iot.core.messagebus.core.rocketmq;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageRejectedException;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessagingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link IotRocketMQMessageBus} 确认发送的单元测试：RocketMQTemplate 为 mock，不连接 RocketMQ
 *
 * @author 芋道源码
 */
public class IotRocketMQMessageBusConfirmTest {

    private static final String TOPIC = "test-topic";

    private final RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
    private final IotRocketMQMessageBus messageBus = new IotRocketMQMessageBus(new RocketMQProperties(), rocketMQTemplate);

    @Test
    public void testPostConfirmed_success() {
        // mock 方法
        SendResult result = new SendResult();
        result.setSendStatus(SendStatus.SEND_OK);
        when(rocketMQTemplate.syncSend(anyString(), (Object) any())).thenReturn(result);

        // 调用，并断言
        assertDoesNotThrow(() -> messageBus.postConfirmed(TOPIC, "message"));
    }

    @Test
    public void testPostConfirmed_notSendOk() {
        // mock 方法：刷盘超时
        SendResult result = new SendResult();
        result.setSendStatus(SendStatus.FLUSH_DISK_TIMEOUT);
        when(rocketMQTemplate.syncSend(anyString(), (Object) any())).thenReturn(result);

        // 调用，并断言：按未确认抛出
        assertThrows(IllegalStateException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
    }

    @Test
    public void testPostConfirmed_messageIllegal() {
        // mock 方法：与 RocketMQTemplate 一致，客户端校验失败（消息体超过 max-message-size）包装为 MessagingException
        when(rocketMQTemplate.syncSend(anyString(), (Object) any())).thenThrow(new MessagingException("syncSend failed",
                new MQClientException(ResponseCode.MESSAGE_ILLEGAL, "the message body size over max value")));

        // 调用，并断言：永久拒绝
        assertThrows(IotMessageRejectedException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
    }

    @Test
    public void testPostConfirmed_brokerMessageIllegal() {
        // mock 方法：broker 返回 MESSAGE_ILLEGAL（消息体超过 broker 的 maxMessageSize、属性超限），包装为 MessagingException
        when(rocketMQTemplate.syncSend(anyString(), (Object) any())).thenThrow(new MessagingException("syncSend failed",
                new MQBrokerException(ResponseCode.MESSAGE_ILLEGAL, "the message body size over max value", "127.0.0.1:10911")));

        // 调用，并断言：永久拒绝
        assertThrows(IotMessageRejectedException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
    }

    @Test
    public void testPostConfirmed_brokerSystemBusy() {
        // mock 方法：broker 繁忙（可用性问题）
        when(rocketMQTemplate.syncSend(anyString(), (Object) any())).thenThrow(new MessagingException("syncSend failed",
                new MQBrokerException(ResponseCode.SYSTEM_BUSY, "broker busy", "127.0.0.1:10911")));

        // 调用，并断言：按未确认抛出，不是永久拒绝
        MessagingException ex = assertThrows(MessagingException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
        assertInstanceOf(MQBrokerException.class, ex.getCause());
    }

    @Test
    public void testPostConfirmed_otherClientException() {
        // mock 方法：其它客户端异常（例如说找不到路由）
        when(rocketMQTemplate.syncSend(anyString(), (Object) any())).thenThrow(new MessagingException("syncSend failed",
                new MQClientException(ResponseCode.TOPIC_NOT_EXIST, "No route info of this topic")));

        // 调用，并断言：按未确认抛出，不是永久拒绝
        MessagingException ex = assertThrows(MessagingException.class, () -> messageBus.postConfirmed(TOPIC, "message"));
        assertInstanceOf(MQClientException.class, ex.getCause());
    }

}
