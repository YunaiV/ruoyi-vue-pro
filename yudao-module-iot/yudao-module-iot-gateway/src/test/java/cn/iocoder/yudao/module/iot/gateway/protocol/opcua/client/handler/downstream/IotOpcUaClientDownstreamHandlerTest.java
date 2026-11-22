package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.downstream;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession.DeviceBinding;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSessionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static org.awaitility.Awaitility.await;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link IotOpcUaClientDownstreamHandler} 的单元测试：参数不合法、排队已满、超时、会话过期、配置过期、分批写入、ValueRank
 * <p>
 * 写入成功、节点不可写、服务端 StatusCode 等由 IotOpcUaClientProtocolIntegrationTest（进程内 Milo Server）覆盖
 *
 * @author 芋道源码
 */
public class IotOpcUaClientDownstreamHandlerTest {

    private static final Long DEVICE_ID = 1L;

    private Vertx vertx;
    private Context context;
    private OpcUaClient client;
    private IotOpcUaClientSession session;
    private DeviceBinding binding;
    /**
     * 设备当前绑定的会话（sessionManager.getDeviceSession 的返回值）
     */
    private volatile IotOpcUaClientSession deviceSession;
    private IotOpcUaClientConfig config;
    private IotOpcUaClientDownstreamHandler handler;

    /**
     * 回复：requestId → 结果（整条失败时为错误码）
     */
    private final Map<String, Object> replies = new ConcurrentHashMap<>();

    @BeforeEach
    public void setUp() {
        vertx = Vertx.vertx();
        context = vertx.getOrCreateContext();
        client = mock(OpcUaClient.class);
        // 会话：已激活，设备 1 有两个可写点位 speed、temp
        session = new IotOpcUaClientSession("session", context);
        session.setClient(client);
        session.setActive(true);
        session.setActiveConnectionEpoch(session.getConnectionEpoch().incrementAndGet());
        session.setMaxNodesPerRead(100);
        session.setMaxNodesPerWrite(100);
        IotOpcUaDeviceConfigRespDTO deviceConfig = new IotOpcUaDeviceConfigRespDTO();
        deviceConfig.setDeviceId(DEVICE_ID);
        deviceConfig.setPoints(Arrays.asList(buildPoint(11L, "speed"), buildPoint(12L, "temp")));
        binding = new DeviceBinding(deviceConfig);
        binding.getResolvedNodeIds().put(11L, new NodeId(2, "Speed"));
        binding.getResolvedNodeIds().put(12L, new NodeId(2, "Temp"));
        session.getDevices().put(DEVICE_ID, binding);
        deviceSession = session;
        IotOpcUaClientSessionManager sessionManager = mock(IotOpcUaClientSessionManager.class);
        when(sessionManager.getDeviceSession(DEVICE_ID)).thenAnswer(invocation -> deviceSession);
        // 回复
        IotDeviceMessageService messageService = mock(IotDeviceMessageService.class);
        doAnswer(invocation -> {
            IotDeviceMessage request = invocation.getArgument(0);
            Map<String, IotDevicePropertySetResultDTO> results = invocation.getArgument(1);
            ErrorCode failure = invocation.getArgument(2);
            replies.put(request.getRequestId(), failure != null ? failure : results);
            return null;
        }).when(messageService).sendPropertySetReply(any(), any(), any(), any());
        config = new IotOpcUaClientConfig();
        handler = new IotOpcUaClientDownstreamHandler(vertx, context, config, sessionManager, messageService, "server-1");
    }

    @AfterEach
    public void tearDown() throws Exception {
        vertx.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    @Test
    public void testHandle_paramsInvalid() {
        // 调用：params 为空 Map
        handler.handle(buildMessage("r1", Collections.emptyMap()));

        // 断言：整条消息失败
        ErrorCode failure = (ErrorCode) awaitReply("r1");
        assertEquals(PROPERTY_SET_PARAMS_INVALID.getCode(), failure.getCode());
        verifyNoInteractions(client);
    }

    @Test
    public void testHandle_queueFullAndTimeout() {
        // 准备参数：每设备最多 1 条排队，写入超时 300ms
        config.setMaxPendingWritesPerDevice(1);
        config.setWriteTimeout(300);
        // mock 方法：读取节点属性一直不返回
        when(client.readAsync(anyDouble(), any(), anyList())).thenReturn(new CompletableFuture<>());

        // 调用：第 1 条进入队列，第 2 条超过排队上限
        handler.handle(buildMessage("r1", Collections.singletonMap("speed", 1)));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(client).readAsync(anyDouble(), any(), anyList()));
        handler.handle(buildMessage("r2", Collections.singletonMap("speed", 2)));

        // 断言：第 2 条立即失败（排队已满）；第 1 条读取超时，不写入
        assertResult(awaitResults("r2").get("speed"), OPC_UA_WRITE_QUEUE_FULL);
        assertResult(awaitResults("r1").get("speed"), OPC_UA_WRITE_TIMEOUT);
        verify(client, never()).writeAsync(anyList());
        // 断言：名额已释放，之后的写入可以再次排队
        handler.handle(buildMessage("r3", Collections.singletonMap("speed", 3)));
        assertResult(awaitResults("r3").get("speed"), OPC_UA_WRITE_TIMEOUT);
    }

    @Test
    public void testHandle_sessionExpired() {
        // mock 方法：读取节点属性期间会话重连（代数变化）
        mockReadAttributes(-1, () -> session.nextGeneration());

        // 调用
        handler.handle(buildMessage("r1", Collections.singletonMap("speed", 1)));

        // 断言：会话过期，不写入（重连后的会话可能已经不是原来的状态）
        assertResult(awaitResults("r1").get("speed"), OPC_UA_WRITE_SESSION_EXPIRED);
        verify(client, never()).writeAsync(anyList());
    }

    @Test
    public void testHandle_configExpired_epochChanged() {
        // mock 方法：读取节点属性期间点位配置变化（禁用、删除、可写或 NodeId 变化都会递增 epoch）
        mockReadAttributes(-1, () -> binding.setEpoch(binding.getEpoch() + 1));

        // 调用
        handler.handle(buildMessage("r1", Collections.singletonMap("speed", 1)));

        // 断言：配置已过期，不用旧的 NodeId 写入
        assertResult(awaitResults("r1").get("speed"), OPC_UA_WRITE_CONFIG_EXPIRED);
        verify(client, never()).writeAsync(anyList());
    }

    @Test
    public void testHandle_configExpired_deviceRebound() {
        // mock 方法：读取节点属性期间设备解绑，并因端点 / 安全配置 / 凭据变化绑定到了其它会话
        IotOpcUaClientSession otherSession = new IotOpcUaClientSession("other", context);
        mockReadAttributes(-1, () -> {
            session.getDevices().remove(DEVICE_ID);
            deviceSession = otherSession;
        });

        // 调用
        handler.handle(buildMessage("r1", Collections.singletonMap("speed", 1)));

        // 断言：配置已过期，不在共享会话上用旧的 NodeId 写入
        assertResult(awaitResults("r1").get("speed"), OPC_UA_WRITE_CONFIG_EXPIRED);
        verify(client, never()).writeAsync(anyList());
    }

    @Test
    public void testHandle_writeBatchedAndMissingStatus() {
        // 准备参数：每次 Write 最多 1 个节点
        session.setMaxNodesPerWrite(1);
        mockReadAttributes(-1, null);
        // mock 方法：第 1 批返回 Good，第 2 批的 StatusCode 缺失
        WriteResponse goodResponse = mock(WriteResponse.class);
        when(goodResponse.getResults()).thenReturn(new StatusCode[]{StatusCode.GOOD});
        WriteResponse missingResponse = mock(WriteResponse.class);
        when(missingResponse.getResults()).thenReturn(new StatusCode[]{null});
        when(client.writeAsync(anyList())).thenReturn(CompletableFuture.completedFuture(goodResponse),
                CompletableFuture.completedFuture(missingResponse));

        // 调用
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("temp", 2);
        handler.handle(buildMessage("r1", params));

        // 断言：分两批写入，各 1 个节点；缺失的 StatusCode 只影响该项
        Map<String, IotDevicePropertySetResultDTO> results = awaitResults("r1");
        assertTrue(results.get("speed").succeeded(), String.valueOf(results.get("speed")));
        assertResult(results.get("temp"), OPC_UA_WRITE_FAILED);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WriteValue>> captor = ArgumentCaptor.forClass(List.class);
        verify(client, times(2)).writeAsync(captor.capture());
        assertEquals(1, captor.getAllValues().get(0).size());
        assertEquals(new NodeId(2, "Speed"), captor.getAllValues().get(0).get(0).getNodeId());
        assertEquals(1, captor.getAllValues().get(1).size());
        assertEquals(new NodeId(2, "Temp"), captor.getAllValues().get(1).get(0).getNodeId());
    }

    @Test
    public void testHandle_connectionChangedBeforeNextBatch() {
        // 准备参数：每次 Write 最多 1 个节点
        session.setMaxNodesPerWrite(1);
        mockReadAttributes(-1, null);
        // mock 方法：第 1 批写入后底层断线，Milo 在自己的线程上回调 onSessionInactive（只递增连接代数，context 尚未处理：
        //   active、generation 都没变，context 上的核对仍会通过）
        WriteResponse goodResponse = mock(WriteResponse.class);
        when(goodResponse.getResults()).thenReturn(new StatusCode[]{StatusCode.GOOD});
        when(client.writeAsync(anyList())).thenAnswer(invocation -> {
            session.getConnectionEpoch().incrementAndGet();
            return CompletableFuture.completedFuture(goodResponse);
        });

        // 调用
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("speed", 1);
        params.put("temp", 2);
        handler.handle(buildMessage("r1", params));

        // 断言：第 1 批已写入；第 2 批由 worker 上 writeAsync 之前的连接代数核对阻断，不写旧会话
        Map<String, IotDevicePropertySetResultDTO> results = awaitResults("r1");
        assertTrue(results.get("speed").succeeded(), String.valueOf(results.get("speed")));
        assertResult(results.get("temp"), OPC_UA_WRITE_SESSION_EXPIRED);
        verify(client, times(1)).writeAsync(anyList());
        assertTrue(session.isActive());
    }

    @Test
    public void testHandle_valueRankUndefinedNegative() {
        // mock 方法：ValueRank 为 -4（未定义的负值），不能按标量写入
        mockReadAttributes(-4, null);

        // 调用
        handler.handle(buildMessage("r1", Collections.singletonMap("speed", 1)));

        // 断言
        assertResult(awaitResults("r1").get("speed"), OPC_UA_WRITE_DATA_TYPE_NOT_SUPPORTED);
        verify(client, never()).writeAsync(anyList());
    }

    @Test
    public void testHandle_sessionInactive() {
        // 准备参数：会话未激活
        session.setActive(false);

        // 调用
        handler.handle(buildMessage("r1", Collections.singletonMap("speed", 1)));

        // 断言
        assertResult(awaitResults("r1").get("speed"), OPC_UA_WRITE_SESSION_INACTIVE);
        verifyNoInteractions(client);
    }

    private static IotOpcUaPointRespDTO buildPoint(Long id, String identifier) {
        IotOpcUaPointRespDTO point = new IotOpcUaPointRespDTO();
        point.setId(id);
        point.setIdentifier(identifier);
        point.setNodeId("ns=2;s=" + identifier);
        point.setWritable(true);
        return point;
    }

    /**
     * mock 读取节点属性：每个节点 DataType=Int32、指定的 ValueRank、可读可写；读取时执行 action（可为空）
     */
    private void mockReadAttributes(int valueRank, Runnable action) {
        when(client.readAsync(anyDouble(), any(), anyList())).thenAnswer(invocation -> {
            List<?> readValueIds = invocation.getArgument(2);
            List<DataValue> values = new ArrayList<>();
            for (int i = 0; i < readValueIds.size() / 4; i++) {
                values.add(new DataValue(new Variant(NodeIds.Int32)));
                values.add(new DataValue(new Variant(valueRank)));
                values.add(new DataValue(new Variant(ubyte(3))));
                values.add(new DataValue(new Variant(ubyte(3))));
            }
            if (action != null) {
                action.run();
            }
            ReadResponse response = mock(ReadResponse.class);
            when(response.getResults()).thenReturn(values.toArray(new DataValue[0]));
            return CompletableFuture.completedFuture(response);
        });
    }

    private static IotDeviceMessage buildMessage(String requestId, Map<String, Object> params) {
        IotDeviceMessage message = IotDeviceMessage.requestOf(requestId,
                IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), params);
        message.setDeviceId(DEVICE_ID);
        return message;
    }

    private Object awaitReply(String requestId) {
        await().atMost(10, TimeUnit.SECONDS).until(() -> replies.containsKey(requestId));
        return replies.get(requestId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, IotDevicePropertySetResultDTO> awaitResults(String requestId) {
        return (Map<String, IotDevicePropertySetResultDTO>) awaitReply(requestId);
    }

    private static void assertResult(IotDevicePropertySetResultDTO result, ErrorCode errorCode) {
        assertNotNull(result);
        assertFalse(result.succeeded(), String.valueOf(result));
        assertEquals(errorCode.getCode(), result.getCode(), String.valueOf(result));
    }

}
