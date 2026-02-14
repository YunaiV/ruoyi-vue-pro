package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream.IotModbusTcpClientDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream.IotModbusTcpClientDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.upstream.IotModbusTcpClientUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 Modbus TCP Client 协议：主动轮询 Modbus 从站设备数据
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpClientProtocol implements IotProtocol {

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
    private final IotModbusTcpClientConnectionManager connectionManager;
    /**
     * 下行消息订阅者
     */
    private IotModbusTcpClientDownstreamSubscriber downstreamSubscriber;

    private final IotModbusTcpClientConfigCacheService configCacheService;
    private final IotModbusTcpClientPollScheduler pollScheduler;

    public IotModbusTcpClientProtocol(ProtocolProperties properties) {
        IotModbusTcpClientConfig modbusTcpClientConfig = properties.getModbusTcpClient();
        Assert.notNull(modbusTcpClientConfig, "Modbus TCP Client 协议配置（modbusTcpClient）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化 Vertx
        this.vertx = Vertx.vertx();

        // 初始化 Manager
        RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
        IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        IotDeviceMessageService messageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.configCacheService = new IotModbusTcpClientConfigCacheService(deviceApi);
        this.connectionManager = new IotModbusTcpClientConnectionManager(redissonClient, vertx,
                messageService, configCacheService, serverId);

        // 初始化 Handler
        IotModbusTcpClientUpstreamHandler upstreamHandler = new IotModbusTcpClientUpstreamHandler(messageService, serverId);

        // 初始化轮询调度器
        this.pollScheduler = new IotModbusTcpClientPollScheduler(vertx, connectionManager, upstreamHandler, configCacheService);
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.MODBUS_TCP_CLIENT;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT Modbus TCP Client 协议 {} 已经在运行中]", getId());
            return;
        }

        try {
            // 1.1 首次加载配置
            refreshConfig();
            // 1.2 启动配置刷新定时器
            int refreshInterval = properties.getModbusTcpClient().getConfigRefreshInterval();
            configRefreshTimerId = vertx.setPeriodic(
                    TimeUnit.SECONDS.toMillis(refreshInterval),
                    id -> refreshConfig()
            );
            running = true;
            log.info("[start][IoT Modbus TCP Client 协议 {} 启动成功，serverId={}]", getId(), serverId);

            // 2. 启动下行消息订阅者
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            IotModbusTcpClientDownstreamHandler downstreamHandler = new IotModbusTcpClientDownstreamHandler(connectionManager,
                    configCacheService);
            this.downstreamSubscriber = new IotModbusTcpClientDownstreamSubscriber(this, downstreamHandler, messageBus);
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT Modbus TCP Client 协议 {} 启动失败]", getId(), e);
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
                log.info("[stop][IoT Modbus TCP Client 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT Modbus TCP Client 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2.1 取消配置刷新定时器
        if (configRefreshTimerId != null) {
            vertx.cancelTimer(configRefreshTimerId);
            configRefreshTimerId = null;
        }
        // 2.2 停止轮询调度器
        pollScheduler.stopAll();
        // 2.3 关闭所有连接
        connectionManager.closeAll();

        // 3. 关闭 Vert.x 实例
        if (vertx != null) {
            try {
                vertx.close().result();
                log.info("[stop][IoT Modbus TCP Client 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT Modbus TCP Client 协议 {} Vertx 关闭失败]", getId(), e);
            }
        }
        running = false;
        log.info("[stop][IoT Modbus TCP Client 协议 {} 已停止]", getId());
    }

    /**
     * 刷新配置
     */
    private synchronized void refreshConfig() {
        try {
            // 1. 从 biz 拉取最新配置（API 失败时返回 null）
            List<IotModbusDeviceConfigRespDTO> configs = configCacheService.refreshConfig();
            if (configs == null) {
                log.warn("[refreshConfig][API 失败，跳过本轮刷新]");
                return;
            }
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
            Set<Long> removedDeviceIds = configCacheService.cleanupRemovedDevices(configs);
            for (Long deviceId : removedDeviceIds) {
                pollScheduler.stopPolling(deviceId);
                connectionManager.removeDevice(deviceId);
            }
        } catch (Exception e) {
            log.error("[refreshConfig][刷新配置失败]", e);
        }
    }

}
