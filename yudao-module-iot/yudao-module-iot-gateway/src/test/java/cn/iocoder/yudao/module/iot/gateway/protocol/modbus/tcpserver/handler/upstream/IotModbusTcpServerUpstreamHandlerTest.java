package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.upstream;

import cn.hutool.core.util.HexUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager.PendingRequest;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerTransactionChannel;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpServerUpstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpServerUpstreamHandlerTest extends BaseMockitoUnitTest {

    private static final long TIMEOUT_SECONDS = 10;
    private static final Long DEVICE_ID = 1L;
    private static final Long POINT_ID = 10L;
    private static final int SLAVE_ID = 1;
    private static final String PRODUCT_KEY = "pk";
    private static final String DEVICE_NAME = "dn";
    private static final String SERVER_ID = "modbus-tcp-server";

    @Mock
    private IotDeviceCommonApi deviceApi;
    @Mock
    private IotDeviceMessageService messageService;
    @Mock
    private IotModbusTcpServerConfigCacheService configCacheService;
    @Mock
    private IotModbusTcpServerPollScheduler pollScheduler;
    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotModbusTcpServerTransactionChannel channel;
    @Mock
    private NetSocket socket;

    private final IotModbusTcpServerConnectionManager connectionManager = new IotModbusTcpServerConnectionManager();
    private final IotModbusTcpServerPendingRequestManager pendingRequestManager = new IotModbusTcpServerPendingRequestManager();
    private IotModbusTcpServerUpstreamHandler upstreamHandler;
    private ConnectionInfo connectionInfo;
    private Vertx vertx;
    /**
     * 连接的 owner（event loop Context）
     */
    private Context owner;

    @BeforeEach
    public void setUp() {
        upstreamHandler = new IotModbusTcpServerUpstreamHandler(deviceApi, messageService, new IotModbusFrameEncoder(65),
                connectionManager, configCacheService, pendingRequestManager, pollScheduler, deviceService,
                SERVER_ID, new AtomicInteger(), 60_000);
        // 已认证的 RTU 连接
        connectionInfo = new ConnectionInfo().setDeviceId(DEVICE_ID).setProductKey(PRODUCT_KEY).setDeviceName(DEVICE_NAME)
                .setSlaveId(SLAVE_ID).setFrameFormat(IotModbusFrameFormatEnum.MODBUS_RTU).setChannel(channel);
        connectionManager.registerConnection(socket, connectionInfo);
        vertx = Vertx.vertx();
        owner = vertx.getOrCreateContext();
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    public void testHandleFrame_pollingReportNotBlockOwner() throws Exception {
        // 准备参数：本连接的轮询请求 P（FC03 读 1 个）、透传请求 R（FC04 读 1 个）都在等响应
        IotModbusPointRespDTO point = buildPoint();
        PendingRequest poll = buildPollRequest();
        PendingRequest raw = buildRawRequest();
        pendingRequestManager.addRequest(poll);
        pendingRequestManager.addRequest(raw);
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(owner);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch delivered = new CountDownLatch(1);
        AtomicBoolean onWorker = new AtomicBoolean();
        // mock 方法：事务通道的 worker 队列（在 owner 上）；点位配置；发送到消息总线阻塞，直到放行
        when(channel.getUpstreamQueue()).thenReturn(queue);
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(buildConfig(point));
        doAnswer(invocation -> {
            onWorker.set(Context.isOnWorkerThread());
            entered.countDown();
            if (!release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("等待放行超时");
            }
            delivered.countDown();
            return null;
        }).when(messageService).sendDeviceMessage(any(IotDeviceMessage.class), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));

        IotModbusFrame rawResponse = buildReadResponseFrame(4, 0x0001);
        try {
            // 调用：owner 上收到 P 的响应，发送阻塞在 worker 上
            runOnOwner(() -> upstreamHandler.handleFrame(socket, buildReadResponseFrame(3, 0x00FA),
                    IotModbusFrameFormatEnum.MODBUS_RTU));
            assertTrue(entered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            // 调用：发送仍阻塞时，owner 上收到 R 的响应，再执行一个哨兵任务
            runOnOwner(() -> upstreamHandler.handleFrame(socket, rawResponse, IotModbusFrameFormatEnum.MODBUS_RTU));
            Boolean sentinel = callOnOwner(() -> Boolean.TRUE);

            // 断言：owner 没有被发送阻塞：R 的响应照常匹配并交给事务通道收尾，哨兵及时执行
            verify(channel).onRawResponse(same(rawResponse), same(raw));
            assertTrue(sentinel);
            assertEquals(1, delivered.getCount());
        } finally {
            release.countDown();
        }
        // 断言：放行后属性上报只投递一次，在 worker 上发送
        assertTrue(delivered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(messageService, times(1)).sendDeviceMessage(captor.capture(), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));
        assertTrue(onWorker.get());
        assertEquals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), captor.getValue().getMethod());
        assertEquals(Collections.singletonMap("temperature", IotModbusCommonUtils.convertToPropertyValue(new int[]{0x00FA}, point)),
                captor.getValue().getParams());
    }

    @Test
    public void testSendOfflineMessage_afterPollingReport() throws Exception {
        // 准备参数：本连接的轮询请求 P
        pendingRequestManager.addRequest(buildPollRequest());
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(owner);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        // mock 方法：事务通道的 worker 队列；点位配置；第一条（属性上报）阻塞直到放行
        when(channel.getUpstreamQueue()).thenReturn(queue);
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(buildConfig(buildPoint()));
        doAnswer(invocation -> {
            entered.countDown();
            if (!release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("等待放行超时");
            }
            return null;
        }).doNothing().when(messageService).sendDeviceMessage(any(IotDeviceMessage.class),
                eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));

        Future<Void> offline;
        try {
            // 调用：owner 上收到 P 的响应，发送阻塞在 worker 上
            runOnOwner(() -> upstreamHandler.handleFrame(socket, buildReadResponseFrame(3, 0x00FA),
                    IotModbusFrameFormatEnum.MODBUS_RTU));
            assertTrue(entered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            // 调用：连接关闭（closeHandler 在 owner 上移除连接、提交下线消息），不等待发送
            offline = callOnOwner(() -> {
                connectionManager.removeConnection(socket);
                return upstreamHandler.sendOfflineMessage(connectionInfo);
            });

            // 断言：下线消息排在被阻塞的属性上报之后，还没有发送；之后本连接不再接收新任务
            assertFalse(offline.isComplete());
            verify(messageService, times(1)).sendDeviceMessage(any(), any(), any(), any());
            assertTrue(callOnOwner(() -> queue.submit(() -> null)).failed());
        } finally {
            release.countDown();
        }
        // 断言：放行后先发属性上报，再发下线消息，各一次
        await(offline);
        assertTrue(offline.succeeded());
        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(messageService, times(2)).sendDeviceMessage(captor.capture(), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));
        List<IotDeviceMessage> messages = captor.getAllValues();
        assertEquals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), messages.get(0).getMethod());
        assertEquals(IotDeviceMessage.buildStateOffline().getMethod(), messages.get(1).getMethod());
        assertEquals(IotDeviceMessage.buildStateOffline().getParams(), messages.get(1).getParams());
    }

    @Test
    public void testSendOfflineMessage_skipWhenReconnected() throws Exception {
        // 准备参数：旧连接关闭后，设备已在新连接上认证注册
        NetSocket newSocket = mock(NetSocket.class);
        connectionManager.removeConnection(socket);
        connectionManager.registerConnection(newSocket, new ConnectionInfo().setDeviceId(DEVICE_ID)
                .setProductKey(PRODUCT_KEY).setDeviceName(DEVICE_NAME).setSlaveId(SLAVE_ID)
                .setFrameFormat(IotModbusFrameFormatEnum.MODBUS_RTU).setChannel(mock(IotModbusTcpServerTransactionChannel.class)));
        // mock 方法：旧连接事务通道的 worker 队列
        when(channel.getUpstreamQueue()).thenReturn(new IotWorkerTaskQueue(owner));

        // 调用：旧连接的下线消息
        Future<Void> offline = callOnOwner(() -> upstreamHandler.sendOfflineMessage(connectionInfo));
        await(offline);

        // 断言：执行时设备已在新连接上线，不发送下线消息
        assertTrue(offline.succeeded());
        verify(messageService, never()).sendDeviceMessage(any(), any(), any(), any());
    }

    // ========== 辅助方法 ==========

    /**
     * 点位：FC03 读 1 个寄存器（INT16）
     */
    private static IotModbusPointRespDTO buildPoint() {
        IotModbusPointRespDTO point = new IotModbusPointRespDTO();
        point.setId(POINT_ID);
        point.setIdentifier("temperature");
        point.setFunctionCode(3);
        point.setRegisterAddress(0);
        point.setRegisterCount(1);
        point.setRawDataType("INT16");
        return point;
    }

    private static IotModbusDeviceConfigRespDTO buildConfig(IotModbusPointRespDTO point) {
        IotModbusDeviceConfigRespDTO config = new IotModbusDeviceConfigRespDTO();
        config.setDeviceId(DEVICE_ID);
        config.setPoints(Collections.singletonList(point));
        return config;
    }

    private PendingRequest buildPollRequest() {
        return new PendingRequest(DEVICE_ID, POINT_ID, "temperature", SLAVE_ID, 3, 0, 1,
                null, System.currentTimeMillis() + 60_000, channel, null);
    }

    private PendingRequest buildRawRequest() {
        IotModbusTx tx = IotModbusTx.ofClient(null, HexUtil.decodeHex("0400000001"),
                System.nanoTime() + TimeUnit.SECONDS.toNanos(60), null);
        return new PendingRequest(DEVICE_ID, null, null, SLAVE_ID, 4, 0, 1,
                null, System.currentTimeMillis() + 60_000, channel, tx);
    }

    /**
     * RTU 读响应帧：读 1 个寄存器（pdu = byteCount + 值）
     */
    private static IotModbusFrame buildReadResponseFrame(int functionCode, int value) {
        return new IotModbusFrame().setSlaveId(SLAVE_ID).setFunctionCode(functionCode)
                .setPdu(new byte[]{0x02, (byte) (value >> 8), (byte) value});
    }

    /**
     * 在 owner 上执行（与 RecordParser、closeHandler 回调一致），等待执行完；owner 被阻塞时超时失败
     */
    private void runOnOwner(Runnable action) throws Exception {
        callOnOwner(() -> {
            action.run();
            return null;
        });
    }

    private <T> T callOnOwner(Callable<T> action) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        owner.runOnContext(v -> {
            try {
                result.complete(action.call());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        });
        return result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 等待 Future 完成（成功或失败）
     */
    private static void await(Future<?> future) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        future.onComplete(ar -> latch.countDown());
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

}
