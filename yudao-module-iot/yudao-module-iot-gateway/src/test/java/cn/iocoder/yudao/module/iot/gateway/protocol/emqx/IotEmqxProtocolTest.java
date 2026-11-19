package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.messages.MqttConnAckMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

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
    @Mock
    private MqttClient replacementMqttClient;

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

    @Test
    public void testPublishMessage_clientClearedConcurrently() {
        // mock 方法：连接检查通过之后，并发的 stop / 重连清空了 mqttClient 字段
        ReflectUtil.setFieldValue(protocol, "mqttClient", mqttClient);
        when(mqttClient.isConnected()).thenAnswer(invocation -> {
            ReflectUtil.setFieldValue(protocol, "mqttClient", null);
            return true;
        });
        when(mqttClient.publish(eq(RAW_PUSH_TOPIC), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false)))
                .thenReturn(Future.succeededFuture(1));

        // 调用：不抛出 NPE
        assertDoesNotThrow(() -> protocol.publishMessage(RAW_PUSH_TOPIC, BYTES));
        // 断言：publish 使用的是做过连接检查的同一个客户端
        verify(mqttClient).publish(eq(RAW_PUSH_TOPIC), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false));
    }

    @Test
    public void testConnectMqttClient_connectThrows() throws Exception {
        // mock 方法：connect 同步抛错
        when(mqttClient.connect(anyInt(), any())).thenThrow(new IllegalStateException("Vertx 已关闭"));
        ReflectUtil.setFieldValue(protocol, "mqttClient", mqttClient);

        // 调用，并断言：按普通连接失败返回，客户端留给定时重连处理
        assertFalse(connectMqttClient(mqttClient));
        assertSame(mqttClient, ReflectUtil.getFieldValue(protocol, "mqttClient"));
    }

    @Test
    public void testConnectMqttClient_interruptedThenLateSuccess() {
        // mock 方法：本次客户端的连接一直未完成
        Promise<MqttConnAckMessage> connectPromise = Promise.promise();
        when(mqttClient.connect(anyInt(), any())).thenReturn(connectPromise.future());
        when(mqttClient.disconnect()).thenReturn(Future.succeededFuture());
        ReflectUtil.setFieldValue(protocol, "mqttClient", mqttClient);

        // 调用：连接等待被中断，向调用方传播原 InterruptedException
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedException.class, () -> connectMqttClient(mqttClient));
        } finally {
            Thread.interrupted();
        }
        // 断言：废弃本次客户端，并立即断开仍在等待 CONNACK 的连接
        assertNull(ReflectUtil.getFieldValue(protocol, "mqttClient"));
        verify(mqttClient, times(1)).disconnect();

        // 调用：替代客户端已就位，旧客户端的异步连接在等待结束后才成功
        ReflectUtil.setFieldValue(protocol, "mqttClient", replacementMqttClient);
        connectPromise.complete();
        // 断言：迟到成功后再次断开旧客户端，不误关替代客户端
        verify(mqttClient, times(2)).disconnect();
        verifyNoInteractions(replacementMqttClient);
        assertSame(replacementMqttClient, ReflectUtil.getFieldValue(protocol, "mqttClient"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCloseHandler_staleClientNotReconnect() throws Exception {
        // 准备参数：运行中，当前客户端已注册 closeHandler
        Vertx vertx = mock(Vertx.class);
        ReflectUtil.setFieldValue(protocol, "vertx", vertx);
        ReflectUtil.setFieldValue(protocol, "running", true);
        ReflectUtil.setFieldValue(protocol, "mqttClient", mqttClient);
        ReflectUtil.invoke(protocol, "setupMqttClientHandlers", mqttClient);
        ArgumentCaptor<Handler<Void>> closeHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
        verify(mqttClient).closeHandler(closeHandlerCaptor.capture());
        Handler<Void> closeHandler = closeHandlerCaptor.getValue();

        // 调用：当前客户端断开，重连任务排队期间，被替换为未连接的替代客户端
        closeHandler.handle(null);
        ArgumentCaptor<Callable<Object>> taskCaptor = ArgumentCaptor.forClass(Callable.class);
        verify(vertx).executeBlocking(taskCaptor.capture());
        ReflectUtil.setFieldValue(protocol, "mqttClient", replacementMqttClient);
        taskCaptor.getValue().call();
        // 断言：过期任务不重连，不触碰替代客户端
        verifyNoInteractions(replacementMqttClient);
        assertSame(replacementMqttClient, ReflectUtil.getFieldValue(protocol, "mqttClient"));

        // 调用：替代客户端就位之后，旧客户端的断开事件才到达
        closeHandler.handle(null);
        // 断言：不再提交重连任务
        verify(vertx, times(1)).executeBlocking(any(Callable.class));
        verifyNoInteractions(replacementMqttClient);
    }

    /**
     * 调用私有的 connectMqttClient：透传它抛出的原始异常
     */
    private boolean connectMqttClient(MqttClient client) throws Exception {
        Method method = IotEmqxProtocol.class.getDeclaredMethod("connectMqttClient", MqttClient.class);
        method.setAccessible(true);
        try {
            return (boolean) method.invoke(protocol, client);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static void assertFailed(Future<Void> future, ErrorCode errorCode) {
        assertTrue(future.failed());
        ServiceException exception = assertInstanceOf(ServiceException.class, future.cause());
        assertEquals(errorCode.getCode(), exception.getCode());
    }

}
