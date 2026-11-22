package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.upstream.IotOpcUaClientUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession.DeviceBinding;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSubscriptionManager.ItemBinding;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSubscriptionManager.SubscriptionGroup;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.eclipse.milo.opcua.sdk.client.subscriptions.MonitoredItemServiceOperationResult;
import org.eclipse.milo.opcua.sdk.client.subscriptions.MonitoredItemSynchronizationException;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaSubscription;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotOpcUaClientSubscriptionManager} 的单元测试：MonitoredItem 同步失败的分类、重建重试、过期回调的丢弃
 *
 * @author 芋道源码
 */
public class IotOpcUaClientSubscriptionManagerTest {

    private static final int INTERVAL = 1000;
    private static final Long DEVICE_ID = 1L;

    private Vertx vertx;
    private Context context;
    private IotOpcUaClientUpstreamHandler upstreamHandler;
    private IotOpcUaClientSubscriptionManager subscriptionManager;

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        context = vertx.getOrCreateContext();
        upstreamHandler = mock(IotOpcUaClientUpstreamHandler.class);
        subscriptionManager = new IotOpcUaClientSubscriptionManager(vertx, context, new IotOpcUaClientConfig(),
                upstreamHandler);
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    // ========== synchronize ==========

    @Test
    public void testSynchronize_serviceFailureThrows() throws Exception {
        // mock 方法：CreateMonitoredItems 服务级失败（例如超时、Subscription 失效）
        OpcUaSubscription subscription = mock(OpcUaSubscription.class);
        MonitoredItemSynchronizationException exception = newSynchronizationException(
                new StatusCode(StatusCodes.Bad_Timeout), null);
        doThrow(exception).when(subscription).synchronizeMonitoredItems();

        // 调用，并断言：原样抛出，由调用方按订阅级失败重建
        assertSame(exception, assertThrows(MonitoredItemSynchronizationException.class,
                () -> IotOpcUaClientSubscriptionManager.synchronize(subscription)));
    }

    @Test
    public void testSynchronize_itemFailureReturned() throws Exception {
        // mock 方法：服务调用成功，单个 MonitoredItem 失败（NodeId 不存在）
        OpcUaSubscription subscription = mock(OpcUaSubscription.class);
        doThrow(newSynchronizationException(StatusCode.GOOD, new StatusCode(StatusCodes.Bad_NodeIdUnknown)))
                .when(subscription).synchronizeMonitoredItems();

        // 调用
        List<String> failures = IotOpcUaClientSubscriptionManager.synchronize(subscription);
        // 断言：只返回失败描述，不抛出
        assertEquals(1, failures.size());
        assertTrue(failures.get(0).contains("temperature"));
    }

    // ========== execute ==========

    @Test
    public void testExecute_serviceFailureRebuildAndRetry() throws Exception {
        // 准备参数：同步 MonitoredItem 时服务级失败
        OpcUaSubscription subscription = mock(OpcUaSubscription.class);
        doThrow(newSynchronizationException(new StatusCode(StatusCodes.Bad_SessionIdInvalid), null))
                .when(subscription).synchronizeMonitoredItems();
        IotOpcUaClientSession session = new IotOpcUaClientSession("session", context);
        SubscriptionGroup group = new SubscriptionGroup(INTERVAL);

        // 调用
        callOnContext(() -> {
            session.getSubscriptionGroups().put(INTERVAL, group);
            subscriptionManager.execute(session, group, () -> IotOpcUaClientSubscriptionManager.synchronize(subscription));
            return null;
        });
        // 断言：进入重建，并按 retryInterval 重试
        await().atMost(5, TimeUnit.SECONDS).until(() -> callOnContext(() -> group.getRetryTimerId() != null));
        assertTrue(callOnContext(group::isRebuild));
    }

    @Test
    public void testExecute_staleServiceFailureIgnored() throws Exception {
        // 准备参数：同步 MonitoredItem 时服务级失败
        OpcUaSubscription subscription = mock(OpcUaSubscription.class);
        doThrow(newSynchronizationException(new StatusCode(StatusCodes.Bad_SessionIdInvalid), null))
                .when(subscription).synchronizeMonitoredItems();
        IotOpcUaClientSession session = new IotOpcUaClientSession("session", context);
        SubscriptionGroup group = new SubscriptionGroup(INTERVAL);

        // 调用：提交后、失败回调前会话进入新的一代（断线、重连过）；回调排在本次 context 任务之后，结果确定
        callOnContext(() -> {
            session.getSubscriptionGroups().put(INTERVAL, group);
            subscriptionManager.execute(session, group, () -> IotOpcUaClientSubscriptionManager.synchronize(subscription));
            session.nextGeneration();
            return null;
        });

        // 断言：旧代的失败不给新代的订阅组标记重建、安排重试
        await().atMost(5, TimeUnit.SECONDS).until(() -> callOnContext(() -> !group.isBusy()));
        assertFalse(callOnContext(group::isRebuild));
        assertNull(callOnContext(group::getRetryTimerId));
    }

    // ========== onSessionActive ==========

    @Test
    public void testOnSessionActive_applyMaxMonitoredItemsPerCall() throws Exception {
        // 准备参数：重连后转移成功的订阅组；本次激活的上限为 2（服务端 OperationLimits 低于本地配置）
        OpcUaSubscription subscription = mock(OpcUaSubscription.class);
        when(subscription.getSubscriptionId()).thenReturn(Optional.of(uint(1)));
        IotOpcUaClientSession session = new IotOpcUaClientSession("session", context);
        SubscriptionGroup group = new SubscriptionGroup(INTERVAL);
        ReflectUtil.setFieldValue(group, "subscription", subscription);

        // 调用
        callOnContext(() -> {
            session.getSubscriptionGroups().put(INTERVAL, group);
            session.setMaxMonitoredItemsPerCall(2);
            subscriptionManager.onSessionActive(session, false);
            return null;
        });

        // 断言：应用到 Subscription（Milo 据此重新计算分批大小），不重建
        verify(subscription).setMaxMonitoredItemsPerCall(eq(uint(2)));
        assertFalse(callOnContext(group::isRebuild));
    }

    @Test
    public void testExecute_itemFailureNoRebuild() throws Exception {
        // 准备参数：只有单个 MonitoredItem 失败
        OpcUaSubscription subscription = mock(OpcUaSubscription.class);
        doThrow(newSynchronizationException(StatusCode.GOOD, new StatusCode(StatusCodes.Bad_NodeIdUnknown)))
                .when(subscription).synchronizeMonitoredItems();
        IotOpcUaClientSession session = new IotOpcUaClientSession("session", context);
        SubscriptionGroup group = new SubscriptionGroup(INTERVAL);

        // 调用
        callOnContext(() -> {
            session.getSubscriptionGroups().put(INTERVAL, group);
            subscriptionManager.execute(session, group, () -> IotOpcUaClientSubscriptionManager.synchronize(subscription));
            return null;
        });
        // 断言：执行完成后不重建、不重试（只告警）
        verify(subscription, timeout(5000)).synchronizeMonitoredItems();
        Thread.sleep(200);
        assertFalse(callOnContext(group::isRebuild));
        assertNull(callOnContext(group::getRetryTimerId));
    }

    // ========== 过期回调 ==========

    @Test
    public void testHandleData_staleGenerationDropped() throws Exception {
        // 准备参数：会话激活，设备的 MonitoredItem
        IotOpcUaClientSession session = new IotOpcUaClientSession("session", context);
        SubscriptionGroup group = new SubscriptionGroup(INTERVAL);
        OpcUaMonitoredItem item = mock(OpcUaMonitoredItem.class);
        when(item.getUserObject()).thenReturn(Optional.of(newItemBinding()));
        List<OpcUaMonitoredItem> items = Collections.singletonList(item);
        List<DataValue> values = Collections.singletonList(new DataValue(new Variant(25.5D)));
        long staleGeneration = callOnContext(() -> {
            session.setActive(true);
            session.getSubscriptionGroups().put(INTERVAL, group);
            IotOpcUaDeviceConfigRespDTO config = new IotOpcUaDeviceConfigRespDTO();
            config.setDeviceId(DEVICE_ID);
            session.getDevices().put(DEVICE_ID, new DeviceBinding(config));
            long generation = session.getGeneration();
            // 回调之后又断线、重连过
            session.nextGeneration();
            return generation;
        });

        // 调用：回调时的代数已过期，整批丢弃
        callOnContext(() -> {
            subscriptionManager.handleData(session, staleGeneration, group, null, items, values);
            return null;
        });
        verify(upstreamHandler, never()).collectValue(any(), anyLong(), anyString(), any(), any());

        // 调用：当前代的通知正常处理
        callOnContext(() -> {
            subscriptionManager.handleData(session, session.getGeneration(), group, null, items, values);
            return null;
        });
        verify(upstreamHandler).collectValue(same(session), eq(DEVICE_ID), eq("temperature"), same(values.get(0)), any());
    }

    @Test
    public void testRequestRebuild_staleGenerationIgnored() throws Exception {
        // 准备参数：会话未激活（重建只标记，等激活后再执行）
        IotOpcUaClientSession session = new IotOpcUaClientSession("session", context);
        SubscriptionGroup group = new SubscriptionGroup(INTERVAL);
        long staleGeneration = callOnContext(() -> {
            session.getSubscriptionGroups().put(INTERVAL, group);
            long generation = session.getGeneration();
            session.nextGeneration();
            return generation;
        });

        // 调用：过期的重建请求忽略
        callOnContext(() -> {
            subscriptionManager.requestRebuild(session, staleGeneration, group, null, "test");
            return null;
        });
        assertFalse(callOnContext(group::isRebuild));
        // 调用：当前代的重建请求生效
        callOnContext(() -> {
            subscriptionManager.requestRebuild(session, session.getGeneration(), group, null, "test");
            return null;
        });
        assertTrue(callOnContext(group::isRebuild));
    }

    // ========== 工具方法 ==========

    private static ItemBinding newItemBinding() {
        return new ItemBinding(DEVICE_ID, "temperature", NodeId.parse("ns=1;s=Temperature"), 0);
    }

    private static MonitoredItemSynchronizationException newSynchronizationException(StatusCode serviceResult,
                                                                                    StatusCode operationResult) {
        OpcUaMonitoredItem item = mock(OpcUaMonitoredItem.class);
        when(item.getUserObject()).thenReturn(Optional.of(newItemBinding()));
        return new MonitoredItemSynchronizationException("test",
                Collections.singletonList(new MonitoredItemServiceOperationResult(item, serviceResult, operationResult)),
                Collections.emptyList(), Collections.emptyList());
    }

    private <T> T callOnContext(Callable<T> callable) throws Exception {
        CompletableFuture<T> future = new CompletableFuture<>();
        context.runOnContext(v -> {
            try {
                future.complete(callable.call());
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future.get(5, TimeUnit.SECONDS);
    }

}
