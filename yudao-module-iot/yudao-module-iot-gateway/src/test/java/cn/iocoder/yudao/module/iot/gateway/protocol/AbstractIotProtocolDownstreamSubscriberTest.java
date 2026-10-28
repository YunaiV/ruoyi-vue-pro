package cn.iocoder.yudao.module.iot.gateway.protocol;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.dto.IotDownstreamEncodeRespDTO;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_EXCEPTION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AbstractIotProtocolDownstreamSubscriber} 的单元测试
 *
 * @author 芋道源码
 */
public class AbstractIotProtocolDownstreamSubscriberTest extends BaseMockitoUnitTest {

    private static final Long DEVICE_ID = 1L;
    private static final String SERVER_ID = "server-1";

    @Mock
    private IotProtocol protocol;
    @Mock
    private IotMessageBus messageBus;
    @Mock
    private Vertx vertx;
    @Mock
    private IotDeviceMessageService deviceMessageService;

    private final IotGatewayProperties gatewayProperties = new IotGatewayProperties();
    private MockedStatic<SpringUtil> springUtilMockedStatic;
    private TestSubscriber subscriber;

    @BeforeEach
    public void setUp() {
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        lenient().when(protocol.getType()).thenReturn(IotProtocolTypeEnum.TCP);
        subscriber = new TestSubscriber(protocol, messageBus);
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
    }

    // ========== onMessage：raw.push ==========

    @Test
    public void testOnMessage_rawPush() {
        // 准备参数：字节在 raw 字段；功能开关关闭时也照常写出
        byte[] bytes = new byte[]{0x01, 0x02, (byte) 0xFF};
        IotDeviceMessage message = buildRawPush(Base64.getEncoder().encodeToString(bytes));

        // 调用
        subscriber.onMessage(message);

        // 断言：从 raw 解码后直接交给协议写出，不经过脚本、不读开关
        assertEquals(1, subscriber.rawMessages.size());
        assertSame(message, subscriber.rawMessages.get(0));
        assertArrayEquals(bytes, subscriber.rawBytes.get(0));
        assertNull(subscriber.rawSources.get(0));
        assertTrue(subscriber.handledMessages.isEmpty());
        springUtilMockedStatic.verifyNoInteractions();
        verify(protocol, never()).getVertx();
    }

    @Test
    public void testOnMessage_rawPushPayloadLimit() {
        // 调用：4097 字节、4096 字节（公共层只按通用上限解码，协议自己的字节规则由协议写出前校验）
        subscriber.onMessage(buildRawPush(Base64.getEncoder().encodeToString(new byte[4097])));
        subscriber.onMessage(buildRawPush(Base64.getEncoder().encodeToString(new byte[4096])));

        // 断言：超过上限的不写出（只记日志），不改走标准路径
        assertEquals(1, subscriber.rawBytes.size());
        assertEquals(4096, subscriber.rawBytes.get(0).length);
        assertTrue(subscriber.handledMessages.isEmpty());
    }

    @Test
    public void testOnMessage_rawPushInvalidRaw() {
        // 调用：raw 为空、不是合法的 Base64
        subscriber.onMessage(buildRawPush(null));
        subscriber.onMessage(buildRawPush("!!not-base64!!"));

        // 断言：不写出，也不改走标准路径
        assertTrue(subscriber.rawMessages.isEmpty());
        assertTrue(subscriber.handledMessages.isEmpty());
    }

    @Test
    public void testOnMessage_rawPushWriteFailureOnlyLog() {
        // 准备参数：协议写出失败（设备离线返回 null、异步失败）
        IotDeviceMessage message = buildRawPush(Base64.getEncoder().encodeToString(new byte[]{0x01}));

        // 调用，并断言：只记日志，不抛出
        subscriber.rawResult = null;
        assertDoesNotThrow(() -> subscriber.onMessage(message));
        subscriber.rawResult = Future.failedFuture(new IllegalStateException("closed"));
        assertDoesNotThrow(() -> subscriber.onMessage(message));
        assertEquals(2, subscriber.rawMessages.size());
        assertTrue(subscriber.handledMessages.isEmpty());
    }

    @Test
    public void testOnMessage_rawPushReply() {
        // 准备参数：raw.push 的回复（带 code）不是下发
        mockScriptProperties();
        IotDeviceMessage reply = IotDeviceMessage.replyOf("r1", IotDeviceMessageMethodEnum.RAW_PUSH.getMethod(),
                null, 0, null).setDeviceId(DEVICE_ID);

        // 调用
        subscriber.onMessage(reply);

        // 断言：直接走标准路径
        assertEquals(Collections.singletonList(reply), subscriber.handledMessages);
        assertTrue(subscriber.rawMessages.isEmpty());
    }

    // ========== onMessage：指令与标准路径 ==========

    @Test
    public void testOnMessage_commandSwitchOff() {
        // 准备参数：功能开关默认关闭
        mockScriptProperties();
        IotDeviceMessage message = buildPropertySet();

        // 调用
        subscriber.onMessage(message);

        // 断言：与原来一致，直接走标准路径，不切 worker、不调用设备消息 Service
        assertEquals(Collections.singletonList(message), subscriber.handledMessages);
        verify(protocol, never()).getVertx();
        verifyNoInteractions(deviceMessageService);
    }

    @Test
    public void testOnMessage_commandSwitchOn() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        mockDeviceMessageService();
        mockExecuteBlocking();
        IotDeviceMessage message = buildPropertySet();

        // 调用
        long beginNanos = System.nanoTime();
        subscriber.onMessage(message);
        long endNanos = System.nanoTime();

        // 断言：切到协议的 worker，交给设备消息 Service 按协议 encode，带上收到指令的时刻
        ArgumentCaptor<Long> receivedCaptor = ArgumentCaptor.forClass(Long.class);
        verify(deviceMessageService).encodeDownstreamCommand(same(message), eq(IotProtocolTypeEnum.TCP),
                receivedCaptor.capture());
        assertTrue(receivedCaptor.getValue() >= beginNanos);
        assertTrue(receivedCaptor.getValue() <= endNanos);
        assertTrue(subscriber.handledMessages.isEmpty());
        assertTrue(subscriber.rawMessages.isEmpty());
    }

    @Test
    public void testOnMessage_commandEncodeResult() {
        // 准备参数：encode 结果分别为标准路径、透传字节、不下发
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        mockDeviceMessageService();
        mockExecuteBlocking();
        IotDeviceMessage standard = buildPropertySet("c1", DEVICE_ID);
        IotDeviceMessage raw = buildPropertySet("c2", DEVICE_ID);
        IotDeviceMessage notSend = buildPropertySet("c3", DEVICE_ID);
        byte[] bytes = new byte[]{0x01, 0x02};
        IotScriptSource source = IotScriptSource.of(100L, "function encode(ctx) {}");
        when(deviceMessageService.encodeDownstreamCommand(any(), any(), anyLong())).thenAnswer(invocation -> {
            IotDeviceMessage message = invocation.getArgument(0);
            return message == standard ? IotDownstreamEncodeRespDTO.standard()
                    : message == raw ? IotDownstreamEncodeRespDTO.raw(bytes, source) : null;
        });

        // 调用
        subscriber.onMessage(standard);
        subscriber.onMessage(raw);
        subscriber.onMessage(notSend);

        // 断言：标准路径 → handleMessage；透传 → handleRawMessage，带上 encode 时固定的脚本；不下发 → 都不调用
        assertEquals(Collections.singletonList(standard), subscriber.handledMessages);
        assertEquals(Collections.singletonList(raw), subscriber.rawMessages);
        assertSame(bytes, subscriber.rawBytes.get(0));
        assertSame(source, subscriber.rawSources.get(0));
        verify(deviceMessageService).encodeDownstreamCommand(same(notSend), any(), anyLong());
    }

    @Test
    public void testOnMessage_commandNotSupportRaw() {
        // 准备参数：协议不支持透传下发（HTTP / CoAP）
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        subscriber.supportsRaw = false;
        IotDeviceMessage message = buildPropertySet();

        // 调用
        subscriber.onMessage(message);

        // 断言：直接走标准路径
        assertEquals(Collections.singletonList(message), subscriber.handledMessages);
        verify(protocol, never()).getVertx();
        verifyNoInteractions(deviceMessageService);
    }

    @Test
    public void testOnMessage_notCommand() {
        // 准备参数：功能开关开启，但不是指令（配置推送、OTA、属性设置的回复）
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        List<IotDeviceMessage> messages = new ArrayList<>();
        messages.add(IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.CONFIG_PUSH.getMethod(),
                Collections.emptyMap()).setDeviceId(DEVICE_ID));
        messages.add(IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.OTA_UPGRADE.getMethod(),
                Collections.emptyMap()).setDeviceId(DEVICE_ID));
        messages.add(IotDeviceMessage.replyOf("r1", IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(),
                null, 0, null).setDeviceId(DEVICE_ID));

        // 调用
        messages.forEach(subscriber::onMessage);

        // 断言：都走标准路径
        assertEquals(messages, subscriber.handledMessages);
        verify(protocol, never()).getVertx();
        verifyNoInteractions(deviceMessageService);
    }

    @Test
    public void testOnMessage_blankMethod() {
        // 调用
        subscriber.onMessage(new IotDeviceMessage().setDeviceId(DEVICE_ID));

        // 断言：直接忽略
        assertTrue(subscriber.handledMessages.isEmpty());
        assertTrue(subscriber.rawMessages.isEmpty());
        springUtilMockedStatic.verifyNoInteractions();
    }

    // ========== onMessage：同一设备的指令保序 ==========

    @Test
    public void testOnMessage_commandsOrderedPerDevice() throws Exception {
        // 准备参数：设备 1 的指令 c1、c2，encode 后走标准路径的 c3；设备 2 的指令 d1
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        mockDeviceMessageService();
        Vertx realVertx = Vertx.vertx();
        when(protocol.getVertx()).thenReturn(realVertx);
        IotDeviceMessage c1 = buildPropertySet("c1", DEVICE_ID);
        IotDeviceMessage c2 = buildPropertySet("c2", DEVICE_ID);
        IotDeviceMessage c3 = buildPropertySet("c3", DEVICE_ID);
        IotDeviceMessage d1 = buildPropertySet("d1", 2L);
        // mock 方法：c1 的 worker 阻塞，直到放行；记录执行顺序
        List<String> order = new CopyOnWriteArrayList<>();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch otherDevice = new CountDownLatch(1);
        doAnswer(invocation -> {
            IotDeviceMessage message = invocation.getArgument(0);
            if (message == c1) {
                entered.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            if (message == d1) {
                otherDevice.countDown();
            }
            order.add(message.getRequestId());
            return message == c3 ? IotDownstreamEncodeRespDTO.standard() : null;
        }).when(deviceMessageService).encodeDownstreamCommand(any(), any(), anyLong());

        try {
            // 调用：c1 阻塞在 worker 上时，依次收到 c2、c3、d1
            subscriber.onMessage(c1);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            long c2BeginNanos = System.nanoTime();
            subscriber.onMessage(c2);
            long c2QueuedNanos = System.nanoTime();
            subscriber.onMessage(c3);
            subscriber.onMessage(d1);

            // 断言：其它设备不受影响；同一设备的 c2、c3（含标准路径）排在 c1 之后，不越过它
            assertTrue(otherDevice.await(5, TimeUnit.SECONDS));
            assertFalse(order.contains("c2"));
            assertFalse(order.contains("c3"));
            assertTrue(subscriber.handledMessages.isEmpty());

            // 放行：按收到的顺序执行
            release.countDown();
            await(() -> subscriber.handledMessages.size() == 1);
            List<String> deviceOrder = new ArrayList<>(order);
            deviceOrder.remove("d1");
            assertEquals(Arrays.asList("c1", "c2", "c3"), deviceOrder);
            assertEquals(Collections.singletonList(c3), subscriber.handledMessages);
            // 断言：c2 排在 c1 之后才执行，传给 service 的仍是收到时（排队之前）的时刻，排队的时间计入截止
            ArgumentCaptor<Long> receivedCaptor = ArgumentCaptor.forClass(Long.class);
            verify(deviceMessageService).encodeDownstreamCommand(same(c2), eq(IotProtocolTypeEnum.TCP),
                    receivedCaptor.capture());
            assertTrue(receivedCaptor.getValue() >= c2BeginNanos);
            assertTrue(receivedCaptor.getValue() <= c2QueuedNanos);
        } finally {
            release.countDown();
            realVertx.close();
        }
    }

    @Test
    public void testOnMessage_commandFailureNotBlockNext() {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        mockDeviceMessageService();
        mockExecuteBlocking();
        IotDeviceMessage c1 = buildPropertySet("c1", DEVICE_ID);
        IotDeviceMessage c2 = buildPropertySet("c2", DEVICE_ID);
        // mock 方法：c1 处理失败
        doAnswer(invocation -> {
            if (invocation.getArgument(0) == c1) {
                throw new IllegalStateException("boom");
            }
            return IotDownstreamEncodeRespDTO.standard();
        }).when(deviceMessageService).encodeDownstreamCommand(any(), any(), anyLong());

        // 调用
        subscriber.onMessage(c1);
        subscriber.onMessage(c2);

        // 断言：c1 失败只记日志，c2 照常执行
        InOrder inOrder = inOrder(deviceMessageService);
        inOrder.verify(deviceMessageService).encodeDownstreamCommand(same(c1), any(), anyLong());
        inOrder.verify(deviceMessageService).encodeDownstreamCommand(same(c2), any(), anyLong());
        assertEquals(Collections.singletonList(c2), subscriber.handledMessages);
        // 断言：队列执行完后已移除，之后的指令新建队列照常执行
        IotDeviceMessage c3 = buildPropertySet("c3", DEVICE_ID);
        subscriber.onMessage(c3);
        assertEquals(Arrays.asList(c2, c3), subscriber.handledMessages);
    }

    @Test
    public void testOnMessage_commandsCapacity() throws Exception {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        mockDeviceMessageService();
        Vertx realVertx = Vertx.vertx();
        when(protocol.getVertx()).thenReturn(realVertx);
        IotDeviceMessage first = buildPropertySet("first", DEVICE_ID);
        IotDeviceMessage overflow = buildPropertySet("overflow", DEVICE_ID);
        // mock 方法：第一条阻塞
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) == first) {
                entered.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            return null;
        }).when(deviceMessageService).encodeDownstreamCommand(any(), any(), anyLong());

        try {
            // 调用：第一条执行中，再排 256 条（上限），第 257 条超出
            subscriber.onMessage(first);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            for (int i = 0; i < 256; i++) {
                subscriber.onMessage(buildPropertySet("q" + i, DEVICE_ID));
            }
            subscriber.onMessage(overflow);
            release.countDown();

            // 断言：上限内的全部执行，超出的丢弃
            verify(deviceMessageService, timeout(5000).times(257)).encodeDownstreamCommand(any(), any(), anyLong());
            verify(deviceMessageService, never()).encodeDownstreamCommand(same(overflow), any(), anyLong());
        } finally {
            release.countDown();
            realVertx.close();
        }
    }

    @Test
    public void testOnMessage_commandsCapacityConfigured() throws Exception {
        // 准备参数：每个设备最多排队 2 条
        gatewayProperties.getScript().setEnabled(true).setMaxPendingCommandsPerDevice(2);
        mockScriptProperties();
        mockDeviceMessageService();
        Vertx realVertx = Vertx.vertx();
        when(protocol.getVertx()).thenReturn(realVertx);
        IotDeviceMessage first = buildPropertySet("first", DEVICE_ID);
        IotDeviceMessage overflow = buildPropertySet("overflow", DEVICE_ID);
        // mock 方法：第一条阻塞
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) == first) {
                entered.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            return null;
        }).when(deviceMessageService).encodeDownstreamCommand(any(), any(), anyLong());

        try {
            // 调用：第一条执行中，再排 2 条（上限），第 3 条超出
            subscriber.onMessage(first);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            subscriber.onMessage(buildPropertySet("q0", DEVICE_ID));
            subscriber.onMessage(buildPropertySet("q1", DEVICE_ID));
            subscriber.onMessage(overflow);
            release.countDown();

            // 断言：按配置的上限，执行中 1 条 + 排队 2 条全部执行，超出的丢弃
            verify(deviceMessageService, timeout(5000).times(3)).encodeDownstreamCommand(any(), any(), anyLong());
            verify(deviceMessageService, never()).encodeDownstreamCommand(same(overflow), any(), anyLong());
        } finally {
            release.countDown();
            realVertx.close();
        }
    }

    @Test
    public void testStop_dropPendingCommands() throws Exception {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        mockDeviceMessageService();
        Vertx realVertx = Vertx.vertx();
        when(protocol.getVertx()).thenReturn(realVertx);
        IotDeviceMessage c1 = buildPropertySet("c1", DEVICE_ID);
        IotDeviceMessage c2 = buildPropertySet("c2", DEVICE_ID);
        // mock 方法：c1 阻塞
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            assertTrue(release.await(10, TimeUnit.SECONDS));
            return null;
        }).when(deviceMessageService).encodeDownstreamCommand(same(c1), any(), anyLong());

        try {
            // 调用：c1 执行中、c2 排队时停止
            subscriber.onMessage(c1);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            subscriber.onMessage(c2);
            subscriber.stop();
            release.countDown();

            // 断言：执行中的 c1 照常结束，排队的 c2 不再执行
            verify(deviceMessageService, after(500).never()).encodeDownstreamCommand(same(c2), any(), anyLong());
            verify(messageBus).unregister(same(subscriber));
        } finally {
            release.countDown();
            realVertx.close();
        }
    }

    @Test
    public void testStop_restartNotReviveOldCommands() throws Exception {
        // 准备参数
        gatewayProperties.getScript().setEnabled(true);
        mockScriptProperties();
        mockDeviceMessageService();
        Vertx realVertx = Vertx.vertx();
        when(protocol.getVertx()).thenReturn(realVertx);
        IotDeviceMessage c1 = buildPropertySet("c1", DEVICE_ID);
        IotDeviceMessage c2 = buildPropertySet("c2", DEVICE_ID);
        IotDeviceMessage c3 = buildPropertySet("c3", DEVICE_ID);
        // mock 方法：c1 阻塞
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) == c1) {
                entered.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            return null;
        }).when(deviceMessageService).encodeDownstreamCommand(any(), any(), anyLong());

        try {
            // 调用：c1 执行中、c2 排队时停止，再启动后收到 c3
            subscriber.onMessage(c1);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            subscriber.onMessage(c2);
            subscriber.stop();
            subscriber.start();
            subscriber.onMessage(c3);

            // 断言：c3 新建队列照常执行，不等待旧的 c1
            verify(deviceMessageService, timeout(5000)).encodeDownstreamCommand(same(c3), any(), anyLong());
            // 断言：c1 结束后，旧队列里的 c2 不会因为新队列而复活
            release.countDown();
            verify(deviceMessageService, after(500).never()).encodeDownstreamCommand(same(c2), any(), anyLong());
        } finally {
            release.countDown();
            realVertx.close();
        }
    }

    // ========== replyRawResult ==========

    @Test
    public void testReplyRawResult() {
        // 准备参数：设备的异常响应（协议判定的设备错误）
        IotDeviceMessage request = buildRawPush(Base64.getEncoder().encodeToString(new byte[]{0x03, 0x00, 0x10, 0x00, 0x02}));
        IotScriptSource source = IotScriptSource.of(100L, "function decode(ctx) {}");
        byte[] payload = new byte[]{(byte) 0x83, 0x02};
        // mock 方法
        when(protocol.getServerId()).thenReturn(SERVER_ID);
        mockDeviceMessageService();
        mockExecuteBlocking();

        // 调用
        subscriber.reply(request, source, payload, MODBUS_EXCEPTION);

        // 断言：切到协议的 worker 回复给 biz，serverId 为本协议实例，设备错误原样传入
        verify(vertx).executeBlocking(any(Callable.class), eq(false));
        verify(deviceMessageService).sendRawReply(same(request), same(source), aryEq(payload), same(MODBUS_EXCEPTION),
                eq(SERVER_ID));
    }

    // ========== 辅助方法 ==========

    private void mockScriptProperties() {
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotGatewayProperties.class)).thenReturn(gatewayProperties);
    }

    private void mockDeviceMessageService() {
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceMessageService.class)).thenReturn(deviceMessageService);
    }

    /**
     * executeBlocking 在当前线程同步执行（SpringUtil 的静态 mock 只在当前线程生效）
     */
    @SuppressWarnings("unchecked")
    private void mockExecuteBlocking() {
        when(protocol.getVertx()).thenReturn(vertx);
        when(vertx.executeBlocking(any(Callable.class), eq(false))).thenAnswer(invocation -> {
            Callable<Object> callable = invocation.getArgument(0);
            return Future.succeededFuture(callable.call());
        });
    }

    private static IotDeviceMessage buildRawPush(String raw) {
        return IotDeviceMessage.requestOf("r1", IotDeviceMessageMethodEnum.RAW_PUSH.getMethod(), null)
                .setDeviceId(DEVICE_ID).setRaw(raw);
    }

    private static IotDeviceMessage buildPropertySet() {
        return buildPropertySet("r2", DEVICE_ID);
    }

    private static IotDeviceMessage buildPropertySet(String requestId, Long deviceId) {
        return IotDeviceMessage.requestOf(requestId, IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(),
                Collections.singletonMap("power", 1)).setDeviceId(deviceId);
    }

    /**
     * 等待条件成立（最长 5 秒）
     */
    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < deadline, "等待超时");
            Thread.sleep(10);
        }
    }

    /**
     * 记录调用的测试订阅者
     */
    private static class TestSubscriber extends AbstractIotProtocolDownstreamSubscriber {

        // worker 上也会调用（指令保序的用例），所以使用线程安全的 List
        private final List<IotDeviceMessage> handledMessages = new CopyOnWriteArrayList<>();
        private final List<IotDeviceMessage> rawMessages = new CopyOnWriteArrayList<>();
        private final List<byte[]> rawBytes = new CopyOnWriteArrayList<>();
        private final List<IotScriptSource> rawSources = new CopyOnWriteArrayList<>();
        private boolean supportsRaw = true;
        private Future<Void> rawResult = Future.succeededFuture();

        TestSubscriber(IotProtocol protocol, IotMessageBus messageBus) {
            super(protocol, messageBus);
        }

        @Override
        protected void handleMessage(IotDeviceMessage message) {
            handledMessages.add(message);
        }

        @Override
        public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes, IotScriptSource source) {
            rawMessages.add(message);
            rawBytes.add(bytes);
            rawSources.add(source);
            return rawResult;
        }

        @Override
        public boolean supportsRawDownstream() {
            return supportsRaw;
        }

        void reply(IotDeviceMessage request, IotScriptSource source, byte[] payload, ErrorCode deviceError) {
            replyRawResult(request, source, payload, deviceError);
        }

    }

}
