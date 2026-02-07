package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.IotModbusDataConverter;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrame;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusRecordParserFactory;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.handler.downstream.IotModbusTcpSlaveDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.handler.downstream.IotModbusTcpSlaveDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.handler.upstream.IotModbusTcpSlaveUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePendingRequestManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePollScheduler;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// TODO @AI：不用主动上报！
/**
 * IoT 网关 Modbus TCP Slave 协议
 * <p>
 * 作为 TCP Server 接收设备主动连接：
 * 1. 设备通过自定义功能码（FC 65）发送认证请求
 * 2. 认证成功后，根据设备配置的 mode 决定工作模式：
 * - mode=1（云端轮询）：网关主动发送 Modbus 读请求，设备响应
 * - mode=2（主动上报）：设备主动上报数据，网关透传
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpSlaveProtocol implements IotProtocol {

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
     * 未认证连接的帧格式缓存：socket → 检测到的帧格式
     */
    private final Map<NetSocket, IotModbusFrameFormatEnum> pendingFrameFormats = new ConcurrentHashMap<>();

    // ========== 各组件 ==========

    private final IotModbusTcpSlaveConfig slaveConfig;
    private final IotModbusFrameCodec frameCodec;
    private final IotModbusTcpSlaveConnectionManager connectionManager;
    private final IotModbusTcpSlaveConfigCacheService configCacheService;
    private final IotModbusTcpSlavePendingRequestManager pendingRequestManager;
    private final IotModbusTcpSlaveUpstreamHandler upstreamHandler;
    private final IotModbusTcpSlaveDownstreamHandler downstreamHandler;
    private final IotModbusTcpSlaveDownstreamSubscriber downstreamSubscriber;
    private final IotModbusTcpSlavePollScheduler pollScheduler;

    public IotModbusTcpSlaveProtocol(ProtocolProperties properties) {
        this.slaveConfig = properties.getModbusTcpSlave();
        Assert.notNull(slaveConfig, "Modbus TCP Slave 协议配置（modbusTcpSlave）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化 Vertx
        this.vertx = Vertx.vertx();

        // 初始化 Manager
        IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.connectionManager = new IotModbusTcpSlaveConnectionManager();
        this.configCacheService = new IotModbusTcpSlaveConfigCacheService(deviceApi);
        this.pendingRequestManager = new IotModbusTcpSlavePendingRequestManager();

        // 初始化帧编解码器
        this.frameCodec = new IotModbusFrameCodec(slaveConfig.getCustomFunctionCode());

        // 初始化 Handler
        IotModbusDataConverter dataConverter = new IotModbusDataConverter();
        IotDeviceMessageService messageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.upstreamHandler = new IotModbusTcpSlaveUpstreamHandler(
                deviceApi, messageService, dataConverter, frameCodec,
                connectionManager, configCacheService, pendingRequestManager, serverId);
        this.downstreamHandler = new IotModbusTcpSlaveDownstreamHandler(
                connectionManager, configCacheService, dataConverter, frameCodec);

        // 初始化轮询调度器
        this.pollScheduler = new IotModbusTcpSlavePollScheduler(
                vertx, connectionManager, frameCodec, pendingRequestManager,
                slaveConfig.getRequestTimeout());

        // 设置认证成功回调：启动轮询
        // TODO @AI：感觉直接去调用，不用注册回调了（更简洁）
        this.upstreamHandler.setOnAuthSuccess((deviceId, config) -> {
            if (config.getMode() != null
                    && config.getMode().equals(IotModbusModeEnum.POLLING.getMode())) {
                pollScheduler.updatePolling(config);
            }
        });

        // 初始化下行消息订阅者
        IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
        this.downstreamSubscriber = new IotModbusTcpSlaveDownstreamSubscriber(
                this, downstreamHandler, messageBus);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.MODBUS_TCP_SLAVE;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT Modbus TCP Slave 协议 {} 已经在运行中]", getId());
            return;
        }

        try {
            // 1.1 首次加载配置
            refreshConfig();
            // 1.2 启动配置刷新定时器
            int refreshInterval = slaveConfig.getConfigRefreshInterval();
            configRefreshTimerId = vertx.setPeriodic(
                    TimeUnit.SECONDS.toMillis(refreshInterval),
                    id -> refreshConfig());

            // 2.1 启动 TCP Server
            startTcpServer();

            // 2.2 启动 PendingRequest 清理定时器
            requestCleanupTimerId = vertx.setPeriodic(
                    slaveConfig.getRequestCleanupInterval(),
                    id -> pendingRequestManager.cleanupExpired());
            running = true;
            log.info("[start][IoT Modbus TCP Slave 协议 {} 启动成功, serverId={}, port={}]",
                    getId(), serverId, properties.getPort());

            // 3. 启动下行消息订阅
            downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT Modbus TCP Slave 协议 {} 启动失败]", getId(), e);
            if (vertx != null) {
                vertx.close();
            }
            // TODO @AI：其它相关的 close；
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        // 1. 停止下行消息订阅
        try {
            downstreamSubscriber.stop();
        } catch (Exception e) {
            log.error("[stop][下行消息订阅器停止失败]", e);
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
        // 2.3 关闭所有连接
        connectionManager.closeAll();
        // 2.4 关闭 TCP Server
        if (netServer != null) {
            try {
                netServer.close().result();
                log.info("[stop][TCP Server 已关闭]");
            } catch (Exception e) {
                log.error("[stop][TCP Server 关闭失败]", e);
            }
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
        log.info("[stop][IoT Modbus TCP Slave 协议 {} 已停止]", getId());
    }

    /**
     * 启动 TCP Server
     */
    private void startTcpServer() {
        // TODO @AI：host 一定要设置么？
        // 1. 创建 TCP Server
        NetServerOptions options = new NetServerOptions()
                .setPort(properties.getPort())
                .setHost("0.0.0.0");
        netServer = vertx.createNetServer(options);

        // 2. 设置连接处理器
        netServer.connectHandler(this::handleConnection);
        // TODO @AI：是不是 sync 就好，不用 onSuccess/onFailure 了？感觉更简洁。失败，肯定就要抛出异常，结束初始化了！
        netServer.listen()
                .onSuccess(server -> log.info("[startTcpServer][TCP Server 启动成功, port={}]",
                        server.actualPort()))
                .onFailure(e -> log.error("[startTcpServer][TCP Server 启动失败]", e));
    }

    /**
     * 处理新连接
     */
    private void handleConnection(NetSocket socket) {
        log.info("[handleConnection][新连接, remoteAddress={}]", socket.remoteAddress());

        // 1.1 创建带帧格式检测的 RecordParser
        // TODO @AI：看看怎么从这个类里面，拿出去；让这个类的职责更单一；
        RecordParser parser = IotModbusRecordParserFactory.create(
                slaveConfig.getCustomFunctionCode(),
                // 完整帧回调
                // TODO @AI：感觉搞个独立的类，稍微好点？！
                frameBuffer -> {
                    byte[] frameBytes = frameBuffer.getBytes();
                    // 获取该连接的帧格式
                    ConnectionInfo connInfo = connectionManager.getConnectionInfo(socket);
                    IotModbusFrameFormatEnum frameFormat = connInfo != null ? connInfo.getFrameFormat() : null;
                    if (frameFormat == null) {
                        // 未认证的连接，使用首帧检测到的帧格式
                        frameFormat = pendingFrameFormats.get(socket);
                    }
                    if (frameFormat == null) {
                        log.warn("[handleConnection][帧格式未检测到, remoteAddress={}]", socket.remoteAddress());
                        return;
                    }

                    // 解码帧
                    IotModbusFrame frame = frameCodec.decodeResponse(frameBytes, frameFormat);
                    // 交给 UpstreamHandler 处理
                    upstreamHandler.handleFrame(socket, frame, frameFormat);
                },
                // 帧格式检测回调：保存到未认证缓存
                detectedFormat -> {
                    // TODO @AI：是不是不用缓存，每次都探测；因为一般 auth 首包后，基本也没探测的诉求了！
                    pendingFrameFormats.put(socket, detectedFormat);
                    // 如果连接已注册（不太可能在检测阶段），也更新
                    // TODO @AI：是否非必须？！
                    connectionManager.setFrameFormat(socket, detectedFormat);
                    log.debug("[handleConnection][帧格式检测: {}, remoteAddress={}]",
                            detectedFormat, socket.remoteAddress());
                }
        );
        // 1.2 设置数据处理器
        socket.handler(parser);

        // 2.1 连接关闭处理
        socket.closeHandler(v -> {
            pendingFrameFormats.remove(socket);
            ConnectionInfo info = connectionManager.removeConnection(socket);
            // TODO @AI：if return 简化下；
            if (info != null && info.getDeviceId() != null) {
                pollScheduler.stopPolling(info.getDeviceId());
                pendingRequestManager.removeDevice(info.getDeviceId());
                log.info("[handleConnection][连接关闭, deviceId={}, remoteAddress={}]",
                        info.getDeviceId(), socket.remoteAddress());
            } else {
                log.info("[handleConnection][未认证连接关闭, remoteAddress={}]", socket.remoteAddress());
            }
        });
        // 2.2 异常处理
        socket.exceptionHandler(e -> {
            log.error("[handleConnection][连接异常, remoteAddress={}]", socket.remoteAddress(), e);
            socket.close();
        });
    }

    /**
     * 刷新配置
     */
    private synchronized void refreshConfig() {
        try {
            // 1. 从 biz 拉取最新配置
            List<IotModbusDeviceConfigRespDTO> configs = configCacheService.refreshConfig();
            log.debug("[refreshConfig][获取到 {} 个 Modbus 设备配置]", configs.size());

            // 2. 更新已连接设备的轮询任务（仅 mode=1）
            for (IotModbusDeviceConfigRespDTO config : configs) {
                try {
                    if (config.getMode() != null
                            && config.getMode().equals(IotModbusModeEnum.POLLING.getMode())) {
                        // 只有已连接的设备才启动轮询
                        ConnectionInfo connInfo = connectionManager.getConnectionInfoByDeviceId(config.getDeviceId());
                        if (connInfo != null) {
                            pollScheduler.updatePolling(config);
                        }
                    }
                } catch (Exception e) {
                    log.error("[refreshConfig][处理设备配置失败, deviceId={}]", config.getDeviceId(), e);
                }
            }

            // 3. 清理已删除设备的资源
            configCacheService.cleanupRemovedDevices(configs, deviceId -> {
                pollScheduler.stopPolling(deviceId);
                pendingRequestManager.removeDevice(deviceId);
            });
        } catch (Exception e) {
            log.error("[refreshConfig][刷新配置失败]", e);
        }
    }

}
