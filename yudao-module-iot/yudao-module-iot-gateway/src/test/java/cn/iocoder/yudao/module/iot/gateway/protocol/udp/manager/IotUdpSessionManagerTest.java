package cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.net.InetSocketAddress;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotUdpSessionManager} 的单元测试
 *
 * @author 芋道源码
 */
public class IotUdpSessionManagerTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1024L;
    private static final byte[] DATA = new byte[]{0x49, 0x52, 0x01, 0x00, 0x00, 0x01};
    private static final InetSocketAddress ADDRESS = new InetSocketAddress("127.0.0.1", 18093);

    @Mock
    private DatagramSocket socket;

    private IotUdpSessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        sessionManager = new IotUdpSessionManager(10, 60000L);
    }

    @Test
    public void testSendToDeviceAsync_success() {
        // mock 方法：会话存在，send 成功
        registerSession();
        when(socket.send(any(Buffer.class), anyInt(), anyString())).thenReturn(Future.succeededFuture());

        // 调用
        Future<Void> future = sessionManager.sendToDeviceAsync(DEVICE_ID, DATA, socket);
        // 断言：发送到会话地址
        assertTrue(future.succeeded());
        ArgumentCaptor<Buffer> dataCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(socket).send(dataCaptor.capture(), eq(ADDRESS.getPort()), eq(ADDRESS.getHostString()));
        assertArrayEquals(DATA, dataCaptor.getValue().getBytes());
    }

    @Test
    public void testSendToDeviceAsync_notWait() {
        // mock 方法：会话存在，send 一直未完成
        registerSession();
        Promise<Void> promise = Promise.promise();
        when(socket.send(any(Buffer.class), anyInt(), anyString())).thenReturn(promise.future());

        // 调用：不等待发送结果，立即返回未完成的 Future（不走 sendToDevice 的 5 秒同步等待）
        Future<Void> future = sessionManager.sendToDeviceAsync(DEVICE_ID, DATA, socket);
        assertFalse(future.isComplete());
        // 发送失败后，转成 106
        promise.fail(new IllegalStateException("Socket closed"));
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    @Test
    public void testSendToDeviceAsync_sessionNotExists() {
        // 调用：会话不存在（或已过期）
        Future<Void> future = sessionManager.sendToDeviceAsync(DEVICE_ID, DATA, socket);
        // 断言
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
        verifyNoInteractions(socket);
    }

    @Test
    public void testSendToDeviceAsync_socketNotAvailable() {
        // mock 方法：会话存在，但 UDP Socket 不可用
        registerSession();

        // 调用，并断言
        assertFailed(sessionManager.sendToDeviceAsync(DEVICE_ID, DATA, null), DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    @Test
    public void testSendToDeviceAsync_sendFailed() {
        // mock 方法：会话存在，但 send 异步失败
        registerSession();
        when(socket.send(any(Buffer.class), anyInt(), anyString()))
                .thenReturn(Future.failedFuture(new IllegalStateException("Socket closed")));

        // 调用
        Future<Void> future = sessionManager.sendToDeviceAsync(DEVICE_ID, DATA, socket);
        // 断言：异步失败转成 106，而不是成功
        assertFailed(future, DEVICE_OFFLINE_OR_WRITE_FAILED);
    }

    private void registerSession() {
        sessionManager.registerSession(DEVICE_ID, new IotUdpSessionManager.SessionInfo()
                .setDeviceId(DEVICE_ID).setProductKey("pk").setDeviceName("dn").setAddress(ADDRESS));
    }

    private static void assertFailed(Future<Void> future, ErrorCode errorCode) {
        assertTrue(future.failed());
        ServiceException exception = assertInstanceOf(ServiceException.class, future.cause());
        assertEquals(errorCode.getCode(), exception.getCode());
    }

}
