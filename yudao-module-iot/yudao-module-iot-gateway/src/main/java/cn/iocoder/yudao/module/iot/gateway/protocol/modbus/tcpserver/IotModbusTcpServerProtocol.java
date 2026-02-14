package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameDecoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream.IotModbusTcpServerDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream.IotModbusTcpServerDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.upstream.IotModbusTcpServerUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IoT 网关 Modbus TCP Server 协议
 * <p>
 * 作为 TCP Server 接收设备主动连接：
 * 1. 设备通过自定义功能码（FC 65）发送认证请求
 * 2. 认证成功后，网关主动发送 Modbus 读请求，设备响应（云端轮询模式）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerProtocol implements IotProtocol {

    /**
     * 协议配置
     */
    private final ProtocolProperties properties;
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
    private final Vertx vertx;
    /**
     * TCP Server
     */
    private NetServer netServer;
    /**
     * 配置刷新定时器 ID
     */
    private Long configRefreshTimerId;
    /**
     * Pending Request 清理定时器 ID
     */
    private Long requestCleanupTimerId;

    /**
     * 连接管理器
     */
    private final IotModbusTcpServerConnectionManager connectionManager;
    /**
     * 下行消息订阅者
     */
    private IotModbusTcpServerDownstreamSubscriber downstreamSubscriber;

    private final IotModbusFrameDecoder frameDecoder;
    @SuppressWarnings("FieldCanBeLocal")
    private final IotModbusFrameEncoder frameEncoder;

    private final IotModbusTcpServerConfigCacheService configCacheService;
    private final IotModbusTcpServerPendingRequestManager pendingRequestManager;
    private final IotModbusTcpServerUpstreamHandler upstreamHandler;
    private final IotModbusTcpServerPollScheduler pollScheduler;
    private final IotDeviceMessageService messageService;

    public IotModbusTcpServerProtocol(ProtocolProperties properties) {
        IotModbusTcpServerConfig slaveConfig = properties.getModbusTcpServer();
        Assert.notNull(slaveConfig, "Modbus TCP Server 协议配置（modbusTcpServer）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化 Vertx
        this.vertx = Vertx.vertx();

        // 初始化 Manager
        IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.connectionManager = new IotModbusTcpServerConnectionManager();
        this.configCacheService = new IotModbusTcpServerConfigCacheService(deviceApi);
        this.pendingRequestManager = new IotModbusTcpServerPendingRequestManager();

        // 初始化帧编解码器
        this.frameDecoder = new IotModbusFrameDecoder(slaveConfig.getCustomFunctionCode());
        this.frameEncoder = new IotModbusFrameEncoder(slaveConfig.getCustomFunctionCode());

        // 初始化共享事务 ID 自增器（PollScheduler 和 DownstreamHandler 共用，避免 transactionId 冲突）
        AtomicInteger transactionIdCounter = new AtomicInteger(0);
        // 初始化轮询调度器
        this.pollScheduler = new IotModbusTcpServerPollScheduler(
                vertx, connectionManager, frameEncoder, pendingRequestManager,
                slaveConfig.getRequestTimeout(), transactionIdCounter, configCacheService);

        // 初始化 Handler
        this.messageService = SpringUtil.getBean(IotDeviceMessageService.class);
        IotDeviceService deviceService = SpringUtil.getBean(IotDeviceService.class);
        this.upstreamHandler = new IotModbusTcpServerUpstreamHandler(
                deviceApi, this.messageService, frameEncoder,
                connectionManager, configCacheService, pendingRequestManager,
                pollScheduler, deviceService, serverId);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.MODBUS_TCP_SERVER;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT Modbus TCP Server 协议 {} 已经在运行中]", getId());
            return;
        }

        try {
            // 1. 启动配置刷新定时器
            IotModbusTcpServerConfig slaveConfig = properties.getModbusTcpServer();
            configRefreshTimerId = vertx.setPeriodic(
                    TimeUnit.SECONDS.toMillis(slaveConfig.getConfigRefreshInterval()),
                    id -> refreshConfig());

            // 2.1 启动 TCP Server
            startTcpServer();

            // 2.2 启动 PendingRequest 清理定时器
            requestCleanupTimerId = vertx.setPeriodic(
                    slaveConfig.getRequestCleanupInterval(),
                    id -> pendingRequestManager.cleanupExpired());
            running = true;
            log.info("[start][IoT Modbus TCP Server 协议 {} 启动成功, serverId={}, port={}]",
                    getId(), serverId, properties.getPort());

            // 3. 启动下行消息订阅
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            IotModbusTcpServerDownstreamHandler downstreamHandler = new IotModbusTcpServerDownstreamHandler(
                    connectionManager, configCacheService, frameEncoder, this.pollScheduler.getTransactionIdCounter());
            this.downstreamSubscriber = new IotModbusTcpServerDownstreamSubscriber(
                    this, downstreamHandler, messageBus);
            downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT Modbus TCP Server 协议 {} 启动失败]", getId(), e);
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
        // 1. 停止下行消息订阅
        if (downstreamSubscriber != null) {
            try {
                downstreamSubscriber.stop();
            } catch (Exception e) {
                log.error("[stop][下行消息订阅器停止失败]", e);
            }
            downstreamSubscriber = null;
        }

        // 2.1 取消定时器
        if (configRefreshTimerId != null) {
            vertx.cancelTimer(configRefreshTimerId);
            configRefreshTimerId = null;
        }
        if (requestCleanupTimerId != null) {
            vertx.cancelTimer(requestCleanupTimerId);
            requestCleanupTimerId = null;
        }
        // 2.2 停止轮询
        pollScheduler.stopAll();
        // 2.3 清理 PendingRequest
        pendingRequestManager.clear();
        // 2.4 关闭所有连接
        connectionManager.closeAll();
        // 2.5 关闭 TCP Server
        if (netServer != null) {
            try {
                netServer.close().result();
                log.info("[stop][TCP Server 已关闭]");
            } catch (Exception e) {
                log.error("[stop][TCP Server 关闭失败]", e);
            }
            netServer = null;
        }

        // 3. 关闭 Vertx
        if (vertx != null) {
            try {
                vertx.close().result();
            } catch (Exception e) {
                log.error("[stop][Vertx 关闭失败]", e);
            }
        }
        running = false;
        log.info("[stop][IoT Modbus TCP Server 协议 {} 已停止]", getId());
    }

    /**
     * 启动 TCP Server
     */
    private void startTcpServer() {
        // 1. 创建 TCP Server
        NetServerOptions options = new NetServerOptions()
                .setPort(properties.getPort());
        netServer = vertx.createNetServer(options);

        // 2. 设置连接处理器
        netServer.connectHandler(this::handleConnection);
        try {
            netServer.listen().toCompletionStage().toCompletableFuture().get();
            log.info("[startTcpServer][TCP Server 启动成功, port={}]", properties.getPort());
        } catch (Exception e) {
            throw new RuntimeException("[startTcpServer][TCP Server 启动失败]", e);
        }
    }

    /**
     * 处理新连接
     */
    private void handleConnection(NetSocket socket) {
        log.info("[handleConnection][新连接, remoteAddress={}]", socket.remoteAddress());

        // 1. 创建 RecordParser 并设置为数据处理器
        RecordParser recordParser =  frameDecoder.createRecordParser((frame, frameFormat) -> {
            // 【重要】帧处理分发，即消息处理
            upstreamHandler.handleFrame(socket, frame, frameFormat);
        });
        socket.handler(recordParser);

        // 2.1 连接关闭处理
        socket.closeHandler(v -> {
            ConnectionInfo info = connectionManager.removeConnection(socket);
            if (info == null || info.getDeviceId() == null) {
                log.info("[handleConnection][未认证连接关闭, remoteAddress={}]", socket.remoteAddress());
                return;
            }
            pollScheduler.stopPolling(info.getDeviceId());
            pendingRequestManager.removeDevice(info.getDeviceId());
            configCacheService.removeConfig(info.getDeviceId());
            // 发送设备下线消息
            try {
                IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
                messageService.sendDeviceMessage(offlineMessage, info.getProductKey(), info.getDeviceName(), serverId);
            } catch (Exception ex) {
                log.error("[handleConnection][发送设备下线消息失败, deviceId={}]", info.getDeviceId(), ex);
            }
            log.info("[handleConnection][连接关闭, deviceId={}, remoteAddress={}]",
                    info.getDeviceId(), socket.remoteAddress());
        });
        // 2.2 异常处理
        socket.exceptionHandler(e -> {
            log.error("[handleConnection][连接异常, remoteAddress={}]", socket.remoteAddress(), e);
            socket.close();
        });
    }

    /**
     * 刷新已连接设备的配置（定时调用）
     */
    private synchronized void refreshConfig() {
        try {
            // 1. 只刷新已连接设备的配置
            Set<Long> connectedDeviceIds = connectionManager.getConnectedDeviceIds();
            if (CollUtil.isEmpty(connectedDeviceIds)) {
                return;
            }
            List<IotModbusDeviceConfigRespDTO> configs =
                    configCacheService.refreshConnectedDeviceConfigList(connectedDeviceIds);
            if (configs == null) {
                log.warn("[refreshConfig][刷新配置失败，跳过本次刷新]");
                return;
            }
            log.debug("[refreshConfig][刷新了 {} 个已连接设备的配置]", configs.size());

            // 2. 更新已连接设备的轮询任务
            for (IotModbusDeviceConfigRespDTO config : configs) {
                try {
                    pollScheduler.updatePolling(config);
                } catch (Exception e) {
                    log.error("[refreshConfig][处理设备配置失败, deviceId={}]", config.getDeviceId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[refreshConfig][刷新配置失败]", e);
        }
    }

}
