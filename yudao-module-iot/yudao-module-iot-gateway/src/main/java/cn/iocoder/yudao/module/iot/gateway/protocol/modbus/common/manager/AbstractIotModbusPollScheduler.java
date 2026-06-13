package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Modbus 轮询调度器基类
 * <p>
 * 封装通用的定时器管理、per-device 请求队列限速逻辑。
 * 子类只需实现 {@link #pollPoint(Long, Long)} 定义具体的轮询动作。
 * 如需将多个点位合并为一个轮询任务，可覆盖 {@link #buildPollTasks(IotModbusDeviceConfigRespDTO)}
 * 和 {@link #pollTask(Long, String)}。
 * <p>
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class AbstractIotModbusPollScheduler {

    protected final Vertx vertx;

    /**
     * 同设备最小请求间隔（毫秒），防止 Modbus 设备性能不足时请求堆积
     */
    private static final long MIN_REQUEST_INTERVAL = 1000;
    /**
     * 每个设备请求队列的最大长度，超出时丢弃最旧请求
     */
    private static final int MAX_QUEUE_SIZE = 1000;

    /**
     * 设备轮询任务的定时器映射：deviceId -> (taskKey -> PollTimerInfo)
     */
    private final Map<Long, Map<String, PollTimerInfo>> devicePollTimers = new ConcurrentHashMap<>();

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

    protected AbstractIotModbusPollScheduler(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * 轮询任务信息
     */
    @Data
    @AllArgsConstructor
    protected static class PollTask {

        /**
         * 任务标识
         */
        private String key;
        /**
         * 轮询间隔（用于判断是否需要更新定时器）
         */
        private Integer pollInterval;

    }

    /**
     * 轮询定时器信息
     */
    @Data
    @AllArgsConstructor
    private static class PollTimerInfo {

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
     * 1. 【删除】任务：停止对应的轮询定时器
     * 2. 【新增】任务：创建对应的轮询定时器
     * 3. 【修改】任务：pollInterval 变化，重建对应的轮询定时器
     *    【修改】其他属性变化：不需要重建定时器（pollTask 运行时从 configCache 取最新配置）
     */
    public void updatePolling(IotModbusDeviceConfigRespDTO config) {
        Long deviceId = config.getDeviceId();
        List<PollTask> newTasks = buildPollTasks(config);
        Map<String, PollTimerInfo> currentTimers = devicePollTimers
                .computeIfAbsent(deviceId, k -> new ConcurrentHashMap<>());
        // 1.1 计算新配置中的任务 Key 集合
        Set<String> newTaskKeys = convertSet(newTasks, PollTask::getKey);
        // 1.2 计算删除的任务 Key 集合
        Set<String> removedTaskKeys = new HashSet<>(currentTimers.keySet());
        removedTaskKeys.removeAll(newTaskKeys);

        // 2. 处理删除的任务：停止不再存在的定时器
        for (String taskKey : removedTaskKeys) {
            PollTimerInfo timerInfo = currentTimers.remove(taskKey);
            if (timerInfo != null) {
                vertx.cancelTimer(timerInfo.getTimerId());
                log.debug("[updatePolling][设备 {} 轮询任务 {} 定时器已删除]", deviceId, taskKey);
            }
        }

        // 3. 处理新增和修改的任务
        if (CollUtil.isEmpty(newTasks)) {
            return;
        }
        for (PollTask task : newTasks) {
            String taskKey = task.getKey();
            Integer newPollInterval = task.getPollInterval();
            PollTimerInfo existingTimer = currentTimers.get(taskKey);
            // 3.1 新增任务：创建定时器
            if (existingTimer == null) {
                Long timerId = createPollTimer(deviceId, taskKey, newPollInterval);
                if (timerId != null) {
                    currentTimers.put(taskKey, new PollTimerInfo(timerId, newPollInterval));
                    log.debug("[updatePolling][设备 {} 轮询任务 {} 定时器已创建, interval={}ms]",
                            deviceId, taskKey, newPollInterval);
                }
            } else if (!Objects.equals(existingTimer.getPollInterval(), newPollInterval)) {
                // 3.2 pollInterval 变化：重建定时器
                vertx.cancelTimer(existingTimer.getTimerId());
                Long timerId = createPollTimer(deviceId, taskKey, newPollInterval);
                if (timerId != null) {
                    currentTimers.put(taskKey, new PollTimerInfo(timerId, newPollInterval));
                    log.debug("[updatePolling][设备 {} 轮询任务 {} 定时器已更新, interval={}ms -> {}ms]",
                            deviceId, taskKey, existingTimer.getPollInterval(), newPollInterval);
                } else {
                    currentTimers.remove(taskKey);
                }
            }
            // 3.3 其他属性变化：无需重建定时器，因为 pollTask() 运行时从 configCache 获取最新配置
        }
    }

    /**
     * 构建轮询任务列表
     *
     * 默认每个点位一个轮询任务。TCP Client 等协议可覆盖该方法，将多个点位合并为一个批量读取任务。
     */
    protected List<PollTask> buildPollTasks(IotModbusDeviceConfigRespDTO config) {
        return convertList(config.getPoints(), point -> new PollTask(String.valueOf(point.getId()), point.getPollInterval()));
    }

    /**
     * 创建轮询定时器
     */
    private Long createPollTimer(Long deviceId, String taskKey, Integer pollInterval) {
        if (pollInterval == null || pollInterval <= 0) {
            return null;
        }
        return vertx.setPeriodic(pollInterval, timerId -> {
            try {
                submitPollRequest(deviceId, taskKey);
            } catch (Exception e) {
                log.error("[createPollTimer][轮询任务失败, deviceId={}, taskKey={}]", deviceId, taskKey, e);
            }
        });
    }

    // ========== 请求队列（per-device 限速） ==========

    /**
     * 提交轮询请求到设备请求队列（保证同设备请求间隔）
     */
    private void submitPollRequest(Long deviceId, String taskKey) {
        // 1. 【重要】将请求添加到设备的请求队列
        Queue<Runnable> queue = deviceRequestQueues.computeIfAbsent(deviceId, k -> new ConcurrentLinkedQueue<>());
        while (queue.size() >= MAX_QUEUE_SIZE) {
            // 超出上限时，丢弃最旧的请求
            queue.poll();
            log.warn("[submitPollRequest][设备 {} 请求队列已满({}), 丢弃最旧请求]", deviceId, MAX_QUEUE_SIZE);
        }
        queue.offer(() -> pollTask(deviceId, taskKey));

        // 2. 处理设备请求队列（如果没有延迟 timer 在等待）
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

        // 不满足间隔要求，延迟执行
        long now = System.currentTimeMillis();
        long lastTime = deviceLastRequestTime.getOrDefault(deviceId, 0L);
        long elapsed = now - lastTime;
        if (elapsed < MIN_REQUEST_INTERVAL) {
            scheduleNextRequest(deviceId, MIN_REQUEST_INTERVAL - elapsed);
            return;
        }

        // 满足间隔要求，立即执行
        Runnable task = queue.poll();
        if (task == null) {
            return;
        }
        deviceLastRequestTime.put(deviceId, now);
        task.run();
        // 继续处理队列中的下一个（如果有的话，需要延迟）
        if (CollUtil.isNotEmpty(queue)) {
            scheduleNextRequest(deviceId);
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
                return;
            }

            // 满足间隔要求，立即执行
            Runnable task = queue.poll();
            if (task == null) {
                return;
            }
            deviceLastRequestTime.put(deviceId, System.currentTimeMillis());
            task.run();
            // 继续处理队列中的下一个（如果有的话，需要延迟）
            if (CollUtil.isNotEmpty(queue)) {
                scheduleNextRequest(deviceId);
            }
        });
    }

    // ========== 轮询执行 ==========

    /**
     * 轮询任务
     *
     * 默认将任务标识作为点位 ID，执行单点轮询。
     */
    protected void pollTask(Long deviceId, String taskKey) {
        pollPoint(deviceId, Long.valueOf(taskKey));
    }

    /**
     * 轮询单个点位（子类实现具体的读取逻辑）
     *
     * @param deviceId 设备 ID
     * @param pointId  点位 ID
     */
    protected abstract void pollPoint(Long deviceId, Long pointId);

    // ========== 停止 ==========

    /**
     * 停止设备的轮询
     */
    public void stopPolling(Long deviceId) {
        Map<String, PollTimerInfo> timers = devicePollTimers.remove(deviceId);
        if (CollUtil.isNotEmpty(timers)) {
            for (PollTimerInfo timerInfo : timers.values()) {
                vertx.cancelTimer(timerInfo.getTimerId());
            }
        }
        // 清理请求队列
        deviceRequestQueues.remove(deviceId);
        deviceLastRequestTime.remove(deviceId);
        deviceDelayTimerActive.remove(deviceId);
        log.debug("[stopPolling][设备 {} 停止了 {} 个轮询定时器]", deviceId,
                CollUtil.isEmpty(timers) ? 0 : timers.size());
    }

    /**
     * 停止所有轮询
     */
    public void stopAll() {
        for (Long deviceId : new ArrayList<>(devicePollTimers.keySet())) {
            stopPolling(deviceId);
        }
    }

}
