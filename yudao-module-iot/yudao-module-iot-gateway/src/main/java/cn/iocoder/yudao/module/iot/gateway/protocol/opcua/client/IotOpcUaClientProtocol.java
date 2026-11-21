package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.downstream.IotOpcUaClientDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.downstream.IotOpcUaClientDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.upstream.IotOpcUaClientUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSessionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSubscriptionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaDeviceStateTracker;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.security.IotOpcUaClientSecurityFactory;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 OPC UA Client 协议：连接 OPC UA 服务端，Read 轮询或 MonitoredItem 订阅采集设备属性；
 * 下行只支持属性设置（写节点的 Value 属性）
 * <p>
 * 所有状态（会话、设备绑定、轮询、订阅、上下线、写入排队）只在一个 Vert.x context 上修改；
 * 阻塞操作（拉取配置、创建客户端、订阅操作、属性写入、发送消息）提交到该 context 的有序 worker 队列
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientProtocol implements IotProtocol {

    /**
     * 停止时等待断开连接的最长时间（毫秒）
     */
    private static final long STOP_TIMEOUT_MILLIS = 10000;

    /**
     * 协议配置
     */
    private final ProtocolProperties properties;
    private final IotOpcUaClientConfig opcUaClientConfig;
    /**
     * 服务器 ID（用于消息追踪，全局唯一）
     */
    @Getter
    private final String serverId;

    /**
     * 运行状态
     */
    @Getter
    private volatile boolean running = false;

    /**
     * Vert.x 实例
     */
    @Getter
    private final Vertx vertx;
    /**
     * 所有状态所在的 context
     */
    private final Context context;
    /**
     * 拉取配置的 worker 队列
     */
    private final IotWorkerTaskQueue configQueue;
    /**
     * 配置刷新定时器 ID
     */
    private Long configRefreshTimerId;
    /**
     * 是否正在刷新配置
     */
    private boolean refreshing;

    private final IotMessageBus messageBus;
    /**
     * 下行消息订阅者
     */
    private IotOpcUaClientDownstreamSubscriber downstreamSubscriber;
    /**
     * 下行属性写入处理器
     */
    @Getter
    private final IotOpcUaClientDownstreamHandler downstreamHandler;

    private final IotOpcUaClientConfigCacheService configCacheService;
    private final IotOpcUaClientSecurityFactory securityFactory;
    private final IotOpcUaDeviceStateTracker stateTracker;
    private final IotOpcUaClientPollScheduler pollScheduler;
    @Getter
    private final IotOpcUaClientSessionManager sessionManager;

    public IotOpcUaClientProtocol(ProtocolProperties properties) {
        this(properties, SpringUtil.getBean(IotDeviceCommonApi.class), SpringUtil.getBean(IotDeviceMessageService.class),
                SpringUtil.getBean(IotMessageBus.class));
    }

    public IotOpcUaClientProtocol(ProtocolProperties properties, IotDeviceCommonApi deviceApi,
                                  IotDeviceMessageService messageService, IotMessageBus messageBus) {
        IotOpcUaClientConfig opcUaClientConfig = properties.getOpcUaClient();
        Assert.notNull(opcUaClientConfig, "OPC UA Client 协议配置（opcUaClient）不能为空");
        this.properties = properties;
        this.opcUaClientConfig = opcUaClientConfig;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());
        this.messageBus = messageBus;

        // 初始化 Vertx：所有状态在同一个 context 上修改
        this.vertx = Vertx.vertx();
        this.context = vertx.getOrCreateContext();
        this.configQueue = new IotWorkerTaskQueue(context);

        // 初始化 Manager
        this.configCacheService = new IotOpcUaClientConfigCacheService(deviceApi);
        this.securityFactory = new IotOpcUaClientSecurityFactory(opcUaClientConfig);
        IotOpcUaClientUpstreamHandler upstreamHandler = new IotOpcUaClientUpstreamHandler(messageService, serverId,
                opcUaClientConfig.getMaxPointsPerMessage(), opcUaClientConfig.getMaxMessageBytes());
        this.stateTracker = new IotOpcUaDeviceStateTracker(vertx, context, messageService, serverId,
                TimeUnit.SECONDS.toMillis(opcUaClientConfig.getStateRetryInterval()));
        this.pollScheduler = new IotOpcUaClientPollScheduler(vertx, context, upstreamHandler);
        IotOpcUaClientSubscriptionManager subscriptionManager = new IotOpcUaClientSubscriptionManager(vertx, context,
                opcUaClientConfig, upstreamHandler);
        this.sessionManager = new IotOpcUaClientSessionManager(vertx, context, opcUaClientConfig, securityFactory,
                stateTracker, pollScheduler, subscriptionManager);
        this.downstreamHandler = new IotOpcUaClientDownstreamHandler(vertx, context, opcUaClientConfig, sessionManager,
                messageService, serverId);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.OPC_UA_CLIENT;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT OPC UA Client 协议 {} 已经在运行中]", getId());
            return;
        }

        try {
            // 1. 初始化客户端证书、信任列表：失败时协议启动失败（不回退匿名）
            securityFactory.initialize();
            running = true;

            // 2. 首次加载配置，并启动配置刷新定时器（在 context 上执行）
            long refreshInterval = TimeUnit.SECONDS.toMillis(opcUaClientConfig.getConfigRefreshInterval());
            context.runOnContext(v -> {
                refreshConfig();
                configRefreshTimerId = vertx.setPeriodic(refreshInterval, id -> refreshConfig());
            });
            log.info("[start][IoT OPC UA Client 协议 {} 启动成功，serverId={}]", getId(), serverId);

            // 3. 启动下行消息订阅者（只处理属性设置）
            if (messageBus != null) {
                this.downstreamSubscriber = new IotOpcUaClientDownstreamSubscriber(this, downstreamHandler, messageBus);
                this.downstreamSubscriber.start();
            }
        } catch (Exception e) {
            log.error("[start][IoT OPC UA Client 协议 {} 启动失败]", getId(), e);
            stop0();
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        stop0();
    }

    private void stop0() {
        // 1. 停止下行消息订阅者
        if (downstreamSubscriber != null) {
            try {
                downstreamSubscriber.stop();
                log.info("[stop][IoT OPC UA Client 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT OPC UA Client 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2. 在 context 上取消定时器、关闭所有会话（关闭 Session、断开连接），等待完成
        running = false;
        CompletableFuture<CompletableFuture<Void>> closing = new CompletableFuture<>();
        try {
            context.runOnContext(v -> {
                if (configRefreshTimerId != null) {
                    vertx.cancelTimer(configRefreshTimerId);
                    configRefreshTimerId = null;
                }
                stateTracker.close();
                closing.complete(sessionManager.closeAll());
            });
            closing.get(STOP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS).get(STOP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("[stop][IoT OPC UA Client 协议 {} 关闭会话未完成]", getId(), e);
        }
        // 3. 关闭信任列表
        securityFactory.close();

        // 4. 关闭 Vert.x 实例
        try {
            vertx.close().toCompletionStage().toCompletableFuture().get(STOP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            log.info("[stop][IoT OPC UA Client 协议 {} Vertx 已关闭]", getId());
        } catch (Exception e) {
            log.error("[stop][IoT OPC UA Client 协议 {} Vertx 关闭失败]", getId(), e);
        }
        log.info("[stop][IoT OPC UA Client 协议 {} 已停止]", getId());
    }

    /**
     * 刷新配置：在 worker 上拉取，回到 context 上应用
     */
    private void refreshConfig() {
        if (!running || refreshing) {
            return;
        }
        refreshing = true;
        configQueue.submit(configCacheService::refreshConfig).onComplete(ar -> {
            refreshing = false;
            if (!running) {
                return;
            }
            // 1. API 失败时返回 null：跳过本轮
            List<IotOpcUaDeviceConfigRespDTO> configs = ar.succeeded() ? ar.result() : null;
            if (configs == null) {
                log.warn("[refreshConfig][API 失败，跳过本轮刷新]");
                return;
            }
            log.debug("[refreshConfig][获取到 {} 个 OPC UA 设备配置]", configs.size());
            // 2. 更新会话、轮询、订阅；清理已删除设备
            try {
                Set<Long> removedDeviceIds = configCacheService.cleanupRemovedDevices(configs);
                sessionManager.applyConfigs(configs, removedDeviceIds);
            } catch (Exception e) {
                log.error("[refreshConfig][刷新配置失败]", e);
            }
        });
    }

}
