package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.AbstractIotModbusPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusTcpMasterUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.handler.upstream.IotModbusTcpUpstreamHandler;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT Modbus TCP Master 轮询调度器：管理点位的轮询定时器，调度读取任务并上报结果
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpPollScheduler extends AbstractIotModbusPollScheduler {

    private final IotModbusTcpConnectionManager connectionManager;
    private final IotModbusTcpUpstreamHandler upstreamHandler;
    private final IotModbusTcpConfigCacheService configCacheService;

    public IotModbusTcpPollScheduler(Vertx vertx,
                                     IotModbusTcpConnectionManager connectionManager,
                                     IotModbusTcpUpstreamHandler upstreamHandler,
                                     IotModbusTcpConfigCacheService configCacheService) {
        super(vertx);
        this.connectionManager = connectionManager;
        this.upstreamHandler = upstreamHandler;
        this.configCacheService = configCacheService;
    }

    // ========== 轮询执行 ==========

    /**
     * 轮询单个点位
     */
    @Override
    protected void pollPoint(Long deviceId, Long pointId) {
        // 1.1 从 configCache 获取最新配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(deviceId);
        if (config == null || CollUtil.isEmpty(config.getPoints())) {
            log.warn("[pollPoint][设备 {} 没有配置]", deviceId);
            return;
        }
        // 1.2 查找点位
        IotModbusPointRespDTO point = IotModbusCommonUtils.findPointById(config, pointId);
        if (point == null) {
            log.warn("[pollPoint][设备 {} 点位 {} 未找到]", deviceId, pointId);
            return;
        }

        // 2.1 获取连接
        IotModbusTcpConnectionManager.ModbusConnection connection = connectionManager.getConnection(deviceId);
        if (connection == null) {
            log.warn("[pollPoint][设备 {} 没有连接]", deviceId);
            return;
        }
        // 2.2 获取 slave ID
        Integer slaveId = connectionManager.getSlaveId(deviceId);
        Assert.notNull(slaveId, "设备 {} 没有配置 slaveId", deviceId);

        // 3. 执行 Modbus 读取
        IotModbusTcpMasterUtils.read(connection, slaveId, point)
                .onSuccess(rawValue -> upstreamHandler.handleReadResult(config, point, rawValue))
                .onFailure(e -> log.error("[pollPoint][读取点位失败, deviceId={}, identifier={}]",
                        deviceId, point.getIdentifier(), e));
    }

}
