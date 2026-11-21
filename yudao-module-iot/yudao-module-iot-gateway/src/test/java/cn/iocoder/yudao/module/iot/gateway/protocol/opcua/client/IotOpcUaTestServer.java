package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.EndpointConfig;
import org.eclipse.milo.opcua.sdk.server.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.OpcUaServerConfigLimits;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.items.DataItem;
import org.eclipse.milo.opcua.sdk.server.items.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.*;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.transport.server.tcp.OpcTcpServerTransport;
import org.eclipse.milo.opcua.stack.transport.server.tcp.OpcTcpServerTransportConfig;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.eclipse.milo.opcua.sdk.server.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ulong;

/**
 * 进程内的 OPC UA 测试服务端（基于 Milo Server SDK）：只用于自测，模拟 OPC UA 服务端的 Read、MonitoredItem、断线和重启
 * <p>
 * 端点（同一端口，不同路径）：
 * <ul>
 *     <li>{@code /yudao}：None/None、Basic256Sha256/Sign、Basic256Sha256/SignAndEncrypt、Aes128_Sha256_RsaOaep/SignAndEncrypt，
 *         token policy 为匿名 + 用户名（用户名 token 的 policy 为 Basic256Sha256）；</li>
 *     <li>{@code /yudao-plain-token}：Basic256Sha256/SignAndEncrypt，用户名 token 的 policy 实际为 None（用于校验口令不能明文发送）。</li>
 * </ul>
 * 节点：命名空间 {@link #NAMESPACE_URI} 下的 {@code s=Temperature}（Double）、{@code s=Counter}（Int32）、{@code s=Name}（String）、
 * {@code s=Flag}（Boolean）、{@code s=Array}（Int32[]）、{@code s=Big}（UInt64）、{@code s=Bad}（Bad 状态）、{@code i=1001}（Int32），
 * 都是只读、DataType 为 BaseDataType；
 * <p>
 * 可写节点（DataType 为具体类型）：{@code s=W.Int16}、{@code s=W.UInt32}、{@code s=W.Double}、{@code s=W.String}、{@code s=W.Flag}、
 * {@code s=W.Time}（UtcTime）、{@code s=W.Array}（Int32[]），{@code s=W.UserDenied}（AccessLevel 可写、UserAccessLevel 只读），
 * {@code s=W.Reject}（可写，但服务端拒绝写入：返回 Bad_OutOfRange）
 *
 * @author 芋道源码
 */
public class IotOpcUaTestServer {

    public static final String NAMESPACE_URI = "urn:yudao:iot:opc-ua:test";
    public static final String PATH = "/yudao";
    public static final String PATH_PLAIN_TOKEN = "/yudao-plain-token";
    public static final String USERNAME = "reader";
    public static final String PASSWORD = "reader-password";

    private final int port;
    private final List<String> extraNamespaceUris;
    private final KeyPair keyPair;
    private final X509Certificate certificate;
    private final MemoryTrustListManager trustListManager = new MemoryTrustListManager();
    /**
     * 节点值：重启后保留，模拟设备的现场值
     */
    private final Map<String, DataValue> values = new ConcurrentHashMap<>();
    /**
     * 可写节点：名字 → 定义（按插入顺序创建）
     */
    private final Map<String, WritableNode> writableNodes = new LinkedHashMap<>();
    /**
     * 服务端 OperationLimits.MaxMonitoredItemsPerCall：为 null 时使用 Milo 默认值；下次启动（重启）后生效
     */
    private volatile Integer maxMonitoredItemsPerCall;
    /**
     * 每次 CreateMonitoredItems 请求在测试命名空间创建的 MonitoredItem 数量：用于断言客户端的分批大小
     */
    private final List<Integer> createdDataItemBatchSizes = new CopyOnWriteArrayList<>();

    private OpcUaServer server;
    private TestNamespace namespace;

    /**
     * @param port               监听端口
     * @param extraNamespaceUris 在测试命名空间之前注册的命名空间：用于改变测试命名空间的索引（模拟服务端重启后命名空间顺序变化）
     */
    public IotOpcUaTestServer(int port, List<String> extraNamespaceUris) throws Exception {
        this.port = port;
        this.extraNamespaceUris = new ArrayList<>(extraNamespaceUris);
        this.keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        this.certificate = new SelfSignedCertificateBuilder(keyPair)
                .setCommonName("yudao opc ua test server")
                .setOrganization("yudao")
                .setApplicationUri("urn:yudao:iot:opc-ua:test-server")
                .addDnsName("localhost")
                .addIpAddress("127.0.0.1")
                .build();
        values.put("Temperature", new DataValue(new Variant(25.5D)));
        values.put("Counter", new DataValue(new Variant(1)));
        values.put("Name", new DataValue(new Variant("pump")));
        values.put("Flag", new DataValue(new Variant(true)));
        values.put("Array", new DataValue(new Variant(new Integer[]{1, 2, 3})));
        values.put("Big", new DataValue(new Variant(ulong("18446744073709551615"))));
        values.put("Bad", new DataValue(new StatusCode(StatusCodes.Bad_SensorFailure)));
        values.put("1001", new DataValue(new Variant(42)));
        writableNodes.put("W.Int16", new WritableNode(NodeIds.Int16, (short) 0));
        writableNodes.put("W.UInt32", new WritableNode(NodeIds.UInt32, uint(0)));
        writableNodes.put("W.Double", new WritableNode(NodeIds.Double, 0D));
        writableNodes.put("W.String", new WritableNode(NodeIds.String, ""));
        writableNodes.put("W.Flag", new WritableNode(NodeIds.Boolean, false));
        writableNodes.put("W.Time", new WritableNode(NodeIds.UtcTime, DateTime.MIN_DATE_TIME));
        writableNodes.put("W.Array", new WritableNode(NodeIds.Int32, new Integer[]{0}));
        writableNodes.put("W.UserDenied", new WritableNode(NodeIds.Int32, 0));
        writableNodes.put("W.Reject", new WritableNode(NodeIds.Int32, 0));
    }

    public synchronized void start() throws Exception {
        if (server != null) {
            return;
        }
        // 1. 证书：固定的服务端证书，信任列表在内存中（重启后保留）
        MemoryCertificateStore certificateStore = new MemoryCertificateStore();
        CertificateQuarantine quarantine = new MemoryCertificateQuarantine();
        RsaSha256CertificateFactory certificateFactory = new RsaSha256CertificateFactory() {
            @Override
            protected KeyPair createRsaSha256KeyPair() {
                return keyPair;
            }

            @Override
            protected X509Certificate[] createRsaSha256CertificateChain(KeyPair keyPair) {
                return new X509Certificate[]{certificate};
            }
        };
        DefaultApplicationGroup group = new DefaultApplicationGroup(trustListManager, certificateStore,
                certificateFactory, new DefaultServerCertificateValidator(trustListManager, quarantine));
        group.initialize();
        DefaultCertificateManager certificateManager = new DefaultCertificateManager(quarantine, group);

        // 2. 端点
        Set<EndpointConfig> endpoints = new LinkedHashSet<>();
        EndpointConfig.Builder base = EndpointConfig.newBuilder()
                .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                .setBindAddress("127.0.0.1").setBindPort(port).setHostname("127.0.0.1")
                .setCertificate(certificate);
        EndpointConfig.Builder main = base.copy().setPath(PATH)
                .addTokenPolicies(USER_TOKEN_POLICY_ANONYMOUS, USER_TOKEN_POLICY_USERNAME);
        endpoints.add(main.copy().setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None).build());
        endpoints.add(main.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256).setSecurityMode(MessageSecurityMode.Sign).build());
        endpoints.add(main.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256).setSecurityMode(MessageSecurityMode.SignAndEncrypt).build());
        endpoints.add(main.copy().setSecurityPolicy(SecurityPolicy.Aes128_Sha256_RsaOaep).setSecurityMode(MessageSecurityMode.SignAndEncrypt).build());
        UserTokenPolicy plainUsername = new UserTokenPolicy("username-plain", UserTokenType.UserName,
                null, null, SecurityPolicy.None.getUri());
        endpoints.add(base.copy().setPath(PATH_PLAIN_TOKEN).addTokenPolicies(plainUsername)
                .setSecurityPolicy(SecurityPolicy.Basic256Sha256).setSecurityMode(MessageSecurityMode.SignAndEncrypt).build());
        // 发现端点：客户端通过它获取端点列表
        endpoints.add(base.copy().setPath(PATH + "/discovery").addTokenPolicies(USER_TOKEN_POLICY_ANONYMOUS)
                .setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None).build());

        // 3. 服务端
        Integer monitoredItemsLimit = maxMonitoredItemsPerCall;
        OpcUaServerConfigLimits limits = new OpcUaServerConfigLimits() {
            @Override
            public UInteger getMaxMonitoredItemsPerCall() {
                return monitoredItemsLimit != null ? uint(monitoredItemsLimit)
                        : OpcUaServerConfigLimits.super.getMaxMonitoredItemsPerCall();
            }
        };
        OpcUaServerConfig config = OpcUaServerConfig.builder()
                .setLimits(limits)
                .setApplicationUri("urn:yudao:iot:opc-ua:test-server")
                .setApplicationName(LocalizedText.english("yudao opc ua test server"))
                .setProductUri("urn:yudao:iot:opc-ua:test-server")
                .setBuildInfo(new BuildInfo("urn:yudao:iot:opc-ua:test-server", "yudao", "test server",
                        OpcUaServer.SDK_VERSION, "", DateTime.now()))
                .setEndpoints(endpoints)
                .setCertificateManager(certificateManager)
                .setIdentityValidator(new CompositeValidator(AnonymousIdentityValidator.INSTANCE,
                        new UsernameIdentityValidator(challenge -> USERNAME.equals(challenge.getUsername())
                                && PASSWORD.equals(challenge.getPassword()))))
                .build();
        OpcUaServer newServer = new OpcUaServer(config, profile ->
                new OpcTcpServerTransport(OpcTcpServerTransportConfig.newBuilder().build()));
        // 先注册额外的命名空间，改变测试命名空间的索引
        extraNamespaceUris.forEach(uri -> newServer.getNamespaceTable().add(uri));
        TestNamespace newNamespace = new TestNamespace(newServer);
        newNamespace.startup();
        newServer.startup().get(10, TimeUnit.SECONDS);
        this.server = newServer;
        this.namespace = newNamespace;
    }

    public synchronized void stop() throws Exception {
        if (server == null) {
            return;
        }
        namespace.shutdown();
        server.shutdown().get(10, TimeUnit.SECONDS);
        server = null;
        namespace = null;
    }

    /**
     * 重启服务端：会话、订阅都失效（客户端只能新建会话，订阅转移失败后重建）
     *
     * @param newExtraNamespaceUris 重启后在测试命名空间之前注册的命名空间
     */
    public synchronized void restart(List<String> newExtraNamespaceUris) throws Exception {
        stop();
        extraNamespaceUris.clear();
        extraNamespaceUris.addAll(newExtraNamespaceUris);
        start();
    }

    /**
     * 设置服务端 OperationLimits.MaxMonitoredItemsPerCall（下次启动、重启后生效）
     *
     * @param maxMonitoredItemsPerCall 上限；为 null 时使用 Milo 默认值
     */
    public void setMaxMonitoredItemsPerCall(Integer maxMonitoredItemsPerCall) {
        this.maxMonitoredItemsPerCall = maxMonitoredItemsPerCall;
    }

    public List<Integer> getCreatedDataItemBatchSizes() {
        return createdDataItemBatchSizes;
    }

    public String getEndpointUrl() {
        return "opc.tcp://127.0.0.1:" + port + PATH;
    }

    public String getPlainTokenEndpointUrl() {
        return "opc.tcp://127.0.0.1:" + port + PATH_PLAIN_TOKEN;
    }

    public int getNamespaceIndex() {
        return namespace.getNamespaceIndex().intValue();
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void trust(X509Certificate clientCertificate) {
        trustListManager.addTrustedCertificate(clientCertificate);
    }

    /**
     * 修改节点值：订阅按采样间隔读取到变化后通知客户端
     *
     * @param name  节点名（字符串 NodeId 的标识；数字节点用 "1001"）
     * @param value 新值
     */
    public void setValue(String name, Object value) {
        DataValue dataValue = new DataValue(new Variant(value));
        values.put(name, dataValue);
        TestNamespace current = namespace;
        if (current != null) {
            current.setValue(name, dataValue);
        }
    }

    /**
     * 读取节点的当前值（包括客户端写入的值）
     *
     * @param name 节点名
     * @return 值；节点不存在时返回 null
     */
    public Object getValue(String name) {
        TestNamespace current = namespace;
        UaVariableNode node = current != null ? current.nodes.get(name) : null;
        return node != null ? node.getValue().getValue().getValue() : null;
    }

    /**
     * 可写节点的定义
     */
    private static class WritableNode {

        private final NodeId dataType;
        private final Object initialValue;

        WritableNode(NodeId dataType, Object initialValue) {
            this.dataType = dataType;
            this.initialValue = initialValue;
        }

    }

    /**
     * 测试命名空间：每个值一个变量节点，挂在 Objects/YudaoTest 下
     */
    private class TestNamespace extends ManagedNamespaceWithLifecycle {

        private final SubscriptionModel subscriptionModel;
        private final Map<String, UaVariableNode> nodes = new ConcurrentHashMap<>();

        TestNamespace(OpcUaServer server) {
            super(server, NAMESPACE_URI);
            subscriptionModel = new SubscriptionModel(server, this);
            getLifecycleManager().addLifecycle(subscriptionModel);
            getLifecycleManager().addStartupTask(this::createNodes);
        }

        private void createNodes() {
            UaFolderNode folder = new UaFolderNode(getNodeContext(), newNodeId("YudaoTest"),
                    newQualifiedName("YudaoTest"), LocalizedText.english("YudaoTest"));
            getNodeManager().addNode(folder);
            folder.addReference(new Reference(folder.getNodeId(), NodeIds.Organizes,
                    NodeIds.ObjectsFolder.expanded(), false));
            for (Map.Entry<String, DataValue> entry : values.entrySet()) {
                String name = entry.getKey();
                NodeId nodeId = "1001".equals(name) ? newNodeId(1001) : newNodeId(name);
                UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                        .setNodeId(nodeId)
                        .setAccessLevel(AccessLevel.READ_ONLY)
                        .setUserAccessLevel(AccessLevel.READ_ONLY)
                        .setBrowseName(newQualifiedName(name))
                        .setDisplayName(LocalizedText.english(name))
                        .setDataType(NodeIds.BaseDataType)
                        .setTypeDefinition(NodeIds.BaseDataVariableType)
                        .build();
                node.setValue(entry.getValue());
                getNodeManager().addNode(node);
                folder.addOrganizes(node);
                nodes.put(name, node);
            }
            for (Map.Entry<String, WritableNode> entry : writableNodes.entrySet()) {
                String name = entry.getKey();
                WritableNode definition = entry.getValue();
                boolean array = definition.initialValue.getClass().isArray();
                UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                        .setNodeId(newNodeId(name))
                        .setAccessLevel(AccessLevel.READ_WRITE)
                        .setUserAccessLevel("W.UserDenied".equals(name) ? AccessLevel.READ_ONLY : AccessLevel.READ_WRITE)
                        .setBrowseName(newQualifiedName(name))
                        .setDisplayName(LocalizedText.english(name))
                        .setDataType(definition.dataType)
                        .setValueRank(array ? ValueRanks.OneDimension : ValueRanks.Scalar)
                        .setTypeDefinition(NodeIds.BaseDataVariableType)
                        .build();
                node.setValue(new DataValue(new Variant(definition.initialValue)));
                if ("W.Reject".equals(name)) {
                    // 模拟设备拒绝：值超出现场允许的范围
                    node.getFilterChain().addLast(new AttributeFilter() {
                        @Override
                        public void writeAttribute(AttributeFilterContext ctx, AttributeId attributeId, Object value)
                                throws UaException {
                            if (attributeId == AttributeId.Value) {
                                throw new UaException(StatusCodes.Bad_OutOfRange);
                            }
                            ctx.writeAttribute(attributeId, value);
                        }
                    });
                }
                getNodeManager().addNode(node);
                folder.addOrganizes(node);
                nodes.put(name, node);
            }
        }

        void setValue(String name, DataValue value) {
            UaVariableNode node = nodes.get(name);
            if (node != null) {
                node.setValue(value);
            }
        }

        @Override
        public void onDataItemsCreated(List<DataItem> dataItems) {
            createdDataItemBatchSizes.add(dataItems.size());
            subscriptionModel.onDataItemsCreated(dataItems);
        }

        @Override
        public void onDataItemsModified(List<DataItem> dataItems) {
            subscriptionModel.onDataItemsModified(dataItems);
        }

        @Override
        public void onDataItemsDeleted(List<DataItem> dataItems) {
            subscriptionModel.onDataItemsDeleted(dataItems);
        }

        @Override
        public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
            subscriptionModel.onMonitoringModeChanged(monitoredItems);
        }

    }

}
