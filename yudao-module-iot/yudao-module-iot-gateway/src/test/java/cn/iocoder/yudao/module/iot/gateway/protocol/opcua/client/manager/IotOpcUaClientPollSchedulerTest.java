package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.upstream.IotOpcUaClientUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession.DeviceBinding;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotOpcUaClientPollScheduler} 的单元测试：分批读取结果的校验、提交读取失败时释放在途标记
 *
 * @author 芋道源码
 */
public class IotOpcUaClientPollSchedulerTest {

    private static final Integer COLLECT_POLL = IotOpcUaCollectModeEnum.POLL.getMode();

    private static final Long DEVICE_ID = 1L;
    private static final int INTERVAL = 50;
    private static final NodeId NODE_TEMPERATURE = NodeId.parse("ns=1;s=Temperature");
    private static final NodeId NODE_COUNTER = NodeId.parse("ns=1;s=Counter");

    private Vertx vertx;
    private Context context;
    private IotDeviceMessageService messageService;
    private IotOpcUaClientPollScheduler pollScheduler;
    private OpcUaClient client;
    private IotOpcUaClientSession session;

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        context = vertx.getOrCreateContext();
        messageService = mock(IotDeviceMessageService.class);
        pollScheduler = new IotOpcUaClientPollScheduler(vertx, context,
                new IotOpcUaClientUpstreamHandler(messageService, "server-1", 100, 65536));
        client = mock(OpcUaClient.class);
        // 会话已激活，每次 Read 1 个节点：两个点位分两批读取
        session = new IotOpcUaClientSession("session", context);
        session.setClient(client);
        session.setActive(true);
        session.setMaxNodesPerRead(1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testPoll_success() throws Exception {
        // mock 方法：每批返回与请求数一致的结果
        when(client.readValuesAsync(anyDouble(), any(TimestampsToReturn.class), anyList())).thenAnswer(invocation -> {
            List<NodeId> nodeIds = invocation.getArgument(2);
            List<DataValue> values = new ArrayList<>();
            nodeIds.forEach(nodeId -> values.add(NODE_TEMPERATURE.equals(nodeId)
                    ? new DataValue(new Variant(25.5D)) : new DataValue(new Variant(7))));
            return CompletableFuture.completedFuture(values);
        });

        // 调用
        startPoll();
        // 断言：两批结果合并为一条属性上报
        verify(messageService, timeout(5000).atLeastOnce()).sendDeviceMessage(argThat((IotDeviceMessage message) -> {
            Map<?, ?> params = (Map<?, ?>) message.getParams();
            return params.size() == 2 && params.get("temperature").equals(25.5D) && params.get("counter").equals(7);
        }), eq("pk"), eq("dev1"), eq("server-1"), any());
    }

    @Test
    public void testPoll_resultSizeMismatchDiscarded() throws Exception {
        // mock 方法：第二批（Counter）返回的结果比请求多，无法按位置对应到点位
        when(client.readValuesAsync(anyDouble(), any(TimestampsToReturn.class), anyList())).thenAnswer(invocation -> {
            List<NodeId> nodeIds = invocation.getArgument(2);
            if (nodeIds.contains(NODE_COUNTER)) {
                return CompletableFuture.completedFuture(Arrays.asList(new DataValue(new Variant(7)),
                        new DataValue(new Variant(8))));
            }
            return CompletableFuture.completedFuture(Collections.singletonList(new DataValue(new Variant(25.5D))));
        });

        // 调用
        startPoll();
        // 断言：持续轮询（在途标记已释放），但整轮丢弃，不上报
        verify(client, timeout(5000).atLeast(6)).readValuesAsync(anyDouble(), any(TimestampsToReturn.class), anyList());
        verify(messageService, never()).sendDeviceMessage(any(), any(), any(), any(), any());
    }

    @Test
    public void testPoll_submitThrowReleaseInFlight() throws Exception {
        // mock 方法：提交读取时同步抛出（例如客户端已断开）
        when(client.readValuesAsync(anyDouble(), any(TimestampsToReturn.class), anyList()))
                .thenThrow(new IllegalStateException("client closed"));

        // 调用
        startPoll();
        // 断言：在途标记已释放，后续定时照常读取（不会因第一次失败而一直跳过）
        verify(client, timeout(5000).atLeast(3)).readValuesAsync(anyDouble(), any(TimestampsToReturn.class), anyList());
        verify(messageService, never()).sendDeviceMessage(any(), any(), any(), any(), any());
    }

    private void startPoll() throws Exception {
        IotOpcUaDeviceConfigRespDTO config = new IotOpcUaDeviceConfigRespDTO();
        config.setDeviceId(DEVICE_ID);
        config.setTenantId(1L);
        config.setProductKey("pk");
        config.setDeviceName("dev1");
        config.setPoints(Arrays.asList(newPoint(11L, "temperature", "ns=1;s=Temperature"),
                newPoint(12L, "counter", "ns=1;s=Counter")));
        DeviceBinding binding = new DeviceBinding(config);
        binding.getResolvedNodeIds().put(11L, NODE_TEMPERATURE);
        binding.getResolvedNodeIds().put(12L, NODE_COUNTER);
        CompletableFuture<Void> future = new CompletableFuture<>();
        context.runOnContext(v -> {
            session.getDevices().put(DEVICE_ID, binding);
            pollScheduler.updateDevice(session, binding);
            future.complete(null);
        });
        future.get(5, TimeUnit.SECONDS);
        assertEquals(1, session.getDevices().size());
    }

    private static IotOpcUaPointRespDTO newPoint(Long id, String identifier, String nodeId) {
        IotOpcUaPointRespDTO point = new IotOpcUaPointRespDTO();
        point.setId(id);
        point.setIdentifier(identifier);
        point.setNodeId(nodeId);
        point.setCollectMode(COLLECT_POLL);
        point.setCollectInterval(INTERVAL);
        return point;
    }

}
