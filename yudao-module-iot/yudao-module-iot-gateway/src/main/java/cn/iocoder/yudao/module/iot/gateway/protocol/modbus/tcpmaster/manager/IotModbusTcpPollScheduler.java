package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.client.IotModbusTcpClientUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster.handler.upstream.IotModbusTcpUpstreamHandler;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

// TODO @AI：类的命名上，要体现上 master。其它类似 /Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-iot/yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/protocol/modbus/tcpmaster 也要！
/**
 * IoT Modbus TCP Master 轮询调度器：管理点位的轮询定时器，调度读取任务并上报结果
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpPollScheduler {

    private final Vertx vertx;
    private final IotModbusTcpConnectionManager connectionManager;
    private final IotModbusTcpUpstreamHandler upstreamHandler;
    private final IotModbusTcpConfigCacheService configCacheService;

    /**
     * 同设备最小请求间隔（毫秒），防止 Modbus 设备性能不足时请求堆积
     */
    private static final long MIN_REQUEST_INTERVAL = 100;

    /**
     * 设备点位的定时器映射：deviceId -> (pointId -> PointTimerInfo)
     */
    private final Map<Long, Map<Long, PointTimerInfo>> devicePointTimers = new ConcurrentHashMap<>();

    /**
     * per-device 请求队列：deviceId -> 待执行请求队列
     */
    private final Map<Long, Queue<Runnable>> deviceRequestQueues = new ConcurrentHashMap<>();

    /**
     * per-device 上次请求时间戳：deviceId -> lastRequestTimeMs
     */
    private final Map<Long, Long> deviceLastRequestTime = new ConcurrentHashMap<>();

    /**
     * per-device 延迟 timer 标记：deviceId -> 是否有延迟 timer 在等待
     */
    private final Map<Long, Boolean> deviceDelayTimerActive = new ConcurrentHashMap<>();

    public IotModbusTcpPollScheduler(Vertx vertx,
                                     IotModbusTcpConnectionManager connectionManager,
                                     IotModbusTcpUpstreamHandler upstreamHandler,
                                     IotModbusTcpConfigCacheService configCacheService) {
        this.vertx = vertx;
        this.connectionManager = connectionManager;
        this.upstreamHandler = upstreamHandler;
        this.configCacheService = configCacheService;
    }

    // ========== 点位定时器 ==========

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

    // ========== 轮询管理 ==========

    /**
     * 更新轮询任务（增量更新）
     *
     * 1. 【删除】点位：停止对应的轮询定时器
     * 2. 【新增】点位：创建对应的轮询定时器
     * 3. 【修改】点位：pollInterval 变化，重建对应的轮询定时器
     * 4. 其他属性变化：不需要重建定时器（pollPoint 运行时从 configCache 取最新 point）
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
                Long timerId = createPollTimer(deviceId, pointId, newPollInterval);
                if (timerId != null) {
                    currentTimers.put(pointId, new PointTimerInfo(timerId, newPollInterval));
                    log.debug("[updatePolling][设备 {} 点位 {} 定时器已创建, interval={}ms]",
                            deviceId, pointId, newPollInterval);
                }
            } else if (!Objects.equals(existingTimer.getPollInterval(), newPollInterval)) {
                // 3.2 pollInterval 变化：重建定时器
                vertx.cancelTimer(existingTimer.getTimerId());
                Long timerId = createPollTimer(deviceId, pointId, newPollInterval);
                if (timerId != null) {
                    currentTimers.put(pointId, new PointTimerInfo(timerId, newPollInterval));
                    log.debug("[updatePolling][设备 {} 点位 {} 定时器已更新, interval={}ms -> {}ms]",
                            deviceId, pointId, existingTimer.getPollInterval(), newPollInterval);
                } else {
                    currentTimers.remove(pointId);
                }
            }
            // 3.3 其他属性变化：无需重建定时器，因为 pollPoint() 运行时从 configCache 获取最新 point，自动使用新配置
        }
    }

    /**
     * 创建轮询定时器
     * <p>
     * 闭包只捕获 deviceId 和 pointId，运行时从 configCache 获取最新配置，避免旧快照问题。
     */
    private Long createPollTimer(Long deviceId, Long pointId, Integer pollInterval) {
        if (pollInterval == null || pollInterval <= 0) {
            return null;
        }
        return vertx.setPeriodic(pollInterval, timerId -> {
            try {
                submitPollRequest(deviceId, pointId);
            } catch (Exception e) {
                log.error("[createPollTimer][轮询点位失败, deviceId={}, pointId={}]", deviceId, pointId, e);
            }
        });
    }

    // ========== 请求队列（per-device 限速） ==========

    /**
     * 提交轮询请求到设备请求队列（保证同设备请求间隔）
     */
    private void submitPollRequest(Long deviceId, Long pointId) {
        Queue<Runnable> queue = deviceRequestQueues.computeIfAbsent(deviceId, k -> new ConcurrentLinkedQueue<>());
        queue.offer(() -> pollPoint(deviceId, pointId));
        processDeviceQueue(deviceId);
    }

    /**
     * 处理设备请求队列
     */
    private void processDeviceQueue(Long deviceId) {
        Queue<Runnable> queue = deviceRequestQueues.get(deviceId);
        if (CollUtil.isEmpty(queue)) {
            return;
        }
        // 检查是否已有延迟 timer 在等待
        if (Boolean.TRUE.equals(deviceDelayTimerActive.get(deviceId))) {
            return;
        }
        long now = System.currentTimeMillis();
        long lastTime = deviceLastRequestTime.getOrDefault(deviceId, 0L);
        long elapsed = now - lastTime;
        if (elapsed >= MIN_REQUEST_INTERVAL) {
            // 满足间隔要求，立即执行
            Runnable task = queue.poll();
            if (task != null) {
                deviceLastRequestTime.put(deviceId, now);
                task.run();
                // 继续处理队列中的下一个（如果有的话，需要延迟）
                if (!queue.isEmpty()) {
                    scheduleNextRequest(deviceId);
                }
            }
        } else {
            // 需要延迟
            scheduleNextRequest(deviceId, MIN_REQUEST_INTERVAL - elapsed);
        }
    }

    private void scheduleNextRequest(Long deviceId) {
        scheduleNextRequest(deviceId, MIN_REQUEST_INTERVAL);
    }

    private void scheduleNextRequest(Long deviceId, long delayMs) {
        deviceDelayTimerActive.put(deviceId, true);
        vertx.setTimer(delayMs, id -> {
            deviceDelayTimerActive.put(deviceId, false);
            Queue<Runnable> queue = deviceRequestQueues.get(deviceId);
            if (CollUtil.isEmpty(queue)) {
                Runnable task = queue.poll();
                if (task != null) {
                    deviceLastRequestTime.put(deviceId, System.currentTimeMillis());
                    task.run();
                }
                // 继续处理
                if (queue != null && !queue.isEmpty()) {
                    scheduleNextRequest(deviceId);
                }
            }
        });
    }

    // ========== 轮询执行 ==========

    /**
     * 轮询单个点位
     */
    private void pollPoint(Long deviceId, Long pointId) {
        // 1.1 从 configCache 获取最新配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(deviceId);
        if (config == null || CollUtil.isEmpty(config.getPoints())) {
            log.warn("[pollPoint][设备 {} 没有配置]", deviceId);
            return;
        }
        // 1.2 查找点位
        // TODO @AI：是不是这里，可以抽到 IotModbusUtils 里？感觉应该有几个地方需要的；
        IotModbusPointRespDTO point = CollUtil.findOne(config.getPoints(), p -> p.getId().equals(pointId));
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
        if (slaveId == null) {
            log.warn("[pollPoint][设备 {} 没有 slaveId]", deviceId);
            return;
        }

        // 3. 执行 Modbus 读取
        IotModbusTcpClientUtils.read(connection, slaveId, point)
                .onSuccess(rawValue -> upstreamHandler.handleReadResult(config, point, rawValue))
                .onFailure(e -> log.error("[pollPoint][读取点位失败, deviceId={}, identifier={}]",
                        deviceId, point.getIdentifier(), e));
    }

    // ========== 停止 ==========

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
        // 清理请求队列
        deviceRequestQueues.remove(deviceId);
        deviceLastRequestTime.remove(deviceId);
        deviceDelayTimerActive.remove(deviceId);
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
