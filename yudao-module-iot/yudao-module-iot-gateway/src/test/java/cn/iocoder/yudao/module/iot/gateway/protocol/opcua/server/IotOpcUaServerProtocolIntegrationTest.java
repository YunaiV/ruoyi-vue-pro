package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerNodeRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import org.eclipse.milo.opcua.sdk.client.DiscoveryClient;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaSubscription;
import org.eclipse.milo.opcua.sdk.server.AccessContext;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.sdk.server.identity.DefaultUsernameIdentity;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.security.CertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link IotOpcUaServerProtocol} 的集成测试：进程内启动 OPC UA Server，用芋道 Milo 客户端验证
 * 只读节点、写入拒绝、MonitoredItem、租户隔离（含跨租户消息不进入重建缓存、未登记所属配置的节点 fail-closed）、
 * 重建保留真实上报的值和时间戳、重建不覆盖并发上报、会话数上限、匿名和 None 安全通道拒绝
 *
 * @author 芋道源码
 */
public class IotOpcUaServerProtocolIntegrationTest {

    /**
     * 配置的端点路径：故意带连续的 / 和末尾的 /，验证规范化
     */
    private static final String CONFIG_PATH = "//yudao-iot//";
    /**
     * 规范化后的端点路径
     */
    private static final String PATH = "/yudao-iot";

    @TempDir
    Path tempDir;

    private int port;
    private IotOpcUaServerProtocol protocol;
    private KeyPair clientKeyPair;
    private X509Certificate clientCertificate;
    private final List<OpcUaClient> clients = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        // 1. 协议配置：两个租户各一个凭据
        IotOpcUaServerConfig config = new IotOpcUaServerConfig();
        config.setHostname("127.0.0.1");
        config.setBindAddress("127.0.0.1");
        config.setPath(CONFIG_PATH);
        config.setPkiDir(tempDir.resolve("pki").toString());
        config.setKeystorePassword("test-keystore");
        config.getCredentials().put("scada-a", newCredential("scada-a", "secret-a", 1L));
        config.getCredentials().put("scada-b", newCredential("scada-b", "secret-b", 2L));
        ProtocolProperties properties = new ProtocolProperties();
        properties.setId("opc-ua-server-test");
        properties.setProtocol("opc_ua_server");
        properties.setOpcUaServer(config);

        // 1.1 客户端证书：启动前放入服务端信任列表目录（pki/trusted/certs）
        clientKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        clientCertificate = new SelfSignedCertificateBuilder(clientKeyPair).setCountryCode("CN").setCommonName("test client")
                .setApplicationUri("urn:yudao:test:client").addDnsName("localhost").build();
        Path trustedDir = tempDir.resolve("pki").resolve("trusted").resolve("certs");
        Files.createDirectories(trustedDir);
        Files.write(trustedDir.resolve("test-client.der"), clientCertificate.getEncoded());

        // 2. biz 返回的配置：租户 1 暴露设备 1 的温度，租户 2 暴露设备 2 的计数
        IotDeviceCommonApi deviceApi = mock(IotDeviceCommonApi.class);
        when(deviceApi.getOpcUaServerConfigList()).thenReturn(CommonResult.success(Arrays.asList(
                newConfig(1L, 1L, "scada-a", newNode(11L, "temperature", 1L, "temperature", "double")),
                newConfig(2L, 2L, "scada-b", newNode(21L, "counter", 2L, "counter", "int")))));

        // 3. 启动：探测到的空闲端口可能在绑定前被其它连接占用（例如客户端连接的本地端口），绑定失败时换端口重试
        for (int attempt = 1; ; attempt++) {
            port = findFreePort();
            properties.setPort(port);
            protocol = new IotOpcUaServerProtocol(properties, deviceApi, null);
            try {
                protocol.start();
                break;
            } catch (IllegalStateException e) {
                protocol = null;
                if (attempt >= 5) {
                    throw e;
                }
            }
        }
        await().atMost(10, TimeUnit.SECONDS).until(() -> protocol.getNamespace().getConfigs().size() == 2);
    }

    /**
     * 探测空闲端口：关闭 SO_REUSEADDR，避免拿到仍处于 TIME_WAIT 的端口（Milo 绑定时不复用地址）
     */
    private static int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(false);
            socket.bind(new InetSocketAddress("127.0.0.1", 0));
            return socket.getLocalPort();
        }
    }

    @AfterEach
    public void tearDown() {
        clients.forEach(client -> {
            try {
                client.disconnect();
            } catch (Exception ignored) {
            }
        });
        if (protocol != null) {
            protocol.stop();
        }
    }

    @Test
    public void testEndpoints_noneOnlyForDiscovery() throws Exception {
        List<EndpointDescription> endpoints = getEndpoints();
        // 断言：会话端点都是 SignAndEncrypt，没有 None 会话端点；只有用户名令牌
        assertFalse(endpoints.isEmpty());
        for (EndpointDescription endpoint : endpoints) {
            assertEquals(MessageSecurityMode.SignAndEncrypt, endpoint.getSecurityMode());
            assertNotEquals(SecurityPolicy.None.getUri(), endpoint.getSecurityPolicyUri());
            assertEquals(1, endpoint.getUserIdentityTokens().length);
            assertEquals(SecurityPolicy.Basic256Sha256.getUri(), endpoint.getUserIdentityTokens()[0].getSecurityPolicyUri());
        }
    }

    @Test
    public void testEndpoints_normalizedPathAndDiscoveryFallback() throws Exception {
        String endpointUrl = "opc.tcp://127.0.0.1:" + port + PATH;
        List<EndpointDescription> endpoints = getEndpoints();
        // 断言：配置的路径带多余的 /，会话端点 URL 仍是规范化后的路径
        assertFalse(endpoints.isEmpty());
        for (EndpointDescription endpoint : endpoints) {
            assertEquals(endpointUrl, endpoint.getEndpointUrl());
            // 调用：客户端回退时在会话端点 URL 后拼接 /discovery；断言：能取到同一组端点
            List<EndpointDescription> fallback = DiscoveryClient.getEndpoints(endpoint.getEndpointUrl() + "/discovery")
                    .get(5, TimeUnit.SECONDS);
            assertEquals(endpoints.size(), fallback.size());
        }
        // 调用：直接对会话端点 URL 调用 GetEndpoints；断言：同样可用
        assertEquals(endpoints.size(), DiscoveryClient.getEndpoints(endpointUrl).get(5, TimeUnit.SECONDS).size());
    }

    @Test
    public void testRead_readOnlyAndWriteDenied() throws Exception {
        OpcUaClient client = connect(new UsernameProvider("scada-a", "secret-a"));
        NodeId nodeA = protocol.getNamespace().buildVariableNodeId(1L, "temperature");

        // 断言：收到属性上报前，节点状态为 Bad_WaitingForInitialData
        assertEquals(StatusCodes.Bad_WaitingForInitialData, readValue(client, nodeA).getStatusCode().getValue());

        // 调用：设备 1 上报温度
        protocol.handleDeviceMessage(newPropertyPost(1L, 1L, Collections.singletonMap("temperature", 25.5D)));
        // 断言：读到最新值；AccessLevel / UserAccessLevel 只有 CurrentRead
        DataValue value = readValue(client, nodeA);
        assertTrue(value.getStatusCode().isGood());
        assertEquals(25.5D, value.getValue().getValue());
        assertEquals(UByte.valueOf(1), readAttribute(client, nodeA, AttributeId.AccessLevel).getValue().getValue());
        assertEquals(UByte.valueOf(1), readAttribute(client, nodeA, AttributeId.UserAccessLevel).getValue().getValue());

        // 调用：写入；断言：拒绝（Milo 访问控制按 UserAccessLevel 先拒绝），值不变
        StatusCode status = client.writeValues(Collections.singletonList(nodeA),
                Collections.singletonList(new DataValue(new Variant(99.9D)))).get(0);
        assertEquals(StatusCodes.Bad_UserAccessDenied, status.getValue());
        assertEquals(25.5D, readValue(client, nodeA).getValue().getValue());

        // 调用：其它租户的设备、租户不一致的上报；断言：不更新
        protocol.handleDeviceMessage(newPropertyPost(1L, 2L, Collections.singletonMap("temperature", 1.0D)));
        assertEquals(25.5D, readValue(client, nodeA).getValue().getValue());
    }

    @Test
    public void testCrossTenantMessage_notCachedForRebuild() throws Exception {
        OpcUaClient client = connect(new UsernameProvider("scada-a", "secret-a"));
        NodeId nodeA = protocol.getNamespace().buildVariableNodeId(1L, "temperature");

        // 调用：租户 2 冒用设备 1 上报温度（设备 1 只绑定在租户 1 的配置下）
        protocol.handleDeviceMessage(newPropertyPost(1L, 2L, Collections.singletonMap("temperature", 66.6D)));
        // 调用：配置变化，重建租户 1 的文件夹和变量
        List<IotOpcUaServerConfigRespDTO> configs = new ArrayList<>(protocol.getNamespace().getConfigs());
        IotOpcUaServerConfigRespDTO renamed = newConfig(1L, 1L, "scada-a", newNode(11L, "temperature", 1L, "temperature", "double"));
        renamed.setName("config-1-renamed");
        configs.set(0, renamed);
        protocol.getNamespace().applyConfigs(configs);

        // 断言：重建后的变量没有带入其它租户的消息
        DataValue value = readValue(client, nodeA);
        assertEquals(StatusCodes.Bad_WaitingForInitialData, value.getStatusCode().getValue());

        // 调用：本租户上报后再重建；断言：重建后保留本租户的最近值
        protocol.handleDeviceMessage(newPropertyPost(1L, 1L, Collections.singletonMap("temperature", 30.5D)));
        renamed = newConfig(1L, 1L, "scada-a", newNode(11L, "temperature", 1L, "temperature", "double"));
        renamed.setName("config-1-renamed-again");
        configs.set(0, renamed);
        protocol.getNamespace().applyConfigs(configs);
        assertEquals(30.5D, readValue(client, nodeA).getValue().getValue());
    }

    @Test
    public void testRebuild_keepsReportedValueAndTimestamp() throws Exception {
        OpcUaClient client = connect(new UsernameProvider("scada-a", "secret-a"));
        IotOpcUaServerNamespace namespace = protocol.getNamespace();
        NodeId nodeA = namespace.buildVariableNodeId(1L, "temperature");

        // 调用：设备 1 上报温度，SourceTimestamp 为消息的上报时间
        LocalDateTime reportTime = LocalDateTime.of(2026, 11, 21, 10, 0, 0);
        IotDeviceMessage message = newPropertyPost(1L, 1L, Collections.singletonMap("temperature", 26.5D));
        message.setReportTime(reportTime);
        protocol.handleDeviceMessage(message);
        DataValue reported = readValue(client, nodeA);
        assertEquals(reportTime.atZone(ZoneId.systemDefault()).toInstant(), reported.getSourceTime().getJavaInstant());

        // 调用：配置变化重建变量、配置未变化再次应用
        Thread.sleep(20);
        List<IotOpcUaServerConfigRespDTO> configs = new ArrayList<>(namespace.getConfigs());
        IotOpcUaServerConfigRespDTO renamed = newConfig(1L, 1L, "scada-a", newNode(11L, "temperature", 1L, "temperature", "double"));
        renamed.setName("config-1-renamed");
        configs.set(0, renamed);
        namespace.applyConfigs(configs);
        DataValue rebuilt = readValue(client, nodeA);
        namespace.applyConfigs(configs);
        DataValue reapplied = readValue(client, nodeA);

        // 断言：沿用最后一次上报的值和 SourceTimestamp，不生成新的时间戳
        for (DataValue value : Arrays.asList(rebuilt, reapplied)) {
            assertTrue(value.getStatusCode().isGood());
            assertEquals(26.5D, value.getValue().getValue());
            assertEquals(reported.getSourceTime(), value.getSourceTime());
        }
    }

    @Test
    public void testUpdateProperties_outOfOrderReport() throws Exception {
        OpcUaClient client = connect(new UsernameProvider("scada-a", "secret-a"));
        IotOpcUaServerNamespace namespace = protocol.getNamespace();
        NodeId nodeA = namespace.buildVariableNodeId(1L, "temperature");
        LocalDateTime newer = LocalDateTime.of(2026, 11, 21, 10, 0, 5);
        LocalDateTime older = newer.minusSeconds(5);

        // 调用：先到达较新的报告，再到达较旧的报告；断言：旧报告不覆盖新报告
        protocol.handleDeviceMessage(newTimedPropertyPost(30.0D, newer));
        protocol.handleDeviceMessage(newTimedPropertyPost(10.0D, older));
        DataValue value = readValue(client, nodeA);
        assertEquals(30.0D, value.getValue().getValue());
        assertEquals(newer.atZone(ZoneId.systemDefault()).toInstant(), value.getSourceTime().getJavaInstant());

        // 调用：配置重建；断言：重建后仍是较新的报告（旧报告没有进入重建缓存）
        List<IotOpcUaServerConfigRespDTO> configs = new ArrayList<>(namespace.getConfigs());
        IotOpcUaServerConfigRespDTO renamed = newConfig(1L, 1L, "scada-a", newNode(11L, "temperature", 1L, "temperature", "double"));
        renamed.setName("config-1-renamed");
        configs.set(0, renamed);
        namespace.applyConfigs(configs);
        assertEquals(30.0D, readValue(client, nodeA).getValue().getValue());

        // 调用：相同时间的报告；断言：允许更新
        protocol.handleDeviceMessage(newTimedPropertyPost(31.0D, newer));
        assertEquals(31.0D, readValue(client, nodeA).getValue().getValue());

        // 调用：没有时间戳的报告；断言：沿用兼容语义直接更新，SourceTimestamp 为网关收到的时间
        IotDeviceMessage untimed = newTimedPropertyPost(32.0D, null);
        protocol.handleDeviceMessage(untimed);
        value = readValue(client, nodeA);
        assertEquals(32.0D, value.getValue().getValue());
        assertTrue(value.getSourceTime().getJavaInstant().isAfter(newer.atZone(ZoneId.systemDefault()).toInstant()));
    }

    @Test
    public void testRebuild_doesNotOverwriteConcurrentReport() throws Exception {
        OpcUaClient client = connect(new UsernameProvider("scada-a", "secret-a"));
        IotOpcUaServerNamespace namespace = protocol.getNamespace();
        NodeId nodeA = namespace.buildVariableNodeId(1L, "temperature");
        List<IotOpcUaServerConfigRespDTO> baseConfigs = new ArrayList<>(namespace.getConfigs());
        int reports = 3000;

        // 调用：一个线程持续上报递增的温度，另一个线程持续重建租户 1 的配置
        CountDownLatch start = new CountDownLatch(1);
        Thread reporter = new Thread(() -> {
            awaitLatch(start);
            for (int i = 1; i <= reports; i++) {
                protocol.handleDeviceMessage(newPropertyPost(1L, 1L, Collections.singletonMap("temperature", (double) i)));
            }
        });
        Thread rebuilder = new Thread(() -> {
            awaitLatch(start);
            for (int i = 0; i < 100; i++) {
                List<IotOpcUaServerConfigRespDTO> configs = new ArrayList<>(baseConfigs);
                IotOpcUaServerConfigRespDTO renamed = newConfig(1L, 1L, "scada-a",
                        newNode(11L, "temperature", 1L, "temperature", "double"));
                renamed.setName("config-1-" + i);
                configs.set(0, renamed);
                namespace.applyConfigs(configs);
            }
        });
        reporter.start();
        rebuilder.start();
        start.countDown();
        reporter.join(TimeUnit.SECONDS.toMillis(30));
        rebuilder.join(TimeUnit.SECONDS.toMillis(30));
        assertFalse(reporter.isAlive());
        assertFalse(rebuilder.isAlive());

        // 断言：最终值是最后一次上报，没有被重建时读到的旧缓存覆盖
        assertEquals((double) reports, readValue(client, nodeA).getValue().getValue());
    }

    @Test
    public void testIsVisible_failClosedForUnownedNode() {
        IotOpcUaServerNamespace namespace = protocol.getNamespace();
        AccessContext sessionA = mockAccessContext("scada-a");
        AccessContext internal = mock(AccessContext.class);
        when(internal.getSession()).thenReturn(Optional.empty());
        NodeId orphan = new NodeId(namespace.getNamespaceIndex(), "cfg-404/orphan");

        // 断言：本配置的节点可见、其它配置的节点不可见
        assertTrue(namespace.isVisible(sessionA, namespace.buildVariableNodeId(1L, "temperature")));
        assertFalse(namespace.isVisible(sessionA, namespace.buildVariableNodeId(2L, "counter")));
        // 断言：本命名空间内没有登记所属配置的节点，对会话不可见（fail-closed）
        assertFalse(namespace.isVisible(sessionA, orphan));
        // 断言：其它命名空间的节点不受影响；没有会话的内部访问不受影响
        assertTrue(namespace.isVisible(sessionA, NodeIds.ObjectsFolder));
        assertTrue(namespace.isVisible(sessionA, new NodeId(0, "external")));
        assertTrue(namespace.isVisible(internal, orphan));
    }

    @Test
    public void testMaxSessions() throws Exception {
        // 调用：配置的 maxSessions = 2，同一凭据连接两个会话
        connect(new UsernameProvider("scada-a", "secret-a"));
        OpcUaClient second = connect(new UsernameProvider("scada-a", "secret-a"));
        // 断言：第三个会话被拒绝；其它凭据不受影响
        assertThrows(Exception.class, () -> connect(new UsernameProvider("scada-a", "secret-a")));
        connect(new UsernameProvider("scada-b", "secret-b"));

        // 调用：关闭一个会话；断言：释放名额后可以重新连接
        second.disconnect();
        clients.remove(second);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> connect(new UsernameProvider("scada-a", "secret-a")));
    }

    @Test
    public void testTenantIsolation() throws Exception {
        OpcUaClient client = connect(new UsernameProvider("scada-a", "secret-a"));
        NodeId nodeB = protocol.getNamespace().buildVariableNodeId(2L, "counter");
        protocol.handleDeviceMessage(newPropertyPost(2L, 2L, Collections.singletonMap("counter", 7)));

        // 断言：读取、写入其它租户配置的节点都返回 Bad_NodeIdUnknown
        assertEquals(StatusCodes.Bad_NodeIdUnknown, readValue(client, nodeB).getStatusCode().getValue());
        StatusCode status = client.writeValues(Collections.singletonList(nodeB),
                Collections.singletonList(new DataValue(new Variant(1)))).get(0);
        assertEquals(StatusCodes.Bad_NodeIdUnknown, status.getValue());

        // 断言：浏览 Objects 只看到本配置的文件夹
        List<String> folders = browse(client, NodeIds.ObjectsFolder).stream()
                .map(r -> r.getBrowseName().getName()).collect(Collectors.toList());
        assertTrue(folders.contains("config-1"));
        assertFalse(folders.contains("config-2"));

        // 断言：租户 2 的会话能读到自己的节点
        OpcUaClient clientB = connect(new UsernameProvider("scada-b", "secret-b"));
        assertEquals(7, readValue(clientB, nodeB).getValue().getValue());
    }

    @Test
    public void testMonitoredItem() throws Exception {
        OpcUaClient client = connect(new UsernameProvider("scada-a", "secret-a"));
        NodeId nodeA = protocol.getNamespace().buildVariableNodeId(1L, "temperature");
        NodeId nodeB = protocol.getNamespace().buildVariableNodeId(2L, "counter");
        protocol.handleDeviceMessage(newPropertyPost(1L, 1L, Collections.singletonMap("temperature", 20.0D)));

        // 调用：订阅本租户和其它租户的节点
        OpcUaSubscription subscription = new OpcUaSubscription(client, 100.0);
        subscription.create();
        List<Object> received = new CopyOnWriteArrayList<>();
        OpcUaMonitoredItem itemA = newMonitoredItem(nodeA, received);
        OpcUaMonitoredItem itemB = newMonitoredItem(nodeB, received);
        subscription.addMonitoredItems(Arrays.asList(itemA, itemB));
        subscription.createMonitoredItems();

        // 断言：本租户的节点创建成功，其它租户的节点 Bad_NodeIdUnknown
        assertTrue(itemA.getCreateResult().orElseThrow(IllegalStateException::new).isGood());
        assertEquals(StatusCodes.Bad_NodeIdUnknown, itemB.getCreateResult().orElseThrow(IllegalStateException::new).getValue());
        // 断言：收到初始值和变化后的值
        await().atMost(5, TimeUnit.SECONDS).until(() -> received.contains(20.0D));
        protocol.handleDeviceMessage(newPropertyPost(1L, 1L, Collections.singletonMap("temperature", 21.5D)));
        await().atMost(5, TimeUnit.SECONDS).until(() -> received.contains(21.5D));
    }

    @Test
    public void testAuthentication_denied() throws Exception {
        // 断言：匿名、错误口令都不能激活会话
        assertThrows(Exception.class, () -> connect(AnonymousProvider.INSTANCE));
        assertThrows(Exception.class, () -> connect(new UsernameProvider("scada-a", "wrong")));
        // 断言：None 只用于发现端点，在 None 安全通道上不能激活会话
        EndpointDescription secure = getEndpoints().get(0);
        EndpointDescription none = new EndpointDescription(secure.getEndpointUrl() + "/discovery", secure.getServer(),
                secure.getServerCertificate(), MessageSecurityMode.None, SecurityPolicy.None.getUri(),
                secure.getUserIdentityTokens(), secure.getTransportProfileUri(), secure.getSecurityLevel());
        assertThrows(Exception.class, () -> connect(none, new UsernameProvider("scada-a", "secret-a")));
    }

    // ========== 工具方法 ==========

    private List<EndpointDescription> getEndpoints() throws Exception {
        return DiscoveryClient.getEndpoints("opc.tcp://127.0.0.1:" + port + PATH + "/discovery").get(5, TimeUnit.SECONDS);
    }

    private OpcUaClient connect(IdentityProvider identityProvider) throws Exception {
        EndpointDescription endpoint = getEndpoints().stream()
                .filter(e -> SecurityPolicy.Basic256Sha256.getUri().equals(e.getSecurityPolicyUri()))
                .findFirst().orElseThrow(IllegalStateException::new);
        return connect(endpoint, identityProvider);
    }

    private OpcUaClient connect(EndpointDescription endpoint, IdentityProvider identityProvider) throws Exception {
        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("test client"))
                .setApplicationUri("urn:yudao:test:client")
                .setKeyPair(clientKeyPair)
                .setCertificate(clientCertificate)
                .setCertificateChain(new X509Certificate[]{clientCertificate})
                .setCertificateValidator(new CertificateValidator.InsecureCertificateValidator())
                .setEndpoint(endpoint)
                .setIdentityProvider(identityProvider)
                .setRequestTimeout(uint(5000))
                .build();
        OpcUaClient client = OpcUaClient.create(config);
        try {
            client.connect();
        } catch (Exception e) {
            try {
                client.disconnect();
            } catch (Exception ignored) {
            }
            throw e;
        }
        clients.add(client);
        return client;
    }

    private static DataValue readValue(OpcUaClient client, NodeId nodeId) throws Exception {
        return readAttribute(client, nodeId, AttributeId.Value);
    }

    private static DataValue readAttribute(OpcUaClient client, NodeId nodeId, AttributeId attributeId) throws Exception {
        return client.read(0, TimestampsToReturn.Both, Collections.singletonList(new ReadValueId(nodeId,
                attributeId.uid(), null, QualifiedName.NULL_VALUE))).getResults()[0];
    }

    private static List<ReferenceDescription> browse(OpcUaClient client, NodeId nodeId) throws Exception {
        BrowseResult result = client.browse(new BrowseDescription(nodeId, BrowseDirection.Forward,
                NodeIds.HierarchicalReferences, true, uint(NodeClass.Object.getValue()),
                uint(BrowseResultMask.All.getValue())));
        return result.getReferences() != null ? Arrays.asList(result.getReferences())
                : Collections.<ReferenceDescription>emptyList();
    }

    private static OpcUaMonitoredItem newMonitoredItem(NodeId nodeId, List<Object> received) {
        OpcUaMonitoredItem item = OpcUaMonitoredItem.newDataItem(nodeId);
        item.setSamplingInterval(50.0);
        item.setQueueSize(uint(10));
        item.setDataValueListener((i, value) -> received.add(value.getValue().getValue()));
        return item;
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static AccessContext mockAccessContext(String alias) {
        DefaultUsernameIdentity identity = new DefaultUsernameIdentity(alias);
        identity.setUserData(alias);
        Session session = mock(Session.class);
        when(session.getIdentity()).thenReturn(identity);
        AccessContext context = mock(AccessContext.class);
        when(context.getSession()).thenReturn(Optional.of(session));
        return context;
    }

    private static IotDeviceMessage newPropertyPost(Long deviceId, Long tenantId, Map<String, Object> params) {
        IotDeviceMessage message = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);
        message.setDeviceId(deviceId);
        message.setTenantId(tenantId);
        return message;
    }

    private static IotDeviceMessage newTimedPropertyPost(double temperature, LocalDateTime reportTime) {
        IotDeviceMessage message = newPropertyPost(1L, 1L, Collections.singletonMap("temperature", temperature));
        message.setReportTime(reportTime);
        return message;
    }

    private static IotOpcUaServerConfig.Credential newCredential(String username, String password, Long tenantId) {
        IotOpcUaServerConfig.Credential credential = new IotOpcUaServerConfig.Credential();
        credential.setUsername(username);
        credential.setPassword(password);
        credential.setTenantIds(Collections.singletonList(tenantId));
        return credential;
    }

    private static IotOpcUaServerConfigRespDTO newConfig(Long id, Long tenantId, String alias,
                                                         IotOpcUaServerNodeRespDTO node) {
        IotOpcUaServerConfigRespDTO config = new IotOpcUaServerConfigRespDTO();
        config.setId(id);
        config.setTenantId(tenantId);
        config.setName("config-" + id);
        config.setCredentialAlias(alias);
        config.setMaxSessions(2);
        config.setMaxSubscriptionsPerSession(5);
        config.setMaxMonitoredItemsPerSession(1000);
        config.setNodes(Collections.singletonList(node));
        return config;
    }

    private static IotOpcUaServerNodeRespDTO newNode(Long id, String browseName, Long deviceId, String identifier,
                                                     String dataType) {
        IotOpcUaServerNodeRespDTO node = new IotOpcUaServerNodeRespDTO();
        node.setId(id);
        node.setBrowseName(browseName);
        node.setDeviceId(deviceId);
        node.setProductKey("pk-" + deviceId);
        node.setDeviceName("dn-" + deviceId);
        node.setIdentifier(identifier);
        node.setDataType(dataType);
        return node;
    }

}
