package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.IotModbusDataConverter;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.client.IotModbusTcpClient;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.handler.downstream.IotModbusTcpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.handler.downstream.IotModbusTcpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.handler.upstream.IotModbusTcpUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.manager.IotModbusTcpConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.manager.IotModbusTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.manager.IotModbusTcpPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 Modbus TCP Master 协议：主动轮询 Modbus 从站设备数据
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpMasterProtocol implements IotProtocol {

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
     * 配置刷新定时器 ID
     */
    private Long configRefreshTimerId;

    /**
     * 连接管理器
     */
    private final IotModbusTcpConnectionManager connectionManager;
    /**
     * 下行消息订阅者
     */
    private final IotModbusTcpDownstreamSubscriber downstreamSubscriber;

    private final IotModbusTcpConfigCacheService configCacheService;
    private final IotModbusTcpPollScheduler pollScheduler;

    public IotModbusTcpMasterProtocol(ProtocolProperties properties) {
        IotModbusTcpMasterConfig modbusTcpMasterConfig = properties.getModbusTcpMaster();
        Assert.notNull(modbusTcpMasterConfig, "Modbus TCP Master 协议配置（modbusTcpMaster）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化 Vertx
        this.vertx = Vertx.vertx();

        // 初始化 Manager
        RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
        IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.connectionManager = new IotModbusTcpConnectionManager(redissonClient, vertx);
        this.configCacheService = new IotModbusTcpConfigCacheService(deviceApi);

        // 初始化 Handler
        IotModbusDataConverter dataConverter = new IotModbusDataConverter();
        IotModbusTcpClient modbusClient = new IotModbusTcpClient();
        IotDeviceMessageService messageService = SpringUtil.getBean(IotDeviceMessageService.class);
        IotModbusTcpUpstreamHandler upstreamHandler = new IotModbusTcpUpstreamHandler(messageService, dataConverter, serverId);
        IotModbusTcpDownstreamHandler downstreamHandler = new IotModbusTcpDownstreamHandler(connectionManager,
                modbusClient, dataConverter, configCacheService);

        // 初始化轮询调度器
        this.pollScheduler = new IotModbusTcpPollScheduler(vertx, connectionManager, modbusClient, upstreamHandler);

        // 初始化下行消息订阅者
        IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
        this.downstreamSubscriber = new IotModbusTcpDownstreamSubscriber(this, downstreamHandler, messageBus);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.MODBUS_TCP_MASTER;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT Modbus TCP Master 协议 {} 已经在运行中]", getId());
            return;
        }

        try {
            // 1.1 首次加载配置
            refreshConfig();
            // 1.2 启动配置刷新定时器
            int refreshInterval = properties.getModbusTcpMaster().getConfigRefreshInterval();
            configRefreshTimerId = vertx.setPeriodic(
                    TimeUnit.SECONDS.toMillis(refreshInterval),
                    id -> refreshConfig()
            );
            running = true;
            log.info("[start][IoT Modbus TCP Master 协议 {} 启动成功，serverId={}]", getId(), serverId);

            // 2. 启动下行消息订阅者
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT Modbus TCP Master 协议 {} 启动失败]", getId(), e);
            // 启动失败时关闭资源
            if (vertx != null) {
                vertx.close();
            }
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        // 1. 停止下行消息订阅者
        try {
            downstreamSubscriber.stop();
            log.info("[stop][IoT Modbus TCP Master 协议 {} 下行消息订阅者已停止]", getId());
        } catch (Exception e) {
            log.error("[stop][IoT Modbus TCP Master 协议 {} 下行消息订阅者停止失败]", getId(), e);
        }

        // 2.1 取消配置刷新定时器
        if (configRefreshTimerId != null) {
            vertx.cancelTimer(configRefreshTimerId);
            configRefreshTimerId = null;
        }
        // 2.2 停止轮询调度器
        pollScheduler.stopAll();
        log.info("[stop][IoT Modbus TCP Master 协议 {} 轮询调度器已停止]", getId());
        // 2.3 关闭所有连接
        connectionManager.closeAll();
        log.info("[stop][IoT Modbus TCP Master 协议 {} 连接管理器已关闭]", getId());

        // 3. 关闭 Vert.x 实例
        if (vertx != null) {
            try {
                vertx.close().result();
                log.info("[stop][IoT Modbus TCP Master 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT Modbus TCP Master 协议 {} Vertx 关闭失败]", getId(), e);
            }
        }
        running = false;
        log.info("[stop][IoT Modbus TCP Master 协议 {} 已停止]", getId());
    }

    /**
     * 刷新配置
     */
    private synchronized void refreshConfig() {
        try {
            // 1. 从 biz 拉取最新配置
            List<IotModbusDeviceConfigRespDTO> configs = configCacheService.refreshConfig();
            log.debug("[refreshConfig][获取到 {} 个 Modbus 设备配置]", configs.size());

            // 2. 更新连接和轮询任务
            for (IotModbusDeviceConfigRespDTO config : configs) {
                try {
                    // 2.1 确保连接存在
                    connectionManager.ensureConnection(config);
                    // 2.2 更新轮询任务
                    pollScheduler.updatePolling(config);
                } catch (Exception e) {
                    log.error("[refreshConfig][处理设备配置失败, deviceId={}]", config.getDeviceId(), e);
                }
            }

            // 3. 清理已删除设备的资源
            configCacheService.cleanupRemovedDevices(configs, deviceId -> {
                pollScheduler.stopPolling(deviceId);
                connectionManager.removeDevice(deviceId);
            });
        } catch (Exception e) {
            log.error("[refreshConfig][刷新配置失败]", e);
        }
    }

}
