package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.client.IotModbusTcpClient;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp.handler.upstream.IotModbusTcpUpstreamHandler;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * IoT Modbus TCP 轮询调度器：管理点位的轮询定时器，调度读取任务并上报结果
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpPollScheduler {

    private final Vertx vertx;
    private final IotModbusTcpConnectionManager connectionManager;
    private final IotModbusTcpClient modbusClient;
    private final IotModbusTcpUpstreamHandler upstreamHandler;

    /**
     * 设备点位的定时器映射：deviceId -> (pointId -> PointTimerInfo)
     */
    private final Map<Long, Map<Long, PointTimerInfo>> devicePointTimers = new ConcurrentHashMap<>();

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
     *
     * 1. 【删除】点位：停止对应的轮询定时器
     * 2. 【新增】点位：创建对应的轮询定时器
     * 3. 【修改】点位：pollInterval 变化，重建对应的轮询定时器
     * 4. 其他属性变化（包括未变化的）：不处理（下次轮询时自动使用新配置）
     */
    public void updatePolling(IotModbusDeviceConfigRespDTO config) {
        Long deviceId = config.getDeviceId();
        List<IotModbusPointRespDTO> newPoints = config.getPoints();
        Map<Long, PointTimerInfo> currentTimers = devicePointTimers
                .computeIfAbsent(deviceId, k -> new ConcurrentHashMap<>());
        // 1.1 计算新配置（包括新增和修改的点位）中的点位 ID 集合
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
     * 轮询单个点位
     */
    private void pollPoint(IotModbusDeviceConfigRespDTO config, IotModbusPointRespDTO point) {
        // 1.1 获取连接
        IotModbusTcpConnectionManager.ModbusConnection connection = connectionManager.getConnection(config.getDeviceId());
        if (connection == null) {
            log.warn("[pollPoint][设备 {} 没有连接]", config.getDeviceId());
            return;
        }
        // 1.2 获取 slave ID
        Integer slaveId = connectionManager.getSlaveId(config.getDeviceId());
        if (slaveId == null) {
            log.warn("[pollPoint][设备 {} 没有 slaveId]", config.getDeviceId());
            return;
        }

        // 2. 执行 Modbus 读取
        modbusClient.read(connection, slaveId, point)
                .onSuccess(rawValue -> upstreamHandler.handleReadResult(config, point, rawValue))
                .onFailure(e -> log.error("[pollPoint][读取点位失败, deviceId={}, identifier={}]",
                        config.getDeviceId(), point.getIdentifier(), e));
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
