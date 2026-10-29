package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotEmqxProtocol} 的单元测试
 *
 * @author 芋道源码
 */
public class IotEmqxProtocolTest extends BaseMockitoUnitTest {

    private static final String RAW_PUSH_TOPIC = "/sys/pk/dn/thing/raw/push";
    private static final byte[] BYTES = new byte[]{0x01, 0x02, 0x03};

    @Mock
    private MqttClient mqttClient;

    private MockedStatic<SpringUtil> springUtilMockedStatic;
    private IotEmqxProtocol protocol;

    @BeforeEach
    public void setUp() {
        // 构造方法里的 handler 通过 SpringUtil 获取 Bean：这里不需要真实的 Bean
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        IotGatewayProperties.ProtocolProperties properties = new IotGatewayProperties.ProtocolProperties()
                .setId("emqx-test").setPort(18083).setEmqx(new IotEmqxConfig());
        protocol = new IotEmqxProtocol(properties);
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
    }

    @Test
    public void testPublishMessageAsync_success() {
        // mock 方法：已连接，publish 成功
        ReflectUtil.setFieldValue(protocol, "mqttClient", mqttClient);
        when(mqttClient.isConnected()).thenReturn(true);
        when(mqttClient.publish(eq(RAW_PUSH_TOPIC), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false)))
                .thenReturn(Future.succeededFuture(1));

        // 调用
        Future<Void> future = protocol.publishMessageAsync(RAW_PUSH_TOPIC, BYTES);
        // 断言
        assertTrue(future.succeeded());
        ArgumentCaptor<Buffer> payloadCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(mqttClient).publish(eq(RAW_PUSH_TOPIC), payloadCaptor.capture(), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false));
        assertArrayEquals(BYTES, payloadCaptor.getValue().getBytes());
    }

    @Test
    public void testPublishMessageAsync_notConnected() {
        // 调用：MQTT Client 未创建
        Future<Void> future = protocol.publishMessageAsync(RAW_PUSH_TOPIC, BYTES);
        // 断言
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);

        // mock 方法：MQTT Client 已断开
        ReflectUtil.setFieldValue(protocol, "mqttClient", mqttClient);
        when(mqttClient.isConnected()).thenReturn(false);
        // 调用，并断言
        assertFailed(protocol.publishMessageAsync(RAW_PUSH_TOPIC, BYTES), DEVICE_OFFLINE_OR_WRITE_FAILED);
        verify(mqttClient, never()).publish(any(), any(Buffer.class), any(), anyBoolean(), anyBoolean());
    }

    @Test
    public void testPublishMessageAsync_publishFailed() {
        // mock 方法：已连接，但 publish 异步失败
        ReflectUtil.setFieldValue(protocol, "mqttClient", mqttClient);
        when(mqttClient.isConnected()).thenReturn(true);
        when(mqttClient.publish(eq(RAW_PUSH_TOPIC), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false)))
                .thenReturn(Future.failedFuture(new IllegalStateException("Connection is closed")));

        // 调用
        Future<Void> future = protocol.publishMessageAsync(RAW_PUSH_TOPIC, BYTES);
        // 断言：异步失败转成 106，而不是成功
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    private static void assertFailed(Future<Void> future, ErrorCode errorCode) {
        assertTrue(future.failed());
        ServiceException exception = assertInstanceOf(ServiceException.class, future.cause());
        assertEquals(errorCode.getCode(), exception.getCode());
    }

}
