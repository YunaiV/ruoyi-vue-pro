package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.SCRIPT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotMqttUpstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotMqttUpstreamHandlerTest extends BaseMockitoUnitTest {

    private static final long TIMEOUT_SECONDS = 10;
    private static final String PRODUCT_KEY = "pk";
    private static final String DEVICE_NAME = "dn";
    private static final String SERVER_ID = "mqtt-server";
    private static final String RAW_POST_TOPIC = "/sys/pk/dn/thing/raw/post";
    private static final byte[] PAYLOAD = new byte[]{0x01, 0x02, 0x03};

    @Mock
    private IotDeviceMessageService deviceMessageService;
    @Mock
    private MqttEndpoint endpoint;

    private final IotMqttConnectionManager connectionManager = new IotMqttConnectionManager();
    private IotMqttUpstreamHandler upstreamHandler;
    private Vertx vertx;
    private Context context;

    @BeforeEach
    public void setUp() {
        upstreamHandler = new IotMqttUpstreamHandler(connectionManager, deviceMessageService, SERVER_ID);
        lenient().when(endpoint.clientIdentifier()).thenReturn("client-1");
        connectionManager.registerConnection(endpoint, new IotMqttConnectionManager.ConnectionInfo()
                .setDeviceId(1L).setProductKey(PRODUCT_KEY).setDeviceName(DEVICE_NAME).setRemoteAddress("127.0.0.1:1883"));
        vertx = Vertx.vertx();
        context = vertx.getOrCreateContext();
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    public void testHandleBusinessRequest_rawPostOrderedAck() throws Exception {
        // 准备参数
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<Boolean> onWorker = new CopyOnWriteArrayList<>();
        // mock 方法：第一条透传上报阻塞，第二条立即完成；记录执行线程
        when(deviceMessageService.sendRawMessage(aryEq(PAYLOAD), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID),
                eq(IotProtocolTypeEnum.MQTT), anyLong())).thenAnswer(invocation -> {
            onWorker.add(Context.isOnWorkerThread());
            entered.countDown();
            if (!release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("等待释放超时");
            }
            return "M1";
        }).thenAnswer(invocation -> {
            onWorker.add(Context.isOnWorkerThread());
            return "M2";
        });

        // 调用：记录每条消息交给 handler 前后的时刻
        long firstBeginNanos = System.nanoTime();
        Future<Void> first = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, RAW_POST_TOPIC, PAYLOAD));
        long firstEndNanos = System.nanoTime();
        long secondBeginNanos = System.nanoTime();
        Future<Void> second = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, RAW_POST_TOPIC, PAYLOAD));
        long secondEndNanos = System.nanoTime();

        try {
            // 断言：处理完成之前不完成（不确认）；第二条排在第一条之后
            assertTrue(entered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertFalse(first.isComplete());
            assertFalse(second.isComplete());
        } finally {
            release.countDown();
        }
        // 断言：释放后按顺序完成；在 worker 上处理
        await(first);
        await(second);
        assertEquals(Arrays.asList(true, true), onWorker);
        // 断言：收到的时刻在交给 handler 时（入队之前）取得：第二条在第一条阻塞期间排队，它的时刻仍在入队前后之间
        ArgumentCaptor<Long> receivedCaptor = ArgumentCaptor.forClass(Long.class);
        verify(deviceMessageService, times(2)).sendRawMessage(any(), any(), any(), any(), any(), receivedCaptor.capture());
        assertBetween(firstBeginNanos, receivedCaptor.getAllValues().get(0), firstEndNanos);
        assertBetween(secondBeginNanos, receivedCaptor.getAllValues().get(1), secondEndNanos);
        verify(deviceMessageService, never()).deserializeDeviceMessage(any(), any(), any());
    }

    @Test
    public void testHandleBusinessRequest_rawPostFailureStillAck() throws Exception {
        // 准备参数
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context);
        // mock 方法：处理失败（脚本超时、未知异常）
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(exception(SCRIPT_TIMEOUT))
                .thenThrow(new IllegalStateException("boom"));

        // 调用
        Future<Void> first = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, RAW_POST_TOPIC, PAYLOAD));
        Future<Void> second = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, RAW_POST_TOPIC, PAYLOAD));

        // 断言：只记日志，照常完成（照常确认），与原来一致
        await(first);
        await(second);
        assertTrue(first.succeeded());
        assertTrue(second.succeeded());
    }

    @Test
    public void testHandleBusinessRequest_standard() throws Exception {
        // 准备参数
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context);
        String topic = "/sys/pk/dn/thing/property/post";
        byte[] payload = "{\"method\":\"thing.property.post\"}".getBytes();
        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", Collections.singletonMap("t", 1));
        // mock 方法
        when(deviceMessageService.deserializeDeviceMessage(payload, PRODUCT_KEY, DEVICE_NAME)).thenReturn(message);

        // 调用
        await(callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, topic, payload)));

        // 断言：反序列化后按标准路径发送，不走透传
        verify(deviceMessageService).sendDeviceMessage(same(message), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));
        verify(deviceMessageService, never()).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    public void testHandleBusinessRequest_invalidRequest() throws Exception {
        // 准备参数
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context);
        MqttEndpoint unknownEndpoint = mock(MqttEndpoint.class);

        // 调用：伪造其它设备的主题、未认证的连接、空载荷
        Future<Void> forged = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue,
                "/sys/pk/other/thing/raw/post", PAYLOAD));
        Future<Void> unknown = callOnContext(() -> upstreamHandler.handleBusinessRequest(unknownEndpoint, queue,
                RAW_POST_TOPIC, PAYLOAD));
        Future<Void> empty = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue,
                RAW_POST_TOPIC, new byte[0]));

        // 断言：只记日志，照常完成（照常确认），不处理
        await(forged);
        await(unknown);
        await(empty);
        assertTrue(forged.succeeded());
        assertTrue(unknown.succeeded());
        assertTrue(empty.succeeded());
        verifyNoInteractions(deviceMessageService);
    }

    @Test
    public void testHandleBusinessRequest_invalidRequestOrderedAck() throws Exception {
        // 准备参数
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        // mock 方法：第一条透传上报阻塞
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenAnswer(invocation -> {
            entered.countDown();
            if (!release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("等待释放超时");
            }
            return "M1";
        });

        // 调用：第二条是空载荷，不处理
        Future<Void> first = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, RAW_POST_TOPIC, PAYLOAD));
        Future<Void> empty = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue,
                RAW_POST_TOPIC, new byte[0]));

        try {
            // 断言：不处理的消息也排在前一条之后完成，确认（PUBACK）不越过前一条
            assertTrue(entered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertFalse(first.isComplete());
            assertFalse(empty.isComplete());
        } finally {
            release.countDown();
        }
        await(first);
        await(empty);
        assertTrue(empty.succeeded());
        verify(deviceMessageService, times(1)).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    public void testHandleBusinessRequest_queueFull() throws Exception {
        // 准备参数：队列最多 1 个待处理任务
        IotWorkerTaskQueue queue = new IotWorkerTaskQueue(context, true, 1);
        CountDownLatch release = new CountDownLatch(1);
        // mock 方法：第一条阻塞
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenAnswer(invocation -> {
            if (!release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("等待释放超时");
            }
            return "M1";
        });

        try {
            // 调用
            Future<Void> first = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, RAW_POST_TOPIC, PAYLOAD));
            Future<Void> second = callOnContext(() -> upstreamHandler.handleBusinessRequest(endpoint, queue, RAW_POST_TOPIC, PAYLOAD));

            // 断言：超过上限立即失败（调用方断开连接，不确认）
            assertTrue(second.failed());
            assertInstanceOf(RejectedExecutionException.class, second.cause());
            assertFalse(first.isComplete());
            release.countDown();
            await(first);
            assertTrue(first.succeeded());
            // 断言：被拒绝的那条不处理
            verify(deviceMessageService, times(1)).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
        } finally {
            release.countDown();
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 在连接所在的 context 线程上执行（与 MQTT 的 publishHandler 一致），等待并返回结果
     */
    private <T> T callOnContext(Callable<T> action) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        context.runOnContext(v -> {
            try {
                result.complete(action.call());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        });
        return result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static void await(Future<?> future) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        future.onComplete(ar -> latch.countDown());
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    private static void assertBetween(long beginNanos, long actualNanos, long endNanos) {
        assertTrue(actualNanos >= beginNanos && actualNanos <= endNanos,
                String.format("%d 不在 [%d, %d] 之间", actualNanos, beginNanos, endNanos));
    }

}
