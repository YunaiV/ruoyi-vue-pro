package cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotWebSocketConnectionManager} 的单元测试
 *
 * @author 芋道源码
 */
public class IotWebSocketConnectionManagerTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1024L;
    private static final byte[] BYTES = new byte[]{0x01, 0x02, 0x03};

    @Mock
    private ServerWebSocket socket;

    private IotWebSocketConnectionManager connectionManager;

    @BeforeEach
    public void setUp() {
        connectionManager = new IotWebSocketConnectionManager();
    }

    @Test
    public void testSendBinaryToDeviceAsync_success() {
        // mock 方法：设备在线，writeBinaryMessage 成功
        registerConnection();
        when(socket.writeBinaryMessage(any(Buffer.class))).thenReturn(Future.succeededFuture());

        // 调用
        Future<Void> future = connectionManager.sendBinaryToDeviceAsync(DEVICE_ID, BYTES);
        // 断言：以 binary 帧写出原始字节，不写 text 帧
        assertTrue(future.succeeded());
        ArgumentCaptor<Buffer> dataCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(socket).writeBinaryMessage(dataCaptor.capture());
        assertArrayEquals(BYTES, dataCaptor.getValue().getBytes());
        verify(socket, never()).writeTextMessage(anyString());
    }

    @Test
    public void testSendBinaryToDeviceAsync_offline() {
        // 调用：设备未连接
        Future<Void> future = connectionManager.sendBinaryToDeviceAsync(DEVICE_ID, BYTES);
        // 断言
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
        verifyNoInteractions(socket);
    }

    @Test
    public void testSendBinaryToDeviceAsync_writeFailed() {
        // mock 方法：设备在线，但 writeBinaryMessage 异步失败（例如写出前连接已关闭）
        registerConnection();
        when(socket.writeBinaryMessage(any(Buffer.class)))
                .thenReturn(Future.failedFuture(new IllegalStateException("WebSocket is closed")));

        // 调用
        Future<Void> future = connectionManager.sendBinaryToDeviceAsync(DEVICE_ID, BYTES);
        // 断言：异步失败转成 106，而不是成功
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    private void registerConnection() {
        connectionManager.registerConnection(socket, DEVICE_ID, new IotWebSocketConnectionManager.ConnectionInfo()
                .setDeviceId(DEVICE_ID).setProductKey("pk").setDeviceName("dn"));
    }

    private static void assertFailed(Future<Void> future, ErrorCode errorCode) {
        assertTrue(future.failed());
        ServiceException exception = assertInstanceOf(ServiceException.class, future.cause());
        assertEquals(errorCode.getCode(), exception.getCode());
    }

}
