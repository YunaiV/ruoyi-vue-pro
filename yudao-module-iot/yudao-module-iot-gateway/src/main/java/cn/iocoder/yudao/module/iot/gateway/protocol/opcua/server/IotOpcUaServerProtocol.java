package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.security.IotOpcUaServerCertificateManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.security.IotOpcUaServerIdentityValidator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.server.EndpointConfig;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.OpcUaServerConfigLimits;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.transport.server.tcp.OpcTcpServerTransport;
import org.eclipse.milo.opcua.stack.transport.server.tcp.OpcTcpServerTransportConfig;

import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

/**
 * IoT 网关 OPC UA Server 协议：把设备属性以只读节点暴露给上位机（北向数据服务）
 * <ol>
 *     <li>默认关闭：application.yaml 中 enabled: false，开启后还必须配置 keystore-password 和凭据；</li>
 *     <li>端点：Basic256Sha256、Aes128_Sha256_RsaOaep、Aes256_Sha256_RsaPss + SignAndEncrypt；None 只用于 /discovery 发现端点，
 *         开启 security-none-allowed 后才提供 None 会话端点；只允许用户名登录，口令始终按 Basic256Sha256 加密；</li>
 *     <li>节点只读、写入一律拒绝，支持 Read、Browse 和 MonitoredItem 订阅；</li>
 *     <li>租户隔离：会话只能看到凭据别名对应配置下的节点，配置的租户必须在凭据的 tenant-ids 中；网关本身不绑定租户；</li>
 *     <li>配置按 config-refresh-interval 从 biz 拉取；节点值来自消息总线上的设备属性上报（thing.property.post）。</li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaServerProtocol implements IotProtocol {

    private static final long STOP_TIMEOUT_MILLIS = 10000;
    /**
     * 用户名令牌策略：口令始终按 Basic256Sha256 加密（包括 None 安全通道）
     */
    static final UserTokenPolicy USER_TOKEN_POLICY_USERNAME = new UserTokenPolicy("username_basic256sha256",
            UserTokenType.UserName, null, null, SecurityPolicy.Basic256Sha256.getUri());
    /**
     * 加密端点使用的安全策略：都搭配 SignAndEncrypt
     */
    private static final List<SecurityPolicy> SECURE_POLICIES = Collections.unmodifiableList(Arrays.asList(
            SecurityPolicy.Basic256Sha256, SecurityPolicy.Aes128_Sha256_RsaOaep, SecurityPolicy.Aes256_Sha256_RsaPss));

    private final ProtocolProperties properties;
    private final IotOpcUaServerConfig serverConfig;
    @Getter
    private final String serverId;
    private final IotDeviceCommonApi deviceApi;
    private final IotMessageBus messageBus;

    @Getter
    private volatile boolean running = false;

    private final IotOpcUaServerCertificateManager certificateManager;
    private final IotOpcUaServerIdentityValidator identityValidator;
    private final DynamicLimits limits;
    @Getter
    private OpcUaServer server;
    @Getter
    private IotOpcUaServerNamespace namespace;
    private ScheduledExecutorService scheduler;
    private PropertyPostSubscriber subscriber;

    public IotOpcUaServerProtocol(ProtocolProperties properties) {
        this(properties, SpringUtil.getBean(IotDeviceCommonApi.class), SpringUtil.getBean(IotMessageBus.class));
    }

    public IotOpcUaServerProtocol(ProtocolProperties properties, IotDeviceCommonApi deviceApi, IotMessageBus messageBus) {
        IotOpcUaServerConfig serverConfig = properties.getOpcUaServer();
        Assert.notNull(serverConfig, "OPC UA Server 协议配置（opcUaServer）不能为空");
        this.properties = properties;
        this.serverConfig = serverConfig;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());
        this.deviceApi = deviceApi;
        this.messageBus = messageBus;
        this.certificateManager = new IotOpcUaServerCertificateManager(serverConfig);
        this.identityValidator = new IotOpcUaServerIdentityValidator(serverConfig,
                alias -> namespace != null ? namespace.getConfigByAlias(alias) : null, this::getLiveSessions);
        this.limits = new DynamicLimits(serverConfig);
    }

    /**
     * 获取当前存活（已创建或已激活）的会话
     */
    private Collection<Session> getLiveSessions() {
        OpcUaServer current = server;
        return current != null ? current.getSessionManager().getAllSessions() : Collections.emptyList();
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.OPC_UA_SERVER;
    }

    @Override
    public synchronized void start() {
        if (running) {
            log.warn("[start][IoT OPC UA Server 协议 {} 已经在运行中]", getId());
            return;
        }
        try {
            // 1. 证书：失败时协议启动失败
            certificateManager.initialize();
            // 2. 创建并启动 Milo Server
            OpcUaServer newServer = new OpcUaServer(buildServerConfig(), profile ->
                    new OpcTcpServerTransport(OpcTcpServerTransportConfig.newBuilder().build()));
            this.server = newServer;
            // 2.1 会话名额：按存活会话的身份计数，会话关闭时释放激活中的预留
            identityValidator.clearSessions();
            newServer.getSessionManager().addSessionListener(identityValidator);
            this.namespace = new IotOpcUaServerNamespace(server);
            namespace.startup();
            server.startup().get(STOP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            running = true;
            // 3. 首次加载配置，并定时刷新
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "iot-opc-ua-server-" + getId());
                thread.setDaemon(true);
                return thread;
            });
            scheduler.scheduleWithFixedDelay(this::refreshConfig, 0, serverConfig.getConfigRefreshInterval(),
                    TimeUnit.SECONDS);
            // 4. 订阅设备属性上报，更新节点值
            if (messageBus != null) {
                this.subscriber = new PropertyPostSubscriber();
                messageBus.register(subscriber);
            }
            log.info("[start][IoT OPC UA Server 协议 {} 启动成功，端点: opc.tcp://{}:{}{}，serverId={}]", getId(),
                    serverConfig.getHostname(), properties.getPort(), normalizeBasePath(serverConfig.getPath()), serverId);
        } catch (Exception e) {
            log.error("[start][IoT OPC UA Server 协议 {} 启动失败]", getId(), e);
            stop0();
            throw e instanceof RuntimeException ? (RuntimeException) e : new IllegalStateException(e);
        }
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        stop0();
    }

    private void stop0() {
        running = false;
        if (subscriber != null) {
            try {
                messageBus.unregister(subscriber);
            } catch (Exception e) {
                log.warn("[stop][IoT OPC UA Server 协议 {} 取消订阅失败]", getId(), e);
            }
            subscriber = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (namespace != null) {
            try {
                namespace.shutdown();
            } catch (Exception e) {
                log.warn("[stop][IoT OPC UA Server 协议 {} 关闭命名空间失败]", getId(), e);
            }
        }
        if (server != null) {
            try {
                server.shutdown().get(STOP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("[stop][IoT OPC UA Server 协议 {} 关闭 Server 未完成]", getId(), e);
            }
        }
        certificateManager.close();
        identityValidator.clearSessions();
        log.info("[stop][IoT OPC UA Server 协议 {} 已停止]", getId());
    }

    private OpcUaServerConfig buildServerConfig() {
        return OpcUaServerConfig.builder()
                .setApplicationUri(certificateManager.getApplicationUri())
                .setApplicationName(LocalizedText.english(serverConfig.getApplicationName()))
                .setProductUri(IotOpcUaServerCertificateManager.PRODUCT_URI)
                .setBuildInfo(new BuildInfo(IotOpcUaServerCertificateManager.PRODUCT_URI, "yudao",
                        serverConfig.getApplicationName(), OpcUaServer.SDK_VERSION, "", DateTime.now()))
                .setEndpoints(buildEndpoints(serverConfig, properties.getPort(), certificateManager.getCertificate()))
                .setCertificateManager(certificateManager.getCertificateManager())
                .setIdentityValidator(identityValidator)
                .setLimits(limits)
                .build();
    }

    /**
     * 构建端点：加密端点 + /discovery 发现端点（None）；开启 security-none-allowed 时再加 None 会话端点
     *
     * @param config      协议配置
     * @param port        监听端口
     * @param certificate 服务端证书
     * @return 端点
     */
    static Set<EndpointConfig> buildEndpoints(IotOpcUaServerConfig config, int port, X509Certificate certificate) {
        // 会话端点、发现端点使用同一个规范化路径：客户端在会话端点 URL 后拼接 /discovery 回退时不会出现双斜杠
        String basePath = normalizeBasePath(config.getPath());
        EndpointConfig.Builder base = EndpointConfig.newBuilder()
                .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                .setBindAddress(config.getBindAddress())
                .setHostname(config.getHostname())
                .setBindPort(port)
                .setPath(basePath)
                .setCertificate(certificate)
                .addTokenPolicies(USER_TOKEN_POLICY_USERNAME);
        Set<EndpointConfig> endpoints = new LinkedHashSet<>();
        for (SecurityPolicy policy : SECURE_POLICIES) {
            endpoints.add(base.copy().setSecurityPolicy(policy).setSecurityMode(MessageSecurityMode.SignAndEncrypt).build());
        }
        if (Boolean.TRUE.equals(config.getSecurityNoneAllowed())) {
            endpoints.add(base.copy().setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None).build());
        }
        // 发现端点：客户端通过 None 调用 GetEndpoints；会话激活由身份校验拒绝 None 安全通道
        endpoints.add(base.copy().setPath(buildDiscoveryPath(basePath))
                .setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None).build());
        return endpoints;
    }

    /**
     * 规范化端点的基础路径：合并连续的 /、去掉末尾的 /、补齐开头的 /；根路径规范化为空字符串
     *
     * @param path 配置的端点路径，例如 /yudao-iot、/yudao-iot/、//a//b//、yudao-iot、/
     * @return 规范化的路径，例如 /yudao-iot、/a/b、空字符串（根路径）
     */
    static String normalizeBasePath(String path) {
        String base = StrUtil.trimToEmpty(path).replaceAll("/{2,}", "/");
        base = StrUtil.removeSuffix(base, "/");
        if (!base.isEmpty() && !base.startsWith("/")) {
            base = "/" + base;
        }
        return base;
    }

    /**
     * 构建发现端点的路径：规范化的基础路径拼接 /discovery，避免出现双斜杠
     *
     * @param path 端点路径，例如 /yudao-iot、/yudao-iot/、/
     * @return 发现端点的路径，例如 /yudao-iot/discovery、/discovery
     */
    static String buildDiscoveryPath(String path) {
        return normalizeBasePath(path) + "/discovery";
    }

    /**
     * 刷新配置：API 失败时保留当前配置
     */
    void refreshConfig() {
        if (!running) {
            return;
        }
        try {
            CommonResult<List<IotOpcUaServerConfigRespDTO>> result = deviceApi.getOpcUaServerConfigList();
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.warn("[refreshConfig][获取 OPC UA Server 配置失败，保留当前配置: {}]", result != null ? result.getMsg() : null);
                return;
            }
            List<IotOpcUaServerConfigRespDTO> configs = filterConfigs(result.getData());
            namespace.applyConfigs(configs);
            limits.update(configs);
            log.debug("[refreshConfig][生效 OPC UA Server 配置 {} 个]", configs.size());
        } catch (Exception e) {
            log.error("[refreshConfig][刷新 OPC UA Server 配置失败]", e);
        }
    }

    /**
     * 过滤配置：配置的租户必须在凭据的 tenant-ids 中
     */
    List<IotOpcUaServerConfigRespDTO> filterConfigs(List<IotOpcUaServerConfigRespDTO> configs) {
        List<IotOpcUaServerConfigRespDTO> result = new ArrayList<>(configs.size());
        for (IotOpcUaServerConfigRespDTO config : configs) {
            if (identityValidator.isTenantAllowed(config.getTenantId(), config.getCredentialAlias())) {
                result.add(config);
            }
        }
        return result;
    }

    /**
     * 处理设备消息：只处理属性上报
     */
    @SuppressWarnings("unchecked")
    void handleDeviceMessage(IotDeviceMessage message) {
        if (namespace == null || message == null || message.getDeviceId() == null
                || !IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod().equals(message.getMethod())) {
            return;
        }
        Object params = message.getParams();
        if (params instanceof String) {
            params = JsonUtils.parseObjectQuietly((String) params, Map.class);
        }
        if (params instanceof Map) {
            // SourceTimestamp：消息的上报时间（网关按本机时区生成）
            DateTime sourceTime = message.getReportTime() != null
                    ? new DateTime(message.getReportTime().atZone(ZoneId.systemDefault()).toInstant()) : null;
            namespace.updateProperties(message.getDeviceId(), message.getTenantId(), (Map<String, Object>) params,
                    sourceTime);
        }
    }

    /**
     * 设备属性上报订阅者：每个 OPC UA Server 协议实例一个分组，接收全部设备消息
     */
    private class PropertyPostSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

        @Override
        public String getTopic() {
            return IotDeviceMessage.MESSAGE_BUS_DEVICE_MESSAGE_TOPIC;
        }

        @Override
        public String getGroup() {
            return "iot_opc_ua_server_" + getId();
        }

        @Override
        public void onMessage(IotDeviceMessage message) {
            try {
                handleDeviceMessage(message);
            } catch (Exception e) {
                log.warn("[onMessage][OPC UA Server 处理设备消息 {} 失败]", message.getId(), e);
            }
        }

    }

    /**
     * 动态限额：Milo 的会话级限额是全局的，取网关上限与所有启用配置中的较小值；
     * 同一凭据别名的会话数由身份校验按配置的 maxSessions 限制
     */
    static class DynamicLimits implements OpcUaServerConfigLimits {

        private final IotOpcUaServerConfig config;
        private volatile int maxSubscriptionsPerSession;
        private volatile int maxMonitoredItemsPerSession;

        DynamicLimits(IotOpcUaServerConfig config) {
            this.config = config;
            this.maxSubscriptionsPerSession = config.getMaxSubscriptionsPerSession();
            this.maxMonitoredItemsPerSession = config.getMaxMonitoredItemsPerSession();
        }

        void update(List<IotOpcUaServerConfigRespDTO> configs) {
            int subscriptions = config.getMaxSubscriptionsPerSession();
            int monitoredItems = config.getMaxMonitoredItemsPerSession();
            for (IotOpcUaServerConfigRespDTO serverConfig : configs) {
                if (serverConfig.getMaxSubscriptionsPerSession() != null && serverConfig.getMaxSubscriptionsPerSession() > 0) {
                    subscriptions = Math.min(subscriptions, serverConfig.getMaxSubscriptionsPerSession());
                }
                if (serverConfig.getMaxMonitoredItemsPerSession() != null && serverConfig.getMaxMonitoredItemsPerSession() > 0) {
                    monitoredItems = Math.min(monitoredItems, serverConfig.getMaxMonitoredItemsPerSession());
                }
            }
            this.maxSubscriptionsPerSession = subscriptions;
            this.maxMonitoredItemsPerSession = monitoredItems;
        }

        @Override
        public UInteger getMaxSessions() {
            return uint(config.getMaxSessions());
        }

        @Override
        public UInteger getMaxSubscriptionsPerSession() {
            return uint(maxSubscriptionsPerSession);
        }

        @Override
        public UInteger getMaxMonitoredItemsPerSession() {
            return uint(maxMonitoredItemsPerSession);
        }

    }

}
