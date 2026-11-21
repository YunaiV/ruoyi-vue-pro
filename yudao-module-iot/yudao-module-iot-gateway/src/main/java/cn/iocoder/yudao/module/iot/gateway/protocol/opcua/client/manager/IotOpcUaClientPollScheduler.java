package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.upstream.IotOpcUaClientUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession.DeviceBinding;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * IoT OPC UA 轮询调度器：按设备、采集间隔分组，定时 Read
 * <p>
 * 1. 每个设备的每个采集间隔一个定时器；同一组上一次读取未完成时跳过本次（读取超时由 Milo 的 requestTimeout 保证）；
 * 2. 按 maxNodesPerRead 分批读取，全部完成后合并为一条（超限时拆分）属性上报；任一批失败、
 *    或返回的结果数与请求的 NodeId 数不一致（无法按位置对应到点位）时整轮丢弃；
 * 3. 结果回到 context 后校验会话代数、设备配置版本，不一致即丢弃（断线、重连、点位变化后的过期结果）。
 * <p>
 * 只在协议的 context 线程上调用
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientPollScheduler {

    private final Vertx vertx;
    private final Context context;
    private final IotOpcUaClientUpstreamHandler upstreamHandler;

    /**
     * 设备的轮询：deviceId -> 轮询信息
     */
    private final Map<Long, DevicePoll> devicePolls = new HashMap<>();

    public IotOpcUaClientPollScheduler(Vertx vertx, Context context, IotOpcUaClientUpstreamHandler upstreamHandler) {
        this.vertx = vertx;
        this.context = context;
        this.upstreamHandler = upstreamHandler;
    }

    /**
     * 更新设备的轮询定时器（增量）：采集间隔没有变化的定时器保留，读取时取最新的点位
     *
     * @param session 设备所在的会话
     * @param binding 设备绑定
     */
    public void updateDevice(IotOpcUaClientSession session, DeviceBinding binding) {
        Long deviceId = binding.getConfig().getDeviceId();
        DevicePoll poll = devicePolls.get(deviceId);
        // 1. 设备换了会话：取消原有定时器
        if (poll != null && poll.session != session) {
            removeDevice(deviceId);
            poll = null;
        }
        Set<Integer> intervals = getPollIntervals(binding.getConfig());
        if (intervals.isEmpty()) {
            if (poll != null) {
                removeDevice(deviceId);
            }
            return;
        }
        if (poll == null) {
            poll = new DevicePoll(session);
            devicePolls.put(deviceId, poll);
        }
        // 2.1 删除不再存在的采集间隔
        for (Integer interval : new ArrayList<>(poll.timerIds.keySet())) {
            if (!intervals.contains(interval)) {
                vertx.cancelTimer(poll.timerIds.remove(interval));
                poll.inFlight.remove(interval);
            }
        }
        // 2.2 新增采集间隔
        for (Integer interval : intervals) {
            if (poll.timerIds.containsKey(interval)) {
                continue;
            }
            DevicePoll target = poll;
            long timerId = vertx.setPeriodic(interval, id -> context.runOnContext(v -> poll(deviceId, target, interval)));
            poll.timerIds.put(interval, timerId);
            log.debug("[updateDevice][设备 {} 轮询定时器已创建, interval={}ms]", deviceId, interval);
        }
    }

    /**
     * 移除设备的轮询定时器
     *
     * @param deviceId 设备 ID
     */
    public void removeDevice(Long deviceId) {
        DevicePoll poll = devicePolls.remove(deviceId);
        if (poll == null) {
            return;
        }
        poll.timerIds.values().forEach(vertx::cancelTimer);
        poll.timerIds.clear();
        poll.inFlight.clear();
    }

    /**
     * 停止所有轮询
     */
    public void stopAll() {
        for (Long deviceId : new ArrayList<>(devicePolls.keySet())) {
            removeDevice(deviceId);
        }
    }

    /**
     * 执行一次轮询
     */
    void poll(Long deviceId, DevicePoll poll, Integer interval) {
        // 1.1 校验：定时器仍有效、会话已激活、OperationLimits 已读取
        if (devicePolls.get(deviceId) != poll || !poll.timerIds.containsKey(interval)) {
            return;
        }
        IotOpcUaClientSession session = poll.session;
        DeviceBinding binding = session.getDevices().get(deviceId);
        OpcUaClient client = session.getClient();
        Integer maxNodesPerRead = session.getMaxNodesPerRead();
        if (binding == null || client == null || !session.isActive() || maxNodesPerRead == null) {
            return;
        }
        // 1.2 上一次读取未完成（同一代）：跳过本次
        long generation = session.getGeneration();
        InFlight previous = poll.inFlight.get(interval);
        if (previous != null && previous.generation == generation) {
            log.debug("[poll][设备 {} interval={}ms 上一次读取未完成，跳过]", deviceId, interval);
            return;
        }
        // 1.3 收集本组点位
        List<IotOpcUaPointRespDTO> points = new ArrayList<>();
        List<NodeId> nodeIds = new ArrayList<>();
        for (IotOpcUaPointRespDTO point : CollUtil.emptyIfNull(binding.getConfig().getPoints())) {
            NodeId nodeId = binding.getResolvedNodeIds().get(point.getId());
            if (nodeId != null && isPoll(point) && Objects.equals(point.getCollectInterval(), interval)) {
                points.add(point);
                nodeIds.add(nodeId);
            }
        }
        if (nodeIds.isEmpty()) {
            return;
        }

        // 2. 分批读取
        InFlight inFlight = new InFlight(generation, binding.getEpoch());
        poll.inFlight.put(interval, inFlight);
        List<List<NodeId>> chunks = CollUtil.split(nodeIds, maxNodesPerRead);
        List<CompletableFuture<List<DataValue>>> futures = new ArrayList<>(chunks.size());
        try {
            for (List<NodeId> chunk : chunks) {
                futures.add(client.readValuesAsync(0, TimestampsToReturn.Neither, chunk));
            }
        } catch (Exception e) {
            // 同步抛出（例如客户端已断开）：释放在途标记，下一次定时再读；已提交的批次结果不再使用
            poll.inFlight.remove(interval, inFlight);
            if (session.warnOnce("read:" + deviceId + ":" + interval)) {
                log.warn("[poll][设备 {} interval={}ms 提交读取失败]", deviceId, interval, e);
            }
            return;
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((ignored, ex) ->
                context.runOnContext(v -> handleReadResult(deviceId, poll, interval, inFlight, points, chunks, futures, ex)));
    }

    /**
     * 处理一次轮询的读取结果（在 Vert.x context 上执行）：释放在途标记，丢弃过期、失败或结果数不一致的结果，
     * 合并各批的属性值后提交上报
     *
     * @param deviceId 设备 ID
     * @param poll     发起读取时设备的轮询信息（已被替换时，结果过期）
     * @param interval 采集间隔（毫秒）
     * @param inFlight 本次读取的在途标记
     * @param points   本次读取的点位，与各批 NodeId 拼接后的顺序一致
     * @param chunks   分批读取的 NodeId
     * @param futures  每一批的读取结果，与 chunks 一一对应
     * @param ex       读取失败的异常；成功时为 null
     */
    void handleReadResult(Long deviceId, DevicePoll poll, Integer interval, InFlight inFlight,
                          List<IotOpcUaPointRespDTO> points, List<List<NodeId>> chunks,
                          List<CompletableFuture<List<DataValue>>> futures, Throwable ex) {
        // 1.1 释放在途标记（只释放自己的）
        poll.inFlight.remove(interval, inFlight);
        // 1.2 过期结果：会话代数、设备配置版本变化，或设备已移除
        IotOpcUaClientSession session = poll.session;
        DeviceBinding binding = session.getDevices().get(deviceId);
        if (devicePolls.get(deviceId) != poll || binding == null || !session.isActive()
                || session.getGeneration() != inFlight.generation || binding.getEpoch() != inFlight.epoch) {
            log.debug("[handleReadResult][设备 {} interval={}ms 的读取结果已过期，丢弃]", deviceId, interval);
            return;
        }
        // 1.3 读取失败
        if (ex != null) {
            if (session.warnOnce("read:" + deviceId + ":" + interval)) {
                log.warn("[handleReadResult][设备 {} interval={}ms 读取失败]", deviceId, interval, ex);
            }
            return;
        }

        // 1.4 每一批的结果数必须与请求的 NodeId 数一致，否则无法按位置对应到点位：整轮丢弃
        List<DataValue> values = new ArrayList<>(points.size());
        for (int i = 0; i < futures.size(); i++) {
            List<DataValue> chunkValues = futures.get(i).join();
            int expected = chunks.get(i).size();
            if (chunkValues == null || chunkValues.size() != expected) {
                if (session.warnOnce("read-size:" + deviceId + ":" + interval)) {
                    log.warn("[handleReadResult][设备 {} interval={}ms 第 {} 批读取结果数 {} 与请求数 {} 不一致，整轮丢弃]",
                            deviceId, interval, i + 1, chunkValues != null ? chunkValues.size() : null, expected);
                }
                return;
            }
            values.addAll(chunkValues);
        }

        // 2. 合并本次读取的属性值
        Map<String, Object> properties = new LinkedHashMap<>();
        for (int i = 0; i < points.size(); i++) {
            upstreamHandler.collectValue(session, deviceId, points.get(i).getIdentifier(), values.get(i), properties);
        }
        if (properties.isEmpty()) {
            return;
        }

        // 3. 上报：提交到会话的 worker 队列
        IotOpcUaDeviceConfigRespDTO config = binding.getConfig();
        session.getUpstreamQueue().submit(() -> {
            upstreamHandler.handlePropertyPost(config, properties);
            return null;
        }).onFailure(e -> log.warn("[handleReadResult][设备 {} 属性上报无法提交，丢弃]", deviceId, e));
    }

    private static Set<Integer> getPollIntervals(IotOpcUaDeviceConfigRespDTO config) {
        Set<Integer> intervals = new HashSet<>();
        for (IotOpcUaPointRespDTO point : CollUtil.emptyIfNull(config.getPoints())) {
            if (isPoll(point) && point.getCollectInterval() != null && point.getCollectInterval() > 0) {
                intervals.add(point.getCollectInterval());
            }
        }
        return intervals;
    }

    private static boolean isPoll(IotOpcUaPointRespDTO point) {
        return IotOpcUaCollectModeEnum.POLL.getMode().equals(point.getCollectMode());
    }

    /**
     * 设备的轮询信息
     */
    static class DevicePoll {

        private final IotOpcUaClientSession session;
        /**
         * 采集间隔 -> 定时器 ID
         */
        private final Map<Integer, Long> timerIds = new HashMap<>();
        /**
         * 采集间隔 -> 在途读取
         */
        private final Map<Integer, InFlight> inFlight = new HashMap<>();

        DevicePoll(IotOpcUaClientSession session) {
            this.session = session;
        }

    }

    /**
     * 在途读取：发起时的会话代数、设备配置版本（按对象身份区分每次读取）
     */
    @AllArgsConstructor
    static class InFlight {

        private final long generation;
        private final long epoch;

    }

}
