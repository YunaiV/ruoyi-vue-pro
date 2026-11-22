package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.security.IotOpcUaClientSecurityFactory;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;
import static org.awaitility.Awaitility.await;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * {@link IotOpcUaClientProtocol} 的模拟验收测试：进程内 Milo Server
 * <p>
 * 覆盖 Read 轮询（分批、合并、拆分）、MonitoredItem 订阅、断线重连、Session 恢复、订阅重建、
 * 命名空间顺序变化后的 NodeId 解析、安全策略（只支持用户名认证：匿名与 None 拒绝、用户名 SignAndEncrypt、明文令牌拒绝、
 * 未信任证书拒绝、主机不一致拒绝）、
 * 属性写入（按节点实际 DataType 转换、访问级别、服务端 StatusCode、会话失效）
 *
 * @author 芋道源码
 */
public class IotOpcUaClientProtocolIntegrationTest {

    private static final Integer AUTH_ANONYMOUS = IotOpcUaAuthTypeEnum.ANONYMOUS.getType();
    private static final Integer AUTH_USERNAME = IotOpcUaAuthTypeEnum.USERNAME.getType();
    private static final Integer COLLECT_POLL = IotOpcUaCollectModeEnum.POLL.getMode();
    private static final Integer COLLECT_SUBSCRIBE = IotOpcUaCollectModeEnum.SUBSCRIBE.getMode();

    private static final String KEYSTORE_PASSWORD = "test-keystore-password";
    private static final String NSU = "nsu=" + IotOpcUaTestServer.NAMESPACE_URI + ";";

    private static IotOpcUaTestServer server;
    private static int port;

    @TempDir
    Path pkiDir;

    private IotOpcUaClientProtocol protocol;
    private final List<IotDeviceMessage> sentMessages = new CopyOnWriteArrayList<>();
    private final List<String> sentDeviceNames = new CopyOnWriteArrayList<>();
    /**
     * 属性设置的回复：请求 requestId → 每个属性的结果
     */
    private final Map<String, Map<String, IotDevicePropertySetResultDTO>> propertySetReplies = new ConcurrentHashMap<>();
    private volatile List<IotOpcUaDeviceConfigRespDTO> deviceConfigs = new ArrayList<>();
    private IotDeviceCommonApi deviceApi;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        port = IotOpcUaMiloCompatibilityTest.findFreePort();
        server = new IotOpcUaTestServer(port, Collections.emptyList());
        server.start();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (protocol != null) {
            protocol.stop();
        }
        // 恢复服务端（重启用例会修改命名空间顺序、节点值、OperationLimits）
        server.setMaxMonitoredItemsPerCall(null);
        server.restart(Collections.emptyList());
        server.setValue("Temperature", 25.5D);
        server.setValue("Counter", 1);
    }

    // ========== Read 轮询 + MonitoredItem 订阅 ==========

    @Test
    public void testPollAndSubscribe_basic256Sha256SignAndEncrypt() throws Exception {
        // 准备参数：分批读取 3 个节点一批，每条消息最多 4 个属性
        IotOpcUaClientConfig config = newConfig();
        config.setMaxNodesPerRead(3);
        config.setMaxPointsPerMessage(4);
        trustEachOther(config, true);
        IotOpcUaDeviceConfigRespDTO pollDevice = newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Arrays.asList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200),
                        newPoint(12L, "counter", NSU + "s=Counter", COLLECT_POLL, 200),
                        newPoint(13L, "name", NSU + "s=Name", COLLECT_POLL, 200),
                        newPoint(14L, "flag", NSU + "s=Flag", COLLECT_POLL, 200),
                        newPoint(15L, "array", NSU + "s=Array", COLLECT_POLL, 200),
                        newPoint(16L, "big", NSU + "s=Big", COLLECT_POLL, 200),
                        newPoint(17L, "bad", NSU + "s=Bad", COLLECT_POLL, 200),
                        newPoint(18L, "numeric", "ns=" + server.getNamespaceIndex() + ";i=1001", COLLECT_POLL, 200),
                        newPoint(19L, "missing", "nsu=urn:not-exists;s=X", COLLECT_POLL, 200)));
        IotOpcUaDeviceConfigRespDTO subscribeDevice = newDevice(2L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(21L, "temperature", NSU + "s=Temperature", COLLECT_SUBSCRIBE, 100)));
        deviceConfigs = Arrays.asList(pollDevice, subscribeDevice);

        // 调用
        startProtocol(config);

        // 断言：两个设备共用一个会话，都上线
        awaitState("dev1", true);
        awaitState("dev2", true);
        assertEquals(1, protocol.getSessionManager().getSessions().size());
        // 断言：轮询结果合并后按 4 个属性拆分；Bad 状态、无法解析的 NodeId 跳过；类型转换正确
        await().atMost(20, TimeUnit.SECONDS).until(() -> collectProperties("dev1").keySet().containsAll(
                Arrays.asList("temperature", "counter", "name", "flag", "array", "big", "numeric")));
        Map<String, Object> properties = collectProperties("dev1");
        assertEquals(25.5D, properties.get("temperature"));
        assertEquals(1, properties.get("counter"));
        assertEquals("pump", properties.get("name"));
        assertEquals(true, properties.get("flag"));
        assertEquals(Arrays.asList(1, 2, 3), properties.get("array"));
        assertEquals("18446744073709551615", properties.get("big"));
        assertEquals(42, properties.get("numeric"));
        assertFalse(properties.containsKey("bad"));
        assertFalse(properties.containsKey("missing"));
        getPropertyMessages("dev1").forEach(message -> assertTrue(getParams(message).size() <= 4));
        // 断言：订阅收到初始值和变化值
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev2", "temperature", 25.5D));
        server.setValue("Temperature", 30.5D);
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev2", "temperature", 30.5D));
    }

    @Test
    public void testPointChange_updateMonitoredItems() throws Exception {
        // 准备参数：先订阅 Temperature
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_SUBSCRIBE, 100))));
        startProtocol(config);
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "temperature", 25.5D));

        // 调用：点位改为订阅 Counter（下一次配置刷新生效）
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(12L, "counter", NSU + "s=Counter", COLLECT_SUBSCRIBE, 100))));
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "counter", 1));
        int before = getPropertyMessages("dev1").size();
        server.setValue("Temperature", 31.5D);
        server.setValue("Counter", 7);

        // 断言：新点位收到变化，旧点位的 MonitoredItem 已删除，不再上报
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "counter", 7));
        TimeUnit.MILLISECONDS.sleep(500);
        getPropertyMessages("dev1").stream().skip(before)
                .forEach(message -> assertFalse(getParams(message).containsKey("temperature")));
    }

    @Test
    public void testKeepAlive_idleSubscriptionKept() throws Exception {
        // 准备参数：发布间隔 100ms × KeepAlive 3 次，值不变化时服务端每 300ms 发送 KeepAlive
        IotOpcUaClientConfig config = newConfig();
        config.setSubscriptionKeepAliveCount(3);
        config.setSubscriptionLifetimeCount(9);
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(12L, "counter", NSU + "s=Counter", COLLECT_SUBSCRIBE, 100))));
        startProtocol(config);
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "counter", 1));
        IotOpcUaClientSession session = protocol.getSessionManager().getSessions().iterator().next();
        Object subscription = session.getSubscriptionGroups().get(100).getSubscription();
        long generation = session.getGeneration();

        // 调用：空闲 3 秒（远超看门狗的 KeepAlive 周期）
        TimeUnit.SECONDS.sleep(3);

        // 断言：KeepAlive 维持订阅，没有被看门狗判定超时而重建；会话没有失效
        assertTrue(session.isActive());
        assertEquals(generation, session.getGeneration());
        assertSame(subscription, session.getSubscriptionGroups().get(100).getSubscription());
        assertEquals(3, session.getSubscriptionGroups().get(100).getSubscription()
                .getRevisedMaxKeepAliveCount().get().intValue());
        server.setValue("Counter", 5);
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "counter", 5));
    }

    @Test
    public void testSubscribe_serverMaxMonitoredItemsPerCall() throws Exception {
        // 准备参数：服务端 MaxMonitoredItemsPerCall = 2（本地默认 100）；同一采集间隔 5 个订阅点位
        server.setMaxMonitoredItemsPerCall(2);
        server.restart(Collections.emptyList());
        server.getCreatedDataItemBatchSizes().clear();
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Arrays.asList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_SUBSCRIBE, 100),
                        newPoint(12L, "counter", NSU + "s=Counter", COLLECT_SUBSCRIBE, 100),
                        newPoint(13L, "name", NSU + "s=Name", COLLECT_SUBSCRIBE, 100),
                        newPoint(14L, "flag", NSU + "s=Flag", COLLECT_SUBSCRIBE, 100),
                        newPoint(15L, "array", NSU + "s=Array", COLLECT_SUBSCRIBE, 100))));

        // 调用
        startProtocol(config);

        // 断言：5 个点位都收到初始值
        await().atMost(20, TimeUnit.SECONDS).until(() -> collectProperties("dev1").keySet().containsAll(
                Arrays.asList("temperature", "counter", "name", "flag", "array")));
        // 断言：会话上限取 min(本地 100, 服务端 2)；服务端收到的每次 CreateMonitoredItems 都不超过 2 个
        IotOpcUaClientSession session = protocol.getSessionManager().getSessions().iterator().next();
        assertEquals(2, session.getMaxMonitoredItemsPerCall());
        List<Integer> batchSizes = new ArrayList<>(server.getCreatedDataItemBatchSizes());
        assertEquals(5, batchSizes.stream().mapToInt(Integer::intValue).sum(), batchSizes::toString);
        assertTrue(batchSizes.stream().allMatch(size -> size <= 2), batchSizes::toString);
    }

    // ========== 断线重连、Session 恢复、订阅重建、命名空间变化 ==========

    @Test
    public void testServerRestart_reconnectAndRebuildSubscription() throws Exception {
        // 准备参数
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Arrays.asList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200),
                        newPoint(12L, "counter", NSU + "s=Counter", COLLECT_SUBSCRIBE, 100))));
        startProtocol(config);
        awaitState("dev1", true);
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "counter", 1));
        IotOpcUaClientSession session = protocol.getSessionManager().getSessions().iterator().next();
        int oldNamespaceIndex = server.getNamespaceIndex();
        long oldGeneration = session.getGeneration();

        // 调用：服务端重启，测试命名空间之前多注册两个命名空间（索引变化）
        server.stop();
        awaitState("dev1", false);
        server.restart(Arrays.asList("urn:yudao:test:extra-a", "urn:yudao:test:extra-b"));

        // 断言：重新上线；命名空间索引已变化，nsu= 写法仍能读取；订阅已重建，能收到变化值
        await().atMost(60, TimeUnit.SECONDS).until(session::isActive);
        awaitState("dev1", true);
        assertNotEquals(oldNamespaceIndex, server.getNamespaceIndex());
        assertTrue(session.getGeneration() > oldGeneration + 1);
        server.setValue("Counter", 99);
        await().atMost(30, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "counter", 99));
        server.setValue("Temperature", 40.5D);
        await().atMost(30, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "temperature", 40.5D));
    }

    @Test
    public void testDeviceRemoved_offline() throws Exception {
        // 准备参数
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200))));
        startProtocol(config);
        awaitState("dev1", true);

        // 调用：设备删除（或禁用）
        deviceConfigs = Collections.emptyList();

        // 断言：发送离线消息，会话关闭
        awaitState("dev1", false);
        await().atMost(10, TimeUnit.SECONDS).until(() -> protocol.getSessionManager().getSessions().isEmpty());
    }

    // ========== 安全策略 ==========

    @Test
    public void testAnonymousAndSecurityNone_rejected() throws Exception {
        // 准备参数：只支持用户名认证。测试服务端本身允许匿名登录，用来证明是网关拒绝的：
        // 1）匿名 + SignAndEncrypt（即使带了有效的凭据别名）；2）用户名 + None
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        IotOpcUaDeviceConfigRespDTO anonymous = newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_ANONYMOUS, Collections.singletonList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200)));
        IotOpcUaDeviceConfigRespDTO none = newDevice(2L, server.getEndpointUrl(), "None", "None",
                AUTH_USERNAME, Collections.singletonList(newPoint(21L, "numeric", NSU + "i=1001", COLLECT_POLL, 200)));
        deviceConfigs = Arrays.asList(anonymous, none);

        // 调用
        startProtocol(config);

        // 断言：配置被拒绝，不建立会话，不上线、不上报
        verify(deviceApi, timeout(10000).atLeast(2)).getOpcUaDeviceConfigList(any());
        assertTrue(protocol.getSessionManager().getSessions().isEmpty());
        assertTrue(getPropertyMessages("dev1").isEmpty());
        assertTrue(getPropertyMessages("dev2").isEmpty());
        assertFalse(hasState("dev1", true));
        assertFalse(hasState("dev2", true));
    }

    @Test
    public void testUsername_aes128SignAndEncrypt() throws Exception {
        // 准备参数：网关本地凭据，租户、端点都命中
        IotOpcUaClientConfig config = newConfig();
        config.getCredentials().put("reader", newCredential(server.getEndpointUrl()));
        trustEachOther(config, true);
        IotOpcUaDeviceConfigRespDTO device = newDevice(1L, server.getEndpointUrl(), "Aes128_Sha256_RsaOaep",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200)));
        device.setCredentialAlias("reader");
        deviceConfigs = Collections.singletonList(device);

        // 调用
        startProtocol(config);

        // 断言
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "temperature", 25.5D));
    }

    @Test
    public void testUsername_rejected() throws Exception {
        // 准备参数：1）Sign 模式；2）凭据不允许该端点；3）端点只提供明文（None）用户名令牌
        IotOpcUaClientConfig config = newConfig();
        config.getCredentials().put("reader", newCredential(server.getEndpointUrl(), server.getPlainTokenEndpointUrl()));
        config.getCredentials().put("other", newCredential("opc.tcp://127.0.0.1:1/other"));
        trustEachOther(config, true);
        IotOpcUaDeviceConfigRespDTO sign = newDevice(1L, server.getEndpointUrl(), "Basic256Sha256", "Sign",
                AUTH_USERNAME, Collections.singletonList(newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200)));
        sign.setCredentialAlias("reader");
        IotOpcUaDeviceConfigRespDTO endpointMismatch = newDevice(2L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(newPoint(21L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200)));
        endpointMismatch.setCredentialAlias("other");
        IotOpcUaDeviceConfigRespDTO plainToken = newDevice(3L, server.getPlainTokenEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(newPoint(31L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200)));
        plainToken.setCredentialAlias("reader");
        deviceConfigs = Arrays.asList(sign, endpointMismatch, plainToken);

        // 调用
        startProtocol(config);

        // 断言：前两个配置被拒绝；明文令牌的端点选择失败，客户端不创建
        verify(deviceApi, timeout(10000).atLeast(3)).getOpcUaDeviceConfigList(any());
        assertEquals(1, protocol.getSessionManager().getSessions().size());
        IotOpcUaClientSession session = protocol.getSessionManager().getSessions().iterator().next();
        assertNull(session.getClient());
        assertTrue(getPropertyMessages("dev1").isEmpty());
        assertTrue(getPropertyMessages("dev2").isEmpty());
        assertTrue(getPropertyMessages("dev3").isEmpty());
        assertFalse(hasState("dev3", true));
    }

    @Test
    public void testUntrustedServerCertificate_rejected() throws Exception {
        // 准备参数：不信任服务端证书
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, false);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200))));

        // 调用
        startProtocol(config);

        // 断言：服务端证书进入 rejected 目录，设备不上线
        Path rejected = pkiDir.resolve("rejected");
        await().atMost(20, TimeUnit.SECONDS).until(() -> {
            try (Stream<Path> files = Files.list(rejected)) {
                return files.findAny().isPresent();
            }
        });
        TimeUnit.SECONDS.sleep(1);
        assertFalse(hasState("dev1", true));
        assertTrue(getPropertyMessages("dev1").isEmpty());
    }

    @Test
    public void testEndpointHostMismatch() throws Exception {
        // 准备参数：配置 localhost，服务端返回 127.0.0.1
        String endpointUrl = server.getEndpointUrl().replace("127.0.0.1", "localhost");
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, endpointUrl, "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newPoint(11L, "temperature", NSU + "s=Temperature", COLLECT_POLL, 200))));

        // 调用：默认拒绝
        startProtocol(config);
        verify(deviceApi, timeout(10000).atLeast(2)).getOpcUaDeviceConfigList(any());
        assertNull(protocol.getSessionManager().getSessions().iterator().next().getClient());
        protocol.stop();

        // 调用：开启 endpoint-host-rewrite 后可以连接
        config.setEndpointHostRewrite(true);
        startProtocol(config);
        await().atMost(20, TimeUnit.SECONDS).until(() -> hasPropertyValue("dev1", "temperature", 25.5D));
    }

    // ========== 属性写入 ==========

    @Test
    public void testPropertySet_writeAndReply() throws Exception {
        // 准备参数：可写的具体类型节点、只读节点、用户无写权限节点、数组节点、服务端拒绝的节点、未开启可写的点位
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Arrays.asList(
                        newWritablePoint(11L, "int16", NSU + "s=W.Int16"),
                        newWritablePoint(12L, "uint32", NSU + "s=W.UInt32"),
                        newWritablePoint(13L, "double", NSU + "s=W.Double"),
                        newWritablePoint(14L, "string", NSU + "s=W.String"),
                        newWritablePoint(15L, "flag", NSU + "s=W.Flag"),
                        newWritablePoint(16L, "time", NSU + "s=W.Time"),
                        newWritablePoint(17L, "overflow", NSU + "s=W.Int16"),
                        newWritablePoint(18L, "readOnly", NSU + "s=Counter"),
                        newWritablePoint(19L, "userDenied", NSU + "s=W.UserDenied"),
                        newWritablePoint(20L, "array", NSU + "s=W.Array"),
                        newWritablePoint(21L, "rejected", NSU + "s=W.Reject"),
                        newPoint(22L, "notWritable", NSU + "s=W.Double", COLLECT_POLL, 1000),
                        newWritablePoint(23L, "missing", "nsu=urn:not-exists;s=X"))));
        startProtocol(config);
        awaitState("dev1", true);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("int16", -1234);
        params.put("uint32", 4294967295L);
        params.put("double", "12.5");
        params.put("string", "on");
        params.put("flag", true);
        params.put("time", 1700000000000L);
        params.put("overflow", 40000);
        params.put("readOnly", 1);
        params.put("userDenied", 1);
        params.put("array", 1);
        params.put("rejected", 1);
        params.put("notWritable", 1.5D);
        params.put("missing", 1);
        params.put("unknown", 1);

        // 调用
        Map<String, IotDevicePropertySetResultDTO> results = propertySet("w1", params);

        // 断言：结果按参数顺序，每个属性一个结果
        assertEquals(new ArrayList<>(params.keySet()), new ArrayList<>(results.keySet()));
        // 断言：按节点实际的 DataType 写入；成功带回服务端的 Good 状态码
        for (String identifier : Arrays.asList("int16", "uint32", "double", "string", "flag", "time")) {
            assertTrue(results.get(identifier).succeeded(), identifier + " " + results.get(identifier));
            assertEquals("0x00000000", results.get(identifier).getStatusCode());
        }
        assertEquals((short) -1234, server.getValue("W.Int16"));
        assertEquals(uint(4294967295L), server.getValue("W.UInt32"));
        assertEquals(12.5D, server.getValue("W.Double"));
        assertEquals("on", server.getValue("W.String"));
        assertEquals(true, server.getValue("W.Flag"));
        assertEquals(new DateTime(Instant.ofEpochMilli(1700000000000L)), server.getValue("W.Time"));
        // 断言：超出 Int16 范围不截断；只读、用户无写权限带回对应的 Bad 状态码；数组不支持
        assertResult(results.get("overflow"), PROPERTY_SET_VALUE_INVALID, null);
        assertResult(results.get("readOnly"), OPC_UA_WRITE_NODE_NOT_WRITABLE, "0x803B0000");
        assertResult(results.get("userDenied"), OPC_UA_WRITE_NODE_NOT_WRITABLE, "0x801F0000");
        assertResult(results.get("array"), OPC_UA_WRITE_DATA_TYPE_NOT_SUPPORTED, null);
        assertEquals(0, server.getValue("W.UserDenied"));
        // 断言：服务端拒绝写入：StatusCode 原样带回
        assertResult(results.get("rejected"), OPC_UA_WRITE_STATUS_BAD, "0x803C0000");
        assertTrue(results.get("rejected").getMsg().contains("Bad_OutOfRange"), results.get("rejected").getMsg());
        // 断言：未开启可写、NodeId 无法解析、没有点位
        assertResult(results.get("notWritable"), PROPERTY_SET_POINT_NOT_WRITABLE, null);
        assertResult(results.get("missing"), OPC_UA_WRITE_NODE_ID_UNRESOLVED, null);
        assertResult(results.get("unknown"), PROPERTY_SET_POINT_NOT_EXISTS, null);
    }

    @Test
    public void testPropertySet_sessionInactive() throws Exception {
        // 准备参数
        IotOpcUaClientConfig config = newConfig();
        trustEachOther(config, true);
        deviceConfigs = Collections.singletonList(newDevice(1L, server.getEndpointUrl(), "Basic256Sha256",
                "SignAndEncrypt", AUTH_USERNAME, Collections.singletonList(
                        newWritablePoint(11L, "int16", NSU + "s=W.Int16"))));
        startProtocol(config);
        awaitState("dev1", true);

        // 调用：服务端停止，会话失效后写入
        server.stop();
        awaitState("dev1", false);
        Map<String, IotDevicePropertySetResultDTO> results = propertySet("w2", Collections.singletonMap("int16", 1));

        // 断言：会话未激活，直接失败
        assertResult(results.get("int16"), OPC_UA_WRITE_SESSION_INACTIVE, null);
        // 调用：设备不属于本网关
        Map<String, IotDevicePropertySetResultDTO> notConfigured = propertySet("w3", Collections.singletonMap("int16", 1),
                99L);
        assertResult(notConfigured.get("int16"), PROPERTY_SET_DEVICE_NOT_CONFIGURED, null);
    }

    // ========== 工具方法 ==========

    /**
     * 网关配置：默认带凭据 reader（只支持用户名认证），允许租户 1、测试服务端的端点（含 localhost 写法、明文令牌端点）
     */
    private IotOpcUaClientConfig newConfig() {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        config.setPkiDir(pkiDir.toString());
        config.setKeystorePassword(KEYSTORE_PASSWORD);
        config.setConfigRefreshInterval(1);
        config.setStateRetryInterval(1);
        config.getCredentials().put("reader", newCredential(server.getEndpointUrl(),
                server.getEndpointUrl().replace("127.0.0.1", "localhost"), server.getPlainTokenEndpointUrl()));
        return config;
    }

    private static IotOpcUaClientConfig.Credential newCredential(String... endpoints) {
        IotOpcUaClientConfig.Credential credential = new IotOpcUaClientConfig.Credential();
        credential.setUsername(IotOpcUaTestServer.USERNAME);
        credential.setPassword(IotOpcUaTestServer.PASSWORD);
        credential.setTenantIds(Collections.singletonList(1L));
        credential.setEndpoints(Arrays.asList(endpoints));
        return credential;
    }

    /**
     * 互相信任：预先生成客户端证书并加入服务端信任列表；按需把服务端证书放进客户端的 trusted/certs
     */
    private void trustEachOther(IotOpcUaClientConfig config, boolean trustServer) throws Exception {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);
        factory.initialize();
        factory.close();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(pkiDir.resolve("own").resolve("client.pfx"))) {
            keyStore.load(in, KEYSTORE_PASSWORD.toCharArray());
        }
        server.trust((X509Certificate) keyStore.getCertificate(keyStore.aliases().nextElement()));
        if (trustServer) {
            Files.write(pkiDir.resolve("trusted").resolve("certs").resolve("server.der"),
                    server.getCertificate().getEncoded());
        }
    }

    private void startProtocol(IotOpcUaClientConfig config) {
        sentMessages.clear();
        sentDeviceNames.clear();
        propertySetReplies.clear();
        deviceApi = mock(IotDeviceCommonApi.class);
        when(deviceApi.getOpcUaDeviceConfigList(any())).thenAnswer(invocation -> CommonResult.success(deviceConfigs));
        IotDeviceMessageService messageService = mock(IotDeviceMessageService.class);
        doAnswer(invocation -> {
            sentMessages.add(invocation.getArgument(0));
            sentDeviceNames.add(invocation.getArgument(2));
            return null;
        }).when(messageService).sendDeviceMessage(any(), any(), any(), any(), any());
        doAnswer(invocation -> {
            sentMessages.add(invocation.getArgument(0));
            sentDeviceNames.add(invocation.getArgument(2));
            return null;
        }).when(messageService).sendDeviceMessage(any(), any(), any(), any());
        doAnswer(invocation -> {
            IotDeviceMessage request = invocation.getArgument(0);
            Map<String, IotDevicePropertySetResultDTO> results = invocation.getArgument(1);
            propertySetReplies.put(request.getRequestId(), new LinkedHashMap<>(results));
            return null;
        }).when(messageService).sendPropertySetReply(any(), any(), isNull(), any());
        IotGatewayProperties.ProtocolProperties properties = new IotGatewayProperties.ProtocolProperties();
        properties.setId("opc-ua-client-test");
        properties.setProtocol("opc_ua_client");
        properties.setPort(4841);
        properties.setOpcUaClient(config);
        protocol = new IotOpcUaClientProtocol(properties, deviceApi, messageService, null);
        protocol.start();
    }

    private static IotOpcUaDeviceConfigRespDTO newDevice(Long deviceId, String endpointUrl, String securityPolicy,
                                                         String securityMode, Integer authType,
                                                         List<IotOpcUaPointRespDTO> points) {
        IotOpcUaDeviceConfigRespDTO device = new IotOpcUaDeviceConfigRespDTO();
        device.setDeviceId(deviceId);
        device.setTenantId(1L);
        device.setProductKey("pk");
        device.setDeviceName("dev" + deviceId);
        device.setEndpointUrl(endpointUrl);
        device.setSecurityPolicy(securityPolicy);
        device.setSecurityMode(securityMode);
        device.setAuthType(authType);
        device.setCredentialAlias("reader");
        device.setTimeout(3000);
        device.setRetryInterval(1000);
        device.setPoints(points);
        return device;
    }

    private static IotOpcUaPointRespDTO newPoint(Long id, String identifier, String nodeId, Integer collectMode,
                                                 Integer collectInterval) {
        IotOpcUaPointRespDTO point = new IotOpcUaPointRespDTO();
        point.setId(id);
        point.setIdentifier(identifier);
        point.setNodeId(nodeId);
        point.setCollectMode(collectMode);
        point.setCollectInterval(collectInterval);
        return point;
    }

    private static IotOpcUaPointRespDTO newWritablePoint(Long id, String identifier, String nodeId) {
        IotOpcUaPointRespDTO point = newPoint(id, identifier, nodeId, COLLECT_POLL, 1000);
        point.setWritable(true);
        return point;
    }

    private Map<String, IotDevicePropertySetResultDTO> propertySet(String requestId, Map<String, Object> params) {
        return propertySet(requestId, params, 1L);
    }

    /**
     * 下发属性设置，等待回复
     */
    private Map<String, IotDevicePropertySetResultDTO> propertySet(String requestId, Map<String, Object> params,
                                                                   Long deviceId) {
        IotDeviceMessage message = IotDeviceMessage.requestOf(requestId,
                IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), params);
        message.setDeviceId(deviceId);
        protocol.getDownstreamHandler().handle(message);
        await().atMost(20, TimeUnit.SECONDS).until(() -> propertySetReplies.containsKey(requestId));
        return propertySetReplies.get(requestId);
    }

    private static void assertResult(IotDevicePropertySetResultDTO result, ErrorCode errorCode, String statusCode) {
        assertNotNull(result);
        assertFalse(result.succeeded(), String.valueOf(result));
        assertEquals(errorCode.getCode(), result.getCode(), String.valueOf(result));
        if (statusCode != null) {
            assertEquals(statusCode, result.getStatusCode());
        }
    }

    private void awaitState(String deviceName, boolean online) {
        await().atMost(30, TimeUnit.SECONDS).until(() -> isLastState(deviceName, online));
    }

    private boolean isLastState(String deviceName, boolean online) {
        IotDeviceMessage last = null;
        for (int i = 0; i < sentMessages.size(); i++) {
            IotDeviceMessage message = sentMessages.get(i);
            if (deviceName.equals(sentDeviceNames.get(i))
                    && IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod().equals(message.getMethod())) {
                last = message;
            }
        }
        return last != null && isOnline(last) == online;
    }

    private boolean hasState(String deviceName, boolean online) {
        for (int i = 0; i < sentMessages.size(); i++) {
            IotDeviceMessage message = sentMessages.get(i);
            if (deviceName.equals(sentDeviceNames.get(i))
                    && IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod().equals(message.getMethod())
                    && isOnline(message) == online) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOnline(IotDeviceMessage stateMessage) {
        return IotDeviceMessage.buildStateUpdateOnline().getParams().equals(stateMessage.getParams());
    }

    private List<IotDeviceMessage> getPropertyMessages(String deviceName) {
        List<IotDeviceMessage> result = new ArrayList<>();
        for (int i = 0; i < sentMessages.size(); i++) {
            IotDeviceMessage message = sentMessages.get(i);
            if (deviceName.equals(sentDeviceNames.get(i))
                    && IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod().equals(message.getMethod())) {
                result.add(message);
            }
        }
        return result;
    }

    private Map<String, Object> collectProperties(String deviceName) {
        Map<String, Object> result = new java.util.HashMap<>();
        getPropertyMessages(deviceName).forEach(message -> result.putAll(getParams(message)));
        return result;
    }

    private boolean hasPropertyValue(String deviceName, String identifier, Object value) {
        return getPropertyMessages(deviceName).stream()
                .anyMatch(message -> value.equals(getParams(message).get(identifier)));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getParams(IotDeviceMessage message) {
        return (Map<String, Object>) message.getParams();
    }

}
