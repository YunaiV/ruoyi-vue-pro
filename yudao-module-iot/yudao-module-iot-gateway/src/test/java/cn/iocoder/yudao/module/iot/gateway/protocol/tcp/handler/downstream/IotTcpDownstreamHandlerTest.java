package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.handler.downstream;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.RAW_NOT_SUPPORTED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotTcpDownstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotTcpDownstreamHandlerTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1024L;
    private static final byte[] BYTES = new byte[]{0x01, 0x02, 0x03};
    /**
     * 帧编码后的数据（模拟 codec 加了帧头）
     */
    private static final byte[] FRAME = new byte[]{0x00, 0x03, 0x01, 0x02, 0x03};

    @Mock
    private IotTcpFrameCodec codec;
    @Mock
    private IotMessageSerializer serializer;
    @Mock
    private NetSocket socket;
    @Mock
    private NetSocket jsonSocket;

    private IotTcpConnectionManager connectionManager;
    private IotTcpDownstreamHandler downstreamHandler;

    @BeforeEach
    public void setUp() {
        connectionManager = new IotTcpConnectionManager(10);
        downstreamHandler = new IotTcpDownstreamHandler(connectionManager, codec, serializer);
    }

    @Test
    public void testHandle_propertySet() {
        // mock 方法：普通连接，序列化 + 帧编码成功
        registerConnection(false);
        IotDeviceMessage message = buildPropertySetMessage();
        when(serializer.serialize(message)).thenReturn(BYTES);
        when(codec.encode(BYTES)).thenReturn(Buffer.buffer(FRAME));

        // 调用
        downstreamHandler.handle(message);
        // 断言：写出 codec 加帧后的数据
        ArgumentCaptor<Buffer> dataCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(socket).write(dataCaptor.capture());
        assertArrayEquals(FRAME, dataCaptor.getValue().getBytes());
    }

    @Test
    public void testHandle_notPropertySet() {
        // mock 方法：普通连接
        registerConnection(false);

        // 调用：非属性设置消息（例如服务调用）
        downstreamHandler.handle(new IotDeviceMessage().setId("m1").setDeviceId(DEVICE_ID)
                .setMethod(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod()));
        // 断言：忽略，不序列化也不写出
        verifyNoInteractions(serializer, codec);
        verify(socket, never()).write(any(Buffer.class));
    }

    @Test
    public void testHandle_offline() {
        // 调用：设备未连接
        downstreamHandler.handle(buildPropertySetMessage());
        // 断言：不序列化也不写出
        verifyNoInteractions(serializer, codec, socket);
    }

    @Test
    public void testHandleRaw_success() {
        // mock 方法：透传连接，write 成功
        registerConnection(true);
        when(codec.encode(BYTES)).thenReturn(Buffer.buffer(FRAME));
        when(socket.write(any(Buffer.class))).thenReturn(Future.succeededFuture());

        // 调用
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言：成功，并写出 codec 加帧后的数据
        assertTrue(future.succeeded());
        ArgumentCaptor<Buffer> dataCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(socket).write(dataCaptor.capture());
        assertArrayEquals(FRAME, dataCaptor.getValue().getBytes());
        verifyNoInteractions(serializer);
    }

    @Test
    public void testHandleRaw_offline() {
        // 调用：设备未连接
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
        verifyNoInteractions(codec);
    }

    @Test
    public void testHandleRaw_notRawConnection() {
        // mock 方法：普通（非透传）连接
        registerConnection(false);

        // 调用
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言：105，不写出
        assertFailed(future, RAW_NOT_SUPPORTED);
        verifyNoInteractions(codec);
        verify(socket, never()).write(any(Buffer.class));
    }

    @Test
    public void testHandleRaw_writeFailed() {
        // mock 方法：透传连接，但 write 异步失败（例如写出前对端已关闭）
        registerConnection(true);
        when(codec.encode(BYTES)).thenReturn(Buffer.buffer(FRAME));
        when(socket.write(any(Buffer.class))).thenReturn(Future.failedFuture(new IllegalStateException("Connection is closed")));

        // 调用
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言：异步失败转成 106，而不是成功
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    @Test
    public void testHandleRaw_encodeFailed() {
        // mock 方法：透传连接，但帧编码失败（例如超过固定长度）
        registerConnection(true);
        when(codec.encode(BYTES)).thenThrow(new IllegalArgumentException("数据长度 3 超过固定长度 2"));

        // 调用
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
        verify(socket, never()).write(any(Buffer.class));
    }

    @Test
    public void testHandleRaw_connectionReplaced() {
        // mock 方法：透传连接；检查之后、写出之前，设备换成了普通 JSON 连接
        registerConnection(true);
        when(codec.encode(BYTES)).thenAnswer(invocation -> {
            connectionManager.registerConnection(jsonSocket, DEVICE_ID, new IotTcpConnectionManager.ConnectionInfo()
                    .setDeviceId(DEVICE_ID).setProductKey("pk").setDeviceName("dn").setRawMode(false));
            return Buffer.buffer(FRAME);
        });

        // 调用
        Future<Void> future = downstreamHandler.handleRawMessage(buildMessage(), BYTES);
        // 断言：写出失败（106），不写到新的 JSON 连接上，也不重试
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
        verify(jsonSocket, never()).write(any(Buffer.class));
        verify(socket, never()).write(any(Buffer.class));
        assertSame(jsonSocket, connectionManager.getSocketByDeviceId(DEVICE_ID));
    }

    private void registerConnection(boolean rawMode) {
        connectionManager.registerConnection(socket, DEVICE_ID, new IotTcpConnectionManager.ConnectionInfo()
                .setDeviceId(DEVICE_ID).setProductKey("pk").setDeviceName("dn").setRawMode(rawMode));
    }

    private static IotDeviceMessage buildMessage() {
        return new IotDeviceMessage().setId("m1").setDeviceId(DEVICE_ID)
                .setMethod(IotDeviceMessageMethodEnum.RAW_PUSH.getMethod());
    }

    private static IotDeviceMessage buildPropertySetMessage() {
        return new IotDeviceMessage().setId("m1").setDeviceId(DEVICE_ID)
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod());
    }

    private static void assertFailed(Future<Void> future, ErrorCode errorCode) {
        assertTrue(future.failed());
        ServiceException exception = assertInstanceOf(ServiceException.class, future.cause());
        assertEquals(errorCode.getCode(), exception.getCode());
    }

}
