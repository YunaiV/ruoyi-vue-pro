package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream.IotModbusTcpServerDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager.PendingRequest;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link IotModbusTcpServerTransactionChannel} 的单元测试
 *
 * @author 芋道源码
 */
public class IotModbusTcpServerTransactionChannelTest {

    private static final Long DEVICE_ID = 1L;
    private static final int SLAVE_ID = 1;
    private static final int REQUEST_TIMEOUT = 60_000;
    private static final Long POINT_ID = 10L;

    private Vertx vertx;
    private Context owner;
    private NetSocket socket;
    private RecordingPendingRequestManager pendingRequestManager;

    /**
     * socket.write 被调用时所在的线程
     */
    private final List<Thread> writeThreads = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        owner = vertx.getOrCreateContext();
        socket = mock(NetSocket.class);
        when(socket.write(any(Buffer.class))).thenAnswer(invocation -> {
            writeThreads.add(Thread.currentThread());
            return Future.succeededFuture();
        });
        pendingRequestManager = new RecordingPendingRequestManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    // ========== 透传：提交与名额 ==========

    @Test
    public void testSubmitRaw_singleOwner() throws Exception {
        // 准备参数
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                new AtomicInteger(), REQUEST_TIMEOUT);
        Thread ownerThread = getOwnerThread();

        // 调用：4 个非 owner 线程并发提交，共 20 个透传
        List<Thread> submitters = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Thread submitter = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    channel.submitRaw(newTx(channel, new TxResult()));
                }
            });
            submitters.add(submitter);
            submitter.start();
        }
        for (Thread submitter : submitters) {
            submitter.join();
        }
        awaitOwner();

        // 断言：写出和登记待响应请求都只发生在 owner 上
        assertEquals(20, writeThreads.size());
        writeThreads.forEach(thread -> assertSame(ownerThread, thread));
        assertEquals(20, pendingRequestManager.addThreads.size());
        pendingRequestManager.addThreads.forEach(thread -> assertSame(ownerThread, thread));
    }

    @Test
    public void testSubmitRaw_queueFull() throws Exception {
        // 准备参数
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                new AtomicInteger(), REQUEST_TIMEOUT);
        TxResult result = new TxResult();

        // 调用：设备不回应，第 33 个时排队 + 在途已满
        for (int i = 0; i < IotModbusTcpServerTransactionChannel.MAX_RAW_OUTSTANDING + 1; i++) {
            channel.submitRaw(newTx(channel, result));
        }
        awaitOwner();

        // 断言
        assertEquals(IotModbusTcpServerTransactionChannel.MAX_RAW_OUTSTANDING, writeThreads.size());
        assertFailures(result, MODBUS_QUEUE_FULL);
    }

    @Test
    public void testAllocateTransactionId_skipPending() throws Exception {
        // 准备参数：计数器从 65535 绕回 0
        AtomicInteger transactionIdCounter = new AtomicInteger(65535);
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                transactionIdCounter, REQUEST_TIMEOUT);
        channel.submitRaw(newTx(channel, new TxResult()));
        awaitOwner();
        // 本连接还有一个 2 号的轮询请求
        pendingRequestManager.addRequest(buildPollRequest(channel, 2));

        // 调用 + 断言：0 号仍被在途透传占用，取下一个空闲编号 1
        assertEquals(0, pendingRequestManager.added.get(0).getTransactionId());
        assertEquals(0, captureWrites(1).get(0).getUnsignedShort(0));
        transactionIdCounter.set(65535);
        assertEquals(1, channel.allocateTransactionId());
        // 调用 + 断言：2 号被轮询请求占用，跳过
        assertEquals(3, channel.allocateTransactionId());
        // 调用 + 断言：RTU 帧格式没有事务号
        assertNull(createChannel(IotModbusFrameFormatEnum.MODBUS_RTU, transactionIdCounter, REQUEST_TIMEOUT)
                .allocateTransactionId());
    }

    // ========== 标准轮询、标准写：保持原逻辑，不受透传影响 ==========

    @Test
    public void testPollPoint_notLimitedByRawQueue() throws Exception {
        // 准备参数：RTU 下 1 个透传在途、31 个排队，第 33 个被拒
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_RTU,
                new AtomicInteger(), REQUEST_TIMEOUT);
        TxResult result = new TxResult();
        for (int i = 0; i < IotModbusTcpServerTransactionChannel.MAX_RAW_OUTSTANDING + 1; i++) {
            channel.submitRaw(newTx(channel, result));
        }
        awaitOwner();
        assertFailures(result, MODBUS_QUEUE_FULL);
        IotModbusTcpServerPollScheduler pollScheduler = createPollScheduler(channel, IotModbusFrameFormatEnum.MODBUS_RTU);

        // 调用：照常轮询两次
        pollScheduler.pollPoint(DEVICE_ID, POINT_ID);
        pollScheduler.pollPoint(DEVICE_ID, POINT_ID);

        // 断言：轮询不排队、不受透传名额限制，直接写出，并按原逻辑登记属于本连接的待响应请求
        assertEquals(3, writeThreads.size());
        List<PendingRequest> polls = pendingRequestManager.getPolls();
        assertEquals(2, polls.size());
        polls.forEach(poll -> assertSame(channel, poll.getChannel()));
    }

    @Test
    public void testOnCleanup_rtuTimeoutNotCloseAndPollContinues() throws Exception {
        // 准备参数：RTU 下轮询 P、透传 R 都已发出
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_RTU,
                new AtomicInteger(), REQUEST_TIMEOUT);
        IotModbusTcpServerPollScheduler pollScheduler = createPollScheduler(channel, IotModbusFrameFormatEnum.MODBUS_RTU);
        pollScheduler.pollPoint(DEVICE_ID, POINT_ID);
        TxResult result = new TxResult();
        channel.submitRaw(newTx(channel, result));
        awaitOwner();
        // 设备都不回应：两个待响应请求到期
        pendingRequestManager.added.forEach(request -> request.setExpireAt(System.currentTimeMillis() - 1));

        // 调用：同一个清理周期，先按原逻辑清理待响应请求，再通知通道；之后清理信号重复到达
        pendingRequestManager.cleanupExpired();
        channel.onCleanup();
        channel.onCleanup();
        awaitOwner();

        // 断言：R 只以 110 收尾一次，不断开连接
        assertFailures(result, MODBUS_TIMEOUT);
        verify(socket, never()).close();
        // 调用 + 断言：下一次轮询照常写出；R 的在途位置已释放，下一个透传也照常写出
        pollScheduler.pollPoint(DEVICE_ID, POINT_ID);
        channel.submitRaw(newTx(channel, new TxResult()));
        awaitOwner();
        assertEquals(4, writeThreads.size());
    }

    @Test
    public void testWriteProperty_logOnSocketWriteWithoutWaitingResponse() throws Exception {
        // 准备参数
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                new AtomicInteger(), REQUEST_TIMEOUT);
        IotModbusTcpServerConnectionManager connectionManager = createConnectionManager(channel,
                IotModbusFrameFormatEnum.MODBUS_TCP);
        IotModbusTcpServerDownstreamHandler downstreamHandler = new IotModbusTcpServerDownstreamHandler(
                connectionManager, mockConfigCacheService(), new IotModbusFrameEncoder(65));
        IotDeviceMessage message = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(),
                MapUtil.of("temperature", 220));
        message.setDeviceId(DEVICE_ID);

        // 调用
        downstreamHandler.handle(message);

        // 断言：写出 FC06 请求（socket 写出成功即记成功）；不登记待响应请求，不等待设备回显
        List<Buffer> writes = captureWrites(1);
        assertArrayEquals(HexUtil.decodeHex("06000000DC"), writes.get(0).getBytes(7, 12));
        assertTrue(pendingRequestManager.added.isEmpty());
    }

    // ========== 透传：响应关联 ==========

    @Test
    public void testOnRawResponse_tcpNormalAndException() throws Exception {
        // 准备参数：两个透传同时在途
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                new AtomicInteger(), REQUEST_TIMEOUT);
        TxResult result1 = new TxResult();
        TxResult result2 = new TxResult();
        channel.submitRaw(newTx(channel, result1));
        channel.submitRaw(newTx(channel, result2));
        awaitOwner();
        int transactionId1 = pendingRequestManager.added.get(0).getTransactionId();
        int transactionId2 = pendingRequestManager.added.get(1).getTransactionId();

        // 调用：先回第 2 个的异常响应，再回第 1 个的正常响应
        receive(channel, IotModbusFrameFormatEnum.MODBUS_TCP, exceptionFrame(transactionId2));
        receive(channel, IotModbusFrameFormatEnum.MODBUS_TCP, readResponseFrame(transactionId1));

        // 断言：按事务号各自回到原请求
        assertResponse(result1, "030400FA0258");
        assertResponse(result2, "8302");
    }

    @Test
    public void testOnRawResponse_rtuDeviceException() throws Exception {
        // 准备参数：RTU 下透传 R 在途，之后才发出轮询 P（同功能码）
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_RTU,
                new AtomicInteger(), REQUEST_TIMEOUT);
        TxResult result = new TxResult();
        channel.submitRaw(newTx(channel, result));
        awaitOwner();
        PendingRequest poll = buildPollRequest(channel, null);
        pendingRequestManager.addRequest(poll);

        // 调用：设备回 83 02
        receive(channel, IotModbusFrameFormatEnum.MODBUS_RTU, exceptionFrame(null));

        // 断言：R 排在最前面，设备的真实异常响应回到透传请求；轮询的待响应请求不被提前消费
        assertResponse(result, "8302");
        assertSame(poll, pendingRequestManager.matchResponse(DEVICE_ID, readResponseFrame(null),
                IotModbusFrameFormatEnum.MODBUS_RTU, channel));
    }

    @Test
    public void testOnRawResponse_rtuPollExceptionNotSkipped() throws Exception {
        // 准备参数：RTU 下轮询 P（同功能码）在前，透传 R 在途
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_RTU,
                new AtomicInteger(), REQUEST_TIMEOUT);
        PendingRequest poll = buildPollRequest(channel, null);
        pendingRequestManager.addRequest(poll);
        TxResult result = new TxResult();
        channel.submitRaw(newTx(channel, result));
        awaitOwner();

        // 调用：设备回 83 02（按 FIFO 是 P 的异常响应）
        AtomicReference<PendingRequest> matched = new AtomicReference<>();
        owner.runOnContext(v -> matched.set(pendingRequestManager.matchResponse(DEVICE_ID, exceptionFrame(null),
                IotModbusFrameFormatEnum.MODBUS_RTU, channel)));
        awaitOwner();

        // 断言：照旧丢弃，不跳过 P 去收尾 R；R 仍在途
        assertNull(matched.get());
        assertTrue(result.responses.isEmpty());
        assertTrue(result.failures.isEmpty());
        // 调用 + 断言：P 照旧按 FIFO 先出队；之后 R 的响应照常回到 R
        assertSame(poll, pendingRequestManager.matchResponse(DEVICE_ID, readResponseFrame(null),
                IotModbusFrameFormatEnum.MODBUS_RTU, channel));
        receive(channel, IotModbusFrameFormatEnum.MODBUS_RTU, readResponseFrame(null));
        assertResponse(result, "030400FA0258");
    }

    // ========== 透传：唯一终态 ==========

    @Test
    public void testOnRawWriteFailed_releaseOnceAndKeepConnection() throws Exception {
        // 准备参数：透传写出失败
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                new AtomicInteger(), REQUEST_TIMEOUT);
        when(socket.write(any(Buffer.class))).thenReturn(Future.failedFuture("写出失败"));
        TxResult result = new TxResult();

        // 调用
        channel.submitRaw(newTx(channel, result));
        awaitOwner();

        // 断言：只以 106 收尾一次，按引用移除待响应请求，不主动关闭连接
        assertFailures(result, DEVICE_OFFLINE_OR_WRITE_FAILED);
        assertFalse(pendingRequestManager.containsTransactionId(DEVICE_ID, channel,
                pendingRequestManager.added.get(0).getTransactionId()));
        verify(socket, never()).close();
        // 调用 + 断言：名额已释放，写出恢复后可以再提交满 MAX_RAW_OUTSTANDING 个
        when(socket.write(any(Buffer.class))).thenReturn(Future.succeededFuture());
        TxResult next = new TxResult();
        for (int i = 0; i < IotModbusTcpServerTransactionChannel.MAX_RAW_OUTSTANDING; i++) {
            channel.submitRaw(newTx(channel, next));
        }
        awaitOwner();
        assertTrue(next.failures.isEmpty());
    }

    @Test
    public void testOnCleanup_queuedExpired() throws Exception {
        // 准备参数：RTU 下 R1 在途、R2 排队，超时时间很短
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_RTU,
                new AtomicInteger(), 200);
        TxResult result1 = new TxResult();
        TxResult result2 = new TxResult();
        channel.submitRaw(newTx(channel, result1));
        channel.submitRaw(newTx(channel, result2));
        awaitOwner();
        Thread.sleep(300);

        // 调用：清理周期到达两次
        channel.onCleanup();
        channel.onCleanup();
        awaitOwner();

        // 断言：在途的 R1、排队的 R2 都只以 110 收尾一次；R2 从未写出
        assertFailures(result1, MODBUS_TIMEOUT);
        assertFailures(result2, MODBUS_TIMEOUT);
        assertEquals(1, writeThreads.size());
    }

    @Test
    public void testOnClose_idempotent() throws Exception {
        // 准备参数：RTU 下 A 在途，B、C 排队；本连接还有一个轮询请求
        IotModbusTcpServerTransactionChannel channel = createChannel(IotModbusFrameFormatEnum.MODBUS_RTU,
                new AtomicInteger(), REQUEST_TIMEOUT);
        pendingRequestManager.addRequest(buildPollRequest(channel, null));
        TxResult resultA = new TxResult();
        TxResult resultB = new TxResult();
        TxResult resultC = new TxResult();
        channel.submitRaw(newTx(channel, resultA));
        channel.submitRaw(newTx(channel, resultB));
        channel.submitRaw(newTx(channel, resultC));
        awaitOwner();

        // 调用：closeHandler 与主动关闭各通知一次，之后清理信号迟到
        channel.onClose();
        channel.onClose();
        channel.onCleanup();
        awaitOwner();

        // 断言：每个透传都只以 115 收尾一次；本连接的待响应请求全部清理
        for (TxResult result : Arrays.asList(resultA, resultB, resultC)) {
            assertFailures(result, MODBUS_DISCONNECTED);
        }
        verify(socket, times(1)).write(any(Buffer.class));
        assertNull(pendingRequestManager.matchResponse(DEVICE_ID, readResponseFrame(null),
                IotModbusFrameFormatEnum.MODBUS_RTU, channel));
        // 调用 + 断言：关闭之后提交的透传直接 115，不写出
        TxResult late = new TxResult();
        channel.submitRaw(newTx(channel, late));
        awaitOwner();
        assertFailures(late, MODBUS_DISCONNECTED);
        verify(socket, times(1)).write(any(Buffer.class));
    }

    @Test
    public void testOnClose_oldNotClearNew() throws Exception {
        // 准备参数：同一设备的旧连接 A、新连接 B 共享计数器，各有一个透传在途
        AtomicInteger transactionIdCounter = new AtomicInteger();
        IotModbusTcpServerTransactionChannel channelA = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                transactionIdCounter, REQUEST_TIMEOUT);
        IotModbusTcpServerTransactionChannel channelB = createChannel(IotModbusFrameFormatEnum.MODBUS_TCP,
                transactionIdCounter, REQUEST_TIMEOUT);
        TxResult resultA = new TxResult();
        TxResult resultB = new TxResult();
        channelA.submitRaw(newTx(channelA, resultA));
        channelB.submitRaw(newTx(channelB, resultB));
        awaitOwner();
        int transactionIdB = pendingRequestManager.added.get(1).getTransactionId();

        // 调用：旧连接 A 关闭
        channelA.onClose();
        awaitOwner();

        // 断言：A 以 115 收尾；B 的待响应请求仍在，响应照常回到 B 的透传
        assertFailures(resultA, MODBUS_DISCONNECTED);
        receive(channelB, IotModbusFrameFormatEnum.MODBUS_TCP, readResponseFrame(transactionIdB));
        assertResponse(resultB, "030400FA0258");
    }

    // ========== 辅助方法 ==========

    private IotModbusTcpServerTransactionChannel createChannel(IotModbusFrameFormatEnum frameFormat,
                                                               AtomicInteger transactionIdCounter,
                                                               int requestTimeout) {
        return new IotModbusTcpServerTransactionChannel(owner, socket, DEVICE_ID, SLAVE_ID, frameFormat,
                new IotModbusFrameEncoder(65), pendingRequestManager, transactionIdCounter, requestTimeout);
    }

    private IotModbusTcpServerConnectionManager createConnectionManager(IotModbusTcpServerTransactionChannel channel,
                                                                        IotModbusFrameFormatEnum frameFormat) {
        IotModbusTcpServerConnectionManager connectionManager = new IotModbusTcpServerConnectionManager();
        connectionManager.registerConnection(socket, new ConnectionInfo().setDeviceId(DEVICE_ID)
                .setSlaveId(SLAVE_ID).setFrameFormat(frameFormat).setChannel(channel));
        return connectionManager;
    }

    private IotModbusTcpServerPollScheduler createPollScheduler(IotModbusTcpServerTransactionChannel channel,
                                                                IotModbusFrameFormatEnum frameFormat) {
        return new IotModbusTcpServerPollScheduler(vertx, createConnectionManager(channel, frameFormat),
                new IotModbusFrameEncoder(65), pendingRequestManager, REQUEST_TIMEOUT, mockConfigCacheService());
    }

    /**
     * 设备配置：一个 FC03 读 1 个寄存器的点位（INT16，可写，单值用 FC06）
     */
    private static IotModbusTcpServerConfigCacheService mockConfigCacheService() {
        IotModbusPointRespDTO point = new IotModbusPointRespDTO();
        point.setId(POINT_ID);
        point.setIdentifier("temperature");
        point.setFunctionCode(3);
        point.setRegisterAddress(0);
        point.setRegisterCount(1);
        point.setRawDataType("INT16");
        IotModbusDeviceConfigRespDTO config = new IotModbusDeviceConfigRespDTO();
        config.setDeviceId(DEVICE_ID);
        config.setPoints(Collections.singletonList(point));
        IotModbusTcpServerConfigCacheService configCacheService = mock(IotModbusTcpServerConfigCacheService.class);
        when(configCacheService.getConfig(DEVICE_ID)).thenReturn(config);
        return configCacheService;
    }

    private static IotModbusTx newTx(IotModbusTcpServerTransactionChannel channel, TxResult result) {
        return IotModbusTx.ofServer(channel, HexUtil.decodeHex("0300100002"), null,
                (responsePdu, failure) -> {
                    if (failure != null) {
                        result.failures.add(failure);
                    } else {
                        result.responses.add(responsePdu);
                    }
                });
    }

    private static PendingRequest buildPollRequest(IotModbusTcpServerTransactionChannel channel, Integer transactionId) {
        return new PendingRequest(DEVICE_ID, POINT_ID, "temperature", SLAVE_ID, 3, 0x10, 2,
                transactionId, System.currentTimeMillis() + REQUEST_TIMEOUT, channel, null);
    }

    private static IotModbusFrame readResponseFrame(Integer transactionId) {
        return new IotModbusFrame().setSlaveId(SLAVE_ID).setFunctionCode(3)
                .setPdu(new byte[]{0x04, 0x00, (byte) 0xFA, 0x02, 0x58}).setTransactionId(transactionId);
    }

    /**
     * 异常响应帧 83 02：解码器已还原原功能码，pdu[0] 是异常码
     */
    private static IotModbusFrame exceptionFrame(Integer transactionId) {
        return new IotModbusFrame().setSlaveId(SLAVE_ID).setFunctionCode(3)
                .setPdu(new byte[]{0x02}).setExceptionCode(2).setTransactionId(transactionId);
    }

    /**
     * 在 owner 上模拟 handlePollingResponse：先匹配出队，命中透传就交给通道
     */
    private void receive(IotModbusTcpServerTransactionChannel channel, IotModbusFrameFormatEnum frameFormat,
                         IotModbusFrame frame) throws InterruptedException {
        owner.runOnContext(v -> {
            PendingRequest request = pendingRequestManager.matchResponse(DEVICE_ID, frame, frameFormat, channel);
            assertNotNull(request);
            assertNotNull(request.getTx());
            channel.onRawResponse(frame, request);
        });
        awaitOwner();
    }

    private List<Buffer> captureWrites(int times) {
        ArgumentCaptor<Buffer> captor = ArgumentCaptor.forClass(Buffer.class);
        verify(socket, times(times)).write(captor.capture());
        return captor.getAllValues();
    }

    private static void assertFailures(TxResult result, ErrorCode expected) {
        assertEquals(1, result.failures.size());
        assertEquals(expected.getCode(), result.failures.get(0).getCode());
    }

    private static void assertResponse(TxResult result, String expectedHex) {
        assertTrue(result.failures.isEmpty());
        assertEquals(1, result.responses.size());
        assertArrayEquals(HexUtil.decodeHex(expectedHex), result.responses.get(0));
    }

    /**
     * 等待 owner 上已经排队的任务全部执行完：同一个 Context 上的任务按提交顺序执行
     */
    private void awaitOwner() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        owner.runOnContext(v -> latch.countDown());
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    private Thread getOwnerThread() throws InterruptedException {
        AtomicReference<Thread> thread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        owner.runOnContext(v -> {
            thread.set(Thread.currentThread());
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        return thread.get();
    }

    /**
     * 透传事务的终态结果
     */
    private static class TxResult {

        private final List<byte[]> responses = Collections.synchronizedList(new ArrayList<>());
        private final List<ErrorCode> failures = Collections.synchronizedList(new ArrayList<>());

    }

    /**
     * 记录 addRequest 调用线程与条目的待响应请求管理器
     */
    private static class RecordingPendingRequestManager extends IotModbusTcpServerPendingRequestManager {

        private final List<Thread> addThreads = Collections.synchronizedList(new ArrayList<>());
        private final List<PendingRequest> added = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void addRequest(PendingRequest request) {
            addThreads.add(Thread.currentThread());
            added.add(request);
            super.addRequest(request);
        }

        private List<PendingRequest> getPolls() {
            List<PendingRequest> polls = new ArrayList<>();
            for (PendingRequest request : added) {
                if (request.getTx() == null) {
                    polls.add(request);
                }
            }
            return polls;
        }

    }

}
