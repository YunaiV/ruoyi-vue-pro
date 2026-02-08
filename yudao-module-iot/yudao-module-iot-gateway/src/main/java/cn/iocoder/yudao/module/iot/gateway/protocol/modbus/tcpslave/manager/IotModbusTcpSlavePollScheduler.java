package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlaveConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager.IotModbusTcpSlavePendingRequestManager.PendingRequest;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * IoT Modbus TCP Slave 轮询调度器
 * <p>
 * 管理点位的轮询定时器，为云端轮询模式的设备调度读取任务。
 * 与 tcpmaster 的 {@code IotModbusTcpPollScheduler} 不同，这里不通过 j2mod 直接读取，而是：
 * 1. 编码 Modbus 读请求帧
 * 2. 通过 ConnectionManager 发送到设备的 TCP 连接
 * 3. 将请求注册到 PendingRequestManager，等待设备响应
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpSlavePollScheduler {

    private final Vertx vertx;
    private final IotModbusTcpSlaveConnectionManager connectionManager;
    private final IotModbusFrameEncoder frameEncoder;
    private final IotModbusTcpSlavePendingRequestManager pendingRequestManager;
    private final int requestTimeout;

    /**
     * 设备点位的定时器映射：deviceId -> (pointId -> PointTimerInfo)
     */
    private final Map<Long, Map<Long, PointTimerInfo>> devicePointTimers = new ConcurrentHashMap<>();

    /**
     * TCP 事务 ID 自增器
     */
    private final AtomicInteger transactionIdCounter = new AtomicInteger(0);

    /**
     * 点位定时器信息
     */
    @Data
    @AllArgsConstructor
    private static class PointTimerInfo {

        /**
         * Vert.x 定时器 ID
         */
        private Long timerId;
        /**
         * 轮询间隔（用于判断是否需要更新定时器）
         */
        private Integer pollInterval;

    }

    /**
     * 更新轮询任务（增量更新）
     */
    public void updatePolling(IotModbusDeviceConfigRespDTO config) {
        Long deviceId = config.getDeviceId();
        List<IotModbusPointRespDTO> newPoints = config.getPoints();
        Map<Long, PointTimerInfo> currentTimers = devicePointTimers
                .computeIfAbsent(deviceId, k -> new ConcurrentHashMap<>());
        // 1.1 计算新配置中的点位 ID 集合
        Set<Long> newPointIds = convertSet(newPoints, IotModbusPointRespDTO::getId);
        // 1.2 计算删除的点位 ID 集合
        Set<Long> removedPointIds = new HashSet<>(currentTimers.keySet());
        removedPointIds.removeAll(newPointIds);

        // 2. 处理删除的点位：停止不再存在的定时器
        for (Long pointId : removedPointIds) {
            PointTimerInfo timerInfo = currentTimers.remove(pointId);
            if (timerInfo != null) {
                vertx.cancelTimer(timerInfo.getTimerId());
                log.debug("[updatePolling][设备 {} 点位 {} 定时器已删除]", deviceId, pointId);
            }
        }

        // 3. 处理新增和修改的点位
        if (CollUtil.isEmpty(newPoints)) {
            return;
        }
        for (IotModbusPointRespDTO point : newPoints) {
            Long pointId = point.getId();
            Integer newPollInterval = point.getPollInterval();
            PointTimerInfo existingTimer = currentTimers.get(pointId);
            // 3.1 新增点位：创建定时器
            if (existingTimer == null) {
                Long timerId = createPollTimer(config, point);
                if (timerId != null) {
                    currentTimers.put(pointId, new PointTimerInfo(timerId, newPollInterval));
                    log.debug("[updatePolling][设备 {} 点位 {} 定时器已创建, interval={}ms]",
                            deviceId, pointId, newPollInterval);
                }
            } else if (!Objects.equals(existingTimer.getPollInterval(), newPollInterval)) {
                // 3.2 pollInterval 变化：重建定时器
                vertx.cancelTimer(existingTimer.getTimerId());
                Long timerId = createPollTimer(config, point);
                if (timerId != null) {
                    currentTimers.put(pointId, new PointTimerInfo(timerId, newPollInterval));
                    log.debug("[updatePolling][设备 {} 点位 {} 定时器已更新, interval={}ms -> {}ms]",
                            deviceId, pointId, existingTimer.getPollInterval(), newPollInterval);
                } else {
                    currentTimers.remove(pointId);
                }
            }
            // 3.3 其他属性变化：不处理（下次轮询时自动使用新配置）
        }
    }

    /**
     * 创建轮询定时器
     */
    private Long createPollTimer(IotModbusDeviceConfigRespDTO config, IotModbusPointRespDTO point) {
        if (point.getPollInterval() == null || point.getPollInterval() <= 0) {
            return null;
        }
        return vertx.setPeriodic(point.getPollInterval(), timerId -> {
            try {
                pollPoint(config, point);
            } catch (Exception e) {
                log.error("[createPollTimer][轮询点位失败, deviceId={}, identifier={}]",
                        config.getDeviceId(), point.getIdentifier(), e);
            }
        });
    }

    /**
     * 轮询单个点位：编码读请求帧 → 发送 → 注册 PendingRequest
     */
    private void pollPoint(IotModbusDeviceConfigRespDTO config, IotModbusPointRespDTO point) {
        Long deviceId = config.getDeviceId();
        // 1. 获取连接信息
        ConnectionInfo connInfo = connectionManager.getConnectionInfoByDeviceId(deviceId);
        if (connInfo == null) {
            log.debug("[pollPoint][设备 {} 没有连接，跳过轮询]", deviceId);
            return;
        }

        // 2.1 确定帧格式和事务 ID
        IotModbusFrameFormatEnum frameFormat = connInfo.getFrameFormat();
        if (frameFormat == null) {
            log.warn("[pollPoint][设备 {} 帧格式为空，跳过轮询]", deviceId);
            return;
        }
        // TODO @AI：是不是得按照设备递增？
        Integer transactionId = frameFormat == IotModbusFrameFormatEnum.MODBUS_TCP
                ? (transactionIdCounter.incrementAndGet() & 0xFFFF)
                : null;
        int slaveId = connInfo.getSlaveId() != null ? connInfo.getSlaveId() : 1;
        // 2.2 编码读请求
        byte[] data = frameEncoder.encodeReadRequest(slaveId, point.getFunctionCode(),
                point.getRegisterAddress(), point.getRegisterCount(), frameFormat, transactionId);
        // 2.3 注册 PendingRequest
        PendingRequest pendingRequest = new PendingRequest(
                deviceId, point.getId(), point.getIdentifier(),
                slaveId, point.getFunctionCode(),
                point.getRegisterAddress(), point.getRegisterCount(),
                transactionId,
                System.currentTimeMillis() + requestTimeout);
        pendingRequestManager.addRequest(pendingRequest);

        // 3. 发送读请求
        connectionManager.sendToDevice(deviceId, data);
        log.debug("[pollPoint][设备={}, 点位={}, FC={}, 地址={}, 数量={}]",
                deviceId, point.getIdentifier(), point.getFunctionCode(),
                point.getRegisterAddress(), point.getRegisterCount());
    }

    /**
     * 停止设备的轮询
     */
    public void stopPolling(Long deviceId) {
        Map<Long, PointTimerInfo> timers = devicePointTimers.remove(deviceId);
        if (CollUtil.isEmpty(timers)) {
            return;
        }
        for (PointTimerInfo timerInfo : timers.values()) {
            vertx.cancelTimer(timerInfo.getTimerId());
        }
        log.debug("[stopPolling][设备 {} 停止了 {} 个轮询定时器]", deviceId, timers.size());
    }

    /**
     * 停止所有轮询
     */
    public void stopAll() {
        for (Long deviceId : new ArrayList<>(devicePointTimers.keySet())) {
            stopPolling(deviceId);
        }
    }

}
