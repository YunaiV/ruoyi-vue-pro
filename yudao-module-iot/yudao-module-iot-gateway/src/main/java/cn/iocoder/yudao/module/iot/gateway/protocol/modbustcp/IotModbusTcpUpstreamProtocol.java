package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// TODO @AI：注释可以简化下
/**
 * IoT Modbus TCP 上行协议
 *
 * 负责：
 * 1. 定时从 biz 拉取 Modbus 设备配置
 * 2. 管理 Modbus TCP 连接
 * 3. 调度轮询任务
 * 4. 处理采集数据上报
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpUpstreamProtocol {

    private final IotGatewayProperties.ModbusTcpProperties modbusTcpProperties;
    private final IotDeviceMessageService messageService;
    private final IotModbusTcpConnectionManager connectionManager;
    private final IotModbusTcpPollScheduler pollScheduler;
    private final IotModbusTcpConfigCacheService configCacheService;
    private final IotModbusTcpUpstreamHandler upstreamHandler;
    private final Vertx vertx;

    /**
     * 服务器 ID，用于标识当前网关实例
     */
    @Getter
    private final String serverId = UUID.randomUUID().toString();

    /**
     * 配置刷新定时器 ID
     */
    private Long configRefreshTimerId;

    @PostConstruct
    public void start() {
        log.info("[start][Modbus TCP 协议启动, serverId={}]", serverId);

        // 0. 设置 serverId 到上行处理器
        upstreamHandler.setServerId(serverId);

        // 1. 首次加载配置
        refreshConfig();

        // 2. 启动配置定时刷新
        int refreshInterval = modbusTcpProperties.getConfigRefreshInterval();
        configRefreshTimerId = vertx.setPeriodic(
                TimeUnit.SECONDS.toMillis(refreshInterval),
                id -> refreshConfig()
        );
        log.info("[start][配置刷新定时器已启动, 间隔={}秒]", refreshInterval);
    }

    @PreDestroy
    public void stop() {
        log.info("[stop][Modbus TCP 协议停止]");

        // 1. 取消配置刷新定时器
        if (configRefreshTimerId != null) {
            vertx.cancelTimer(configRefreshTimerId);
        }

        // 2. 停止轮询调度器
        pollScheduler.stopAll();

        // 3. 关闭所有连接
        connectionManager.closeAll();
    }

    /**
     * 刷新配置
     */
    private void refreshConfig() {
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
                    // TODO @AI：【重要】如果点位配置没变化，是不是不用 update？
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
