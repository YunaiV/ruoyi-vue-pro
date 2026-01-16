package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO @AI：注释可以简化？
/**
 * IoT Modbus TCP 轮询调度器
 *
 * 负责：
 * 1. 管理每个点位的轮询定时器
 * 2. 调度 Modbus 读取任务
 * 3. 处理读取结果并上报
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
     * 设备的定时器列表：deviceId -> timerId 列表
     */
    private final Map<Long, List<Long>> deviceTimers = new ConcurrentHashMap<>();

    /**
     * 更新轮询任务
     */
    public void updatePolling(IotModbusDeviceConfigRespDTO config) {
        Long deviceId = config.getDeviceId();

        // 1. 停止旧的轮询任务
        stopPolling(deviceId);

        // 2.1 为每个点位创建新的轮询任务
        List<Long> timerIds = new ArrayList<>();
        // TODO @AI：if return 简化；上面的 size 加下；config.getPoints()
        if (config.getPoints() != null) {
            for (IotModbusPointRespDTO point : config.getPoints()) {
                Long timerId = createPollTimer(config, point);
                if (timerId != null) {
                    timerIds.add(timerId);
                }
            }
        }
        // 2.2 记录定时器
        // TODO @AI：CollUtil.isNotEmpty；if return 简化；
        if (!timerIds.isEmpty()) {
            deviceTimers.put(deviceId, timerIds);
            log.debug("[updatePolling][设备 {} 创建了 {} 个轮询定时器]", deviceId, timerIds.size());
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
        // TODO @AI：超时时间，有实现么？？？
        modbusClient.read(connection, slaveId, point)
                .onSuccess(rawValue -> upstreamHandler.handleReadResult(config, point, rawValue))
                .onFailure(e -> log.error("[pollPoint][读取点位失败, deviceId={}, identifier={}]",
                        config.getDeviceId(), point.getIdentifier(), e));
    }

    /**
     * 停止设备的轮询
     */
    public void stopPolling(Long deviceId) {
        List<Long> timerIds = deviceTimers.remove(deviceId);
        // TODO @AI：CollUtil.isNotEmpty；并且 if return；
        if (timerIds != null) {
            for (Long timerId : timerIds) {
                vertx.cancelTimer(timerId);
            }
            log.debug("[stopPolling][设备 {} 停止了 {} 个轮询定时器]", deviceId, timerIds.size());
        }
    }

    /**
     * 停止所有轮询
     */
    public void stopAll() {
        for (Long deviceId : deviceTimers.keySet()) {
            stopPolling(deviceId);
        }
    }

}
