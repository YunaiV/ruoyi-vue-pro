package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.AbstractIotModbusPollScheduler;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerPendingRequestManager.PendingRequest;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * IoT Modbus TCP Server 轮询调度器：编码读请求帧，通过 TCP 连接发送到设备，注册 PendingRequest 等待响应
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerPollScheduler extends AbstractIotModbusPollScheduler {

    private final IotModbusTcpServerConnectionManager connectionManager;
    private final IotModbusFrameEncoder frameEncoder;
    private final IotModbusTcpServerPendingRequestManager pendingRequestManager;
    private final IotModbusTcpServerConfigCacheService configCacheService;
    private final int requestTimeout;
    /**
     * TCP 事务 ID 自增器（与 DownstreamHandler 共享）
     */
    @Getter
    private final AtomicInteger transactionIdCounter;

    public IotModbusTcpServerPollScheduler(Vertx vertx,
                                          IotModbusTcpServerConnectionManager connectionManager,
                                          IotModbusFrameEncoder frameEncoder,
                                          IotModbusTcpServerPendingRequestManager pendingRequestManager,
                                          int requestTimeout,
                                          AtomicInteger transactionIdCounter,
                                          IotModbusTcpServerConfigCacheService configCacheService) {
        super(vertx);
        this.connectionManager = connectionManager;
        this.frameEncoder = frameEncoder;
        this.pendingRequestManager = pendingRequestManager;
        this.requestTimeout = requestTimeout;
        this.transactionIdCounter = transactionIdCounter;
        this.configCacheService = configCacheService;
    }

    // ========== 轮询执行 ==========

    /**
     * 轮询单个点位
     */
    @Override
    @SuppressWarnings("DuplicatedCode")
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
        ConnectionInfo connection = connectionManager.getConnectionInfoByDeviceId(deviceId);
        if (connection == null) {
            log.debug("[pollPoint][设备 {} 没有连接，跳过轮询]", deviceId);
            return;
        }
        // 2.2 获取 slave ID
        IotModbusFrameFormatEnum frameFormat = connection.getFrameFormat();
        Assert.notNull(frameFormat, "设备 {} 的帧格式不能为空", deviceId);
        Integer slaveId = connection.getSlaveId();
        Assert.notNull(connection.getSlaveId(), "设备 {} 的 slaveId 不能为空", deviceId);

        // 3.1 编码读请求
        Integer transactionId = frameFormat == IotModbusFrameFormatEnum.MODBUS_TCP
                ? (transactionIdCounter.incrementAndGet() & 0xFFFF)
                : null;
        byte[] data = frameEncoder.encodeReadRequest(slaveId, point.getFunctionCode(),
                point.getRegisterAddress(), point.getRegisterCount(), frameFormat, transactionId);
        // 3.2 注册 PendingRequest
        PendingRequest pendingRequest = new PendingRequest(
                deviceId, point.getId(), point.getIdentifier(),
                slaveId, point.getFunctionCode(),
                point.getRegisterAddress(), point.getRegisterCount(),
                transactionId,
                System.currentTimeMillis() + requestTimeout);
        pendingRequestManager.addRequest(pendingRequest);
        // 3.3 发送读请求
        connectionManager.sendToDevice(deviceId, data).onSuccess(v ->
                log.debug("[pollPoint][设备={}, 点位={}, FC={}, 地址={}, 数量={}]",
                        deviceId, point.getIdentifier(), point.getFunctionCode(),
                        point.getRegisterAddress(), point.getRegisterCount())
        ).onFailure(e ->
                log.warn("[pollPoint][发送失败, 设备={}, 点位={}]", deviceId, point.getIdentifier(), e)
        );
    }

}
