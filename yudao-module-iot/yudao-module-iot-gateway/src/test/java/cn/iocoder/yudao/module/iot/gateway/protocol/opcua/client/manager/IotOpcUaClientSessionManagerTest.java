package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.security.IotOpcUaClientSecurityFactory;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.OperationLimits;
import org.eclipse.milo.opcua.sdk.client.SessionActivityListener;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * {@link IotOpcUaClientSessionManager} 的单元测试：会话激活后的 OperationLimits 上限、订阅同步与上线时机，会话 key 的重试间隔隔离
 *
 * @author 芋道源码
 */
public class IotOpcUaClientSessionManagerTest {

    private static final Integer AUTH_USERNAME = IotOpcUaAuthTypeEnum.USERNAME.getType();

    private static final String ENDPOINT = "opc.tcp://192.168.1.10:4840/server";

    private Vertx vertx;
    private Context context;
    private IotOpcUaClientSecurityFactory securityFactory;
    private IotOpcUaDeviceStateTracker stateTracker;
    private IotOpcUaClientSubscriptionManager subscriptionManager;
    private IotOpcUaClientSessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        context = vertx.getOrCreateContext();
        securityFactory = mock(IotOpcUaClientSecurityFactory.class);
        stateTracker = mock(IotOpcUaDeviceStateTracker.class);
        subscriptionManager = mock(IotOpcUaClientSubscriptionManager.class);
        sessionManager = new IotOpcUaClientSessionManager(vertx, context, new IotOpcUaClientConfig(), securityFactory,
                stateTracker, mock(IotOpcUaClientPollScheduler.class), subscriptionManager);
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testHandleSessionActive_afterOperationLimits() throws Exception {
        // mock 方法：创建客户端成功，连接不完成（由测试触发会话激活）；OperationLimits 读取阻塞，直到放行；
        // 服务端上限低于本地配置（MaxNodesPerRead 10 < 100，MaxMonitoredItemsPerCall 2 < 100，MaxNodesPerWrite 5 < 100）
        OpcUaClient client = mock(OpcUaClient.class, RETURNS_DEEP_STUBS);
        when(client.connectAsync()).thenReturn(new CompletableFuture<>());
        when(client.getNamespaceTable()).thenReturn(new NamespaceTable());
        CountDownLatch operationLimitsLatch = new CountDownLatch(1);
        OperationLimits limits = mock(OperationLimits.class);
        when(limits.maxNodesPerRead()).thenReturn(Optional.of(uint(10)));
        when(limits.maxMonitoredItemsPerCall()).thenReturn(Optional.of(uint(2)));
        when(limits.maxNodesPerWrite()).thenReturn(Optional.of(uint(5)));
        when(client.readOperationLimits()).thenAnswer(invocation -> {
            assertTrue(operationLimitsLatch.await(10, TimeUnit.SECONDS));
            return limits;
        });
        when(securityFactory.createClient(any())).thenReturn(client);
        IotOpcUaDeviceConfigRespDTO device1 = newDevice(1L, 10000);
        IotOpcUaDeviceConfigRespDTO device2 = newDevice(2L, 10000);
        callOnContext(() -> {
            sessionManager.applyConfigs(Collections.singletonList(device1), Collections.emptySet());
            return null;
        });
        IotOpcUaClientSession session = callOnContext(() -> sessionManager.getSessions().iterator().next());
        await().atMost(5, TimeUnit.SECONDS).until(() -> callOnContext(() -> session.getClient() != null));
        verify(stateTracker).offline(eq(device1));

        // 调用：会话激活；OperationLimits 读取完成前，又挂入一个设备
        callOnContext(() -> {
            sessionManager.handleSessionActive(session, client, session.getConnectionEpoch().incrementAndGet());
            sessionManager.applyConfigs(Arrays.asList(device1, device2), Collections.emptySet());
            return null;
        });
        // 断言：会话已激活，但 OperationLimits 读取完成前不同步订阅、不上线（与一期激活顺序一致）
        assertTrue(callOnContext(session::isActive));
        assertNull(callOnContext(session::getMaxNodesPerRead));
        assertNull(callOnContext(session::getMaxMonitoredItemsPerCall));
        assertNull(callOnContext(session::getMaxNodesPerWrite));
        verify(subscriptionManager, never()).onSessionActive(any(), anyBoolean());
        verify(stateTracker, never()).online(any());

        // 调用：OperationLimits 读取完成
        operationLimitsLatch.countDown();
        // 断言：取本地与服务端的较小值后，先同步订阅，再让会话内的设备都上线
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(stateTracker).online(eq(device1));
            verify(stateTracker).online(eq(device2));
        });
        assertEquals(10, callOnContext(session::getMaxNodesPerRead));
        assertEquals(2, callOnContext(session::getMaxMonitoredItemsPerCall));
        assertEquals(5, callOnContext(session::getMaxNodesPerWrite));
        InOrder inOrder = inOrder(subscriptionManager, stateTracker);
        inOrder.verify(subscriptionManager).onSessionActive(eq(session), eq(false));
        inOrder.verify(stateTracker, times(2)).online(any());

        assertEquals(session.getConnectionEpoch().get(), session.getActiveConnectionEpoch());

        // 调用：底层断线，Milo 在自己的线程上回调 onSessionInactive；此时 context 被占用，还没处理失效
        ArgumentCaptor<SessionActivityListener> listenerCaptor = ArgumentCaptor.forClass(SessionActivityListener.class);
        verify(client).addSessionActivityListener(listenerCaptor.capture());
        CountDownLatch contextLatch = new CountDownLatch(1);
        context.runOnContext(v -> {
            try {
                contextLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        listenerCaptor.getValue().onSessionInactive(null);
        // 断言：连接代数立即变化（写入据此阻断），而 active / generation 要等切回 context 才更新
        assertNotEquals(session.getConnectionEpoch().get(), session.getActiveConnectionEpoch());
        assertTrue(session.isActive());

        // 调用：context 处理失效后，上限清空，等下次激活重新读取
        contextLatch.countDown();
        await().atMost(5, TimeUnit.SECONDS).until(() -> !session.isActive());
        assertNull(callOnContext(session::getMaxMonitoredItemsPerCall));
        assertNull(callOnContext(session::getMaxNodesPerWrite));
    }

    @Test
    public void testApplyConfigs_retryIntervalSplitsSession() throws Exception {
        // mock 方法：创建客户端失败，会话按重试间隔重试
        when(securityFactory.createClient(any())).thenThrow(new IllegalStateException("test"));
        IotOpcUaDeviceConfigRespDTO device1 = newDevice(1L, 10000);
        IotOpcUaDeviceConfigRespDTO device2 = newDevice(2L, 10000);
        IotOpcUaDeviceConfigRespDTO device3 = newDevice(3L, 30000);

        // 调用
        callOnContext(() -> {
            sessionManager.applyConfigs(Arrays.asList(device1, device2, device3), Collections.emptySet());
            return null;
        });

        // 断言：重试间隔相同的设备共用会话，不同的分到两个会话；每个会话的重试间隔确定
        List<IotOpcUaClientSession> sessions = callOnContext(() -> sessionManager.getSessions().stream()
                .sorted(Comparator.comparing(s -> s.getDevices().size()))
                .collect(Collectors.toList()));
        assertEquals(2, sessions.size());
        assertEquals(Collections.singleton(3L), callOnContext(() -> sessions.get(0).getDevices().keySet()));
        assertEquals(30000, callOnContext(() -> sessions.get(0).getAnyDeviceConfig().getRetryInterval()));
        assertEquals(Arrays.asList(1L, 2L), callOnContext(() -> sessions.get(1).getDevices().keySet().stream()
                .sorted().collect(Collectors.toList())));
        assertEquals(10000, callOnContext(() -> sessions.get(1).getAnyDeviceConfig().getRetryInterval()));
    }

    // ========== 工具方法 ==========

    private static IotOpcUaDeviceConfigRespDTO newDevice(Long deviceId, Integer retryInterval) {
        IotOpcUaDeviceConfigRespDTO device = new IotOpcUaDeviceConfigRespDTO();
        device.setDeviceId(deviceId);
        device.setTenantId(1L);
        device.setProductKey("pk");
        device.setDeviceName("dev" + deviceId);
        device.setEndpointUrl(ENDPOINT);
        device.setSecurityPolicy("Basic256Sha256");
        device.setSecurityMode("SignAndEncrypt");
        device.setAuthType(AUTH_USERNAME);
        device.setCredentialAlias("reader");
        device.setTimeout(3000);
        device.setRetryInterval(retryInterval);
        device.setPoints(Collections.emptyList());
        return device;
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
