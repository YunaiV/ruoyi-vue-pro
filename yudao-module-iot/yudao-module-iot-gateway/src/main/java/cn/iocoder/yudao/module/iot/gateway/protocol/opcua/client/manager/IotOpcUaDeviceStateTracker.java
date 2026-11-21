package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * IoT OPC UA 设备上下线状态跟踪器
 * <p>
 * 按设备记录期望状态（会话激活 → 在线；会话失效、设备移除 → 离线）和已上报状态，不一致时发送状态消息：
 * 1. 状态消息不走离线暂存，发送失败时按 stateRetryInterval 重试，直到成功或期望状态再次变化；
 * 2. 已移除设备的离线消息发送成功后，不再跟踪该设备；
 * 3. 协议停止时不逐个发送离线消息（与 Modbus TCP Client 一致），已排队、尚未执行的状态消息也不再发送。
 * <p>
 * 只在协议的 context 线程上调用
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaDeviceStateTracker {

    private final Vertx vertx;
    private final IotDeviceMessageService messageService;
    private final String serverId;
    private final long retryIntervalMillis;
    /**
     * 发送状态消息的有序 worker 队列
     */
    private final IotWorkerTaskQueue sendQueue;

    /**
     * 设备状态：deviceId -> 状态
     */
    private final Map<Long, DeviceState> states = new HashMap<>();

    private Long retryTimerId;
    /**
     * 是否已关闭：worker 线程上执行排队的发送任务前也会检查，所以使用 volatile
     */
    private volatile boolean closed;

    public IotOpcUaDeviceStateTracker(Vertx vertx, Context context, IotDeviceMessageService messageService,
                                      String serverId, long retryIntervalMillis) {
        this.vertx = vertx;
        this.messageService = messageService;
        this.serverId = serverId;
        this.retryIntervalMillis = retryIntervalMillis;
        this.sendQueue = new IotWorkerTaskQueue(context);
    }

    /**
     * 设置设备期望在线
     *
     * @param config 设备配置
     */
    public void online(IotOpcUaDeviceConfigRespDTO config) {
        update(config, true, false);
    }

    /**
     * 设置设备期望离线
     *
     * @param config 设备配置
     */
    public void offline(IotOpcUaDeviceConfigRespDTO config) {
        update(config, false, false);
    }

    /**
     * 设备已移除：期望离线，离线消息发送成功后不再跟踪
     *
     * @param config 设备配置（最后一次的配置）
     */
    public void remove(IotOpcUaDeviceConfigRespDTO config) {
        update(config, false, true);
    }

    private void update(IotOpcUaDeviceConfigRespDTO config, boolean online, boolean removed) {
        if (closed || config == null) {
            return;
        }
        // 移除从未跟踪过的设备（例如配置一直不可用）：不发送
        if (removed && !states.containsKey(config.getDeviceId())) {
            return;
        }
        DeviceState state = states.computeIfAbsent(config.getDeviceId(), id -> new DeviceState());
        state.config = config;
        state.desired = online;
        state.removed = removed;
        trySend(config.getDeviceId(), state);
    }

    private void trySend(Long deviceId, DeviceState state) {
        // 1. 已经一致，或正在发送（发送完成后再比较一次）
        if (state.sending) {
            return;
        }
        if (Objects.equals(state.reported, state.desired)) {
            state.failed = false;
            if (state.removed) {
                states.remove(deviceId);
            }
            return;
        }
        // 2. 提交到 worker 发送
        boolean online = state.desired;
        IotOpcUaDeviceConfigRespDTO config = state.config;
        state.sending = true;
        sendQueue.submit(() -> {
            // 协议已停止：排队中的状态消息不再发送
            if (closed) {
                return null;
            }
            IotDeviceMessage message = online ? IotDeviceMessage.buildStateUpdateOnline()
                    : IotDeviceMessage.buildStateOffline();
            // 不带协议：不走离线暂存，失败时抛出，由本类重试
            messageService.sendDeviceMessage(message, config.getProductKey(), config.getDeviceName(), serverId);
            return null;
        }).onComplete(ar -> {
            state.sending = false;
            if (closed || states.get(deviceId) != state) {
                return;
            }
            if (ar.succeeded()) {
                state.reported = online;
                state.failed = false;
                log.info("[trySend][设备 {} 已{}]", deviceId, online ? "上线" : "下线");
            } else {
                state.failed = true;
                log.warn("[trySend][设备 {} {}消息发送失败，{} ms 后重试]", deviceId, online ? "上线" : "下线",
                        retryIntervalMillis, ar.cause());
                scheduleRetry();
                return;
            }
            // 发送期间期望状态又变化了：继续发送
            trySend(deviceId, state);
        });
    }

    private void scheduleRetry() {
        if (retryTimerId != null || closed) {
            return;
        }
        retryTimerId = vertx.setTimer(retryIntervalMillis, id -> {
            retryTimerId = null;
            if (closed) {
                return;
            }
            for (Map.Entry<Long, DeviceState> entry : new ArrayList<>(states.entrySet())) {
                if (entry.getValue().failed) {
                    trySend(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    /**
     * 获取已上报在线的设备编号（测试、诊断用）
     *
     * @return 设备编号
     */
    public List<Long> getOnlineDeviceIds() {
        List<Long> result = new ArrayList<>();
        states.forEach((deviceId, state) -> {
            if (Boolean.TRUE.equals(state.reported)) {
                result.add(deviceId);
            }
        });
        return result;
    }

    /**
     * 关闭：取消重试，不再发送（包括已排队、尚未执行的状态消息）
     */
    public void close() {
        closed = true;
        if (retryTimerId != null) {
            vertx.cancelTimer(retryTimerId);
            retryTimerId = null;
        }
        states.clear();
    }

    private static class DeviceState {

        private IotOpcUaDeviceConfigRespDTO config;
        /**
         * 期望状态：true 在线，false 离线
         */
        private boolean desired;
        /**
         * 已上报状态：null 表示还没有上报过
         */
        private Boolean reported;
        /**
         * 是否已移除：离线消息发送成功后不再跟踪
         */
        private boolean removed;
        private boolean sending;
        private boolean failed;

    }

}
