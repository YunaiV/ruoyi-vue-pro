package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotMqttDownstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotMqttDownstreamHandlerTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1024L;
    private static final String RAW_PUSH_TOPIC = "/sys/pk/dn/thing/raw/push";
    private static final String PROPERTY_SET_TOPIC = "/sys/pk/dn/thing/property/set";
    private static final byte[] BYTES = new byte[]{0x01, 0x02, 0x03};

    @Mock
    private IotDeviceMessageService deviceMessageService;
    @Mock
    private MqttEndpoint endpoint;

    private IotMqttConnectionManager connectionManager;
    private IotMqttDownstreamHandler downstreamHandler;

    @BeforeEach
    public void setUp() {
        connectionManager = new IotMqttConnectionManager();
        downstreamHandler = new IotMqttDownstreamHandler(deviceMessageService, connectionManager);
    }

    @Test
    public void testHandle_propertySet() {
        // mock 方法：设备在线，序列化成功
        registerConnection();
        IotDeviceMessage message = buildPropertySetMessage();
        byte[] payload = "{\"id\":\"m1\"}".getBytes();
        when(deviceMessageService.serializeDeviceMessage(message, "pk", "dn")).thenReturn(payload);
        when(endpoint.publish(anyString(), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false)))
                .thenReturn(Future.succeededFuture(1));

        // 调用
        downstreamHandler.handle(message);
        // 断言：发布到 property/set 主题，载荷为序列化结果
        ArgumentCaptor<Buffer> payloadCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(endpoint).publish(eq(PROPERTY_SET_TOPIC), payloadCaptor.capture(), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false));
        assertArrayEquals(payload, payloadCaptor.getValue().getBytes());
    }

    @Test
    public void testHandle_offline() {
        // 调用：设备未连接
        downstreamHandler.handle(buildPropertySetMessage());
        // 断言：不序列化，也不发布
        verifyNoInteractions(deviceMessageService, endpoint);
    }

    @Test
    public void testHandle_serializeEmpty() {
        // mock 方法：设备在线，但序列化结果为空
        registerConnection();
        IotDeviceMessage message = buildPropertySetMessage();
        when(deviceMessageService.serializeDeviceMessage(message, "pk", "dn")).thenReturn(new byte[0]);

        // 调用：异常只记录日志，不向外抛出
        assertDoesNotThrow(() -> downstreamHandler.handle(message));
        // 断言：不发布
        verify(endpoint, never()).publish(anyString(), any(Buffer.class), any(MqttQoS.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void testHandleRaw_success() {
        // mock 方法：设备在线，publish 成功
        registerConnection();
        when(endpoint.publish(anyString(), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false)))
                .thenReturn(Future.succeededFuture(1));

        // 调用
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言：成功，并发布到 raw/push 主题，载荷不变
        assertTrue(future.succeeded());
        ArgumentCaptor<Buffer> payloadCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(endpoint).publish(eq(RAW_PUSH_TOPIC), payloadCaptor.capture(), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false));
        assertArrayEquals(BYTES, payloadCaptor.getValue().getBytes());
        verifyNoInteractions(deviceMessageService);
    }

    @Test
    public void testHandleRaw_offline() {
        // 调用：设备未连接
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    @Test
    public void testHandleRaw_writeFailed() {
        // mock 方法：设备在线，publish 已提交，写出结果稍后才返回
        registerConnection();
        Promise<Integer> write = Promise.promise();
        when(endpoint.publish(anyString(), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false)))
                .thenReturn(write.future());
        ListAppender<ILoggingEvent> appender = attachAppender();
        try {
            // 调用
            Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
            // 断言：不等待写出，提交即返回成功
            assertTrue(future.succeeded());
            assertTrue(getWarnMessages(appender).isEmpty());

            // 调用：写出失败（例如写出前连接已关闭）
            write.fail(new IllegalStateException("Connection is closed"));
            // 断言：晚到的写出失败记 warn 日志（设备、主题、原因）
            List<String> warnMessages = getWarnMessages(appender);
            assertEquals(1, warnMessages.size());
            assertTrue(warnMessages.get(0).contains("发送消息失败"));
            assertTrue(warnMessages.get(0).contains(String.valueOf(DEVICE_ID)));
            assertTrue(warnMessages.get(0).contains(RAW_PUSH_TOPIC));
            assertTrue(warnMessages.get(0).contains("Connection is closed"));
        } finally {
            detachAppender(appender);
        }
    }

    @Test
    public void testHandleRaw_publishThrows() {
        // mock 方法：设备在线，但 publish 同步抛出异常
        registerConnection();
        when(endpoint.publish(anyString(), any(Buffer.class), eq(MqttQoS.AT_LEAST_ONCE), eq(false), eq(false)))
                .thenThrow(new IllegalStateException("Connection not accepted yet"));

        // 调用
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    private void registerConnection() {
        connectionManager.registerConnection(endpoint, new IotMqttConnectionManager.ConnectionInfo()
                .setDeviceId(DEVICE_ID).setProductKey("pk").setDeviceName("dn").setRemoteAddress("127.0.0.1:1883"));
    }

    private static IotDeviceMessage buildMessage() {
        return new IotDeviceMessage().setId("m1").setDeviceId(DEVICE_ID)
                .setMethod(IotDeviceMessageMethodEnum.RAW_PUSH.getMethod());
    }

    private static IotDeviceMessage buildPropertySetMessage() {
        return new IotDeviceMessage().setId("m1").setDeviceId(DEVICE_ID)
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod());
    }

    private static ListAppender<ILoggingEvent> attachAppender() {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(IotMqttConnectionManager.class)).addAppender(appender);
        return appender;
    }

    private static void detachAppender(ListAppender<ILoggingEvent> appender) {
        ((Logger) LoggerFactory.getLogger(IotMqttConnectionManager.class)).detachAppender(appender);
        appender.stop();
    }

    private static List<String> getWarnMessages(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream().filter(event -> event.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage).collect(Collectors.toList());
    }

    private static void assertFailed(Future<Void> future, ErrorCode errorCode) {
        assertTrue(future.failed());
        ServiceException exception = assertInstanceOf(ServiceException.class, future.cause());
        assertEquals(errorCode.getCode(), exception.getCode());
    }

}
