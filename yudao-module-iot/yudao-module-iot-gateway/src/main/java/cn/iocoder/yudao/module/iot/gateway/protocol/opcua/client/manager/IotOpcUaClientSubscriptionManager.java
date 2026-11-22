package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.upstream.IotOpcUaClientUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession.DeviceBinding;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.MonitoredItemServiceOperationResult;
import org.eclipse.milo.opcua.sdk.client.subscriptions.MonitoredItemSynchronizationException;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaSubscription;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.DataChangeTrigger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.DeadbandType;
import org.eclipse.milo.opcua.stack.core.types.structured.DataChangeFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

/**
 * IoT OPC UA 订阅管理器：MonitoredItem 订阅采集
 * <p>
 * 1. 每个会话的每个采集间隔一个订阅组（一个 Subscription，发布间隔 = 采集间隔），
 *    MonitoredItem 的采样间隔 = 采集间隔，队列 1、丢弃最旧，DataChangeFilter(StatusValue, 无死区)；
 * 2. 断线重连由 Milo 负责：Session 恢复后 Subscription 转移成功则继续使用；转移失败（onTransferFailed）、
 *    看门狗超时（onWatchdogTimerElapsed）、状态变为 Bad、命名空间表变化时重建整个订阅组；
 * 3. 点位变化时按差异增删 MonitoredItem，只影响变化的点位；
 * 4. 一次通知中同一设备的属性合并为一条（超限时拆分）属性上报；回调时的会话代数与处理时不一致（期间断线、重连）、
 *    会话未激活、订阅组已重建、点位已移除或变化时丢弃旧通知；
 * 5. 同步 MonitoredItem 时服务级失败（超时、Session / Subscription 失效等）按订阅级失败处理：重建订阅组并按 retryInterval 重试；
 *    只有单个 MonitoredItem 的操作失败（例如 NodeId 不存在）只告警；旧代（期间断线、重连过）的失败不影响新代的订阅组；
 * 6. 每次调用的 MonitoredItem 数量上限取会话本次激活的 maxMonitoredItemsPerCall（本地配置与服务端 OperationLimits 的较小值），
 *    设置到 Subscription 上，由 Milo 按该值分批；会话读取 OperationLimits 完成前不同步订阅。
 * <p>
 * 除 Milo 的回调外只在协议的 context 线程上调用；Milo 的回调切回 context 后再处理；
 * 订阅的创建、同步、删除会阻塞，提交到会话的 serviceQueue 上按顺序执行，每个订阅组同一时间只有一个在途操作
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientSubscriptionManager {

    private final Vertx vertx;
    private final Context context;
    private final IotOpcUaClientConfig config;
    private final IotOpcUaClientUpstreamHandler upstreamHandler;

    public IotOpcUaClientSubscriptionManager(Vertx vertx, Context context, IotOpcUaClientConfig config,
                                             IotOpcUaClientUpstreamHandler upstreamHandler) {
        this.vertx = vertx;
        this.context = context;
        this.config = config;
        this.upstreamHandler = upstreamHandler;
    }

    // ========== 会话事件 ==========

    /**
     * 会话激活（OperationLimits 读取完成后调用）：转移失败（已被 Milo 重置）的订阅组、命名空间表变化时的所有订阅组重建；
     * 转移成功的订阅组应用本次激活的 MonitoredItem 上限，再按差异同步
     *
     * @param session          会话
     * @param namespaceChanged 命名空间表是否变化
     */
    public void onSessionActive(IotOpcUaClientSession session, boolean namespaceChanged) {
        for (SubscriptionGroup group : session.getSubscriptionGroups().values()) {
            if (namespaceChanged || isSubscriptionGone(group.subscription)) {
                group.rebuild = true;
            } else {
                // Milo 按 Subscription 缓存分批大小：重新设置后，下次同步按本次激活的上限重新计算
                group.subscription.setMaxMonitoredItemsPerCall(uint(session.getMaxMonitoredItemsPerCall()));
            }
        }
        reconcileAll(session);
    }

    /**
     * 设备的点位变化（包括新增、移除设备）：会话激活时按差异同步；未激活时等激活后再同步
     *
     * @param session 会话
     */
    public void onDeviceChanged(IotOpcUaClientSession session) {
        if (session.isActive()) {
            reconcileAll(session);
        }
    }

    /**
     * 会话关闭：作废所有订阅组（服务端的 Subscription 随 Session 关闭删除）
     *
     * @param session 会话
     */
    public void closeSession(IotOpcUaClientSession session) {
        for (SubscriptionGroup group : session.getSubscriptionGroups().values()) {
            group.items.values().forEach(item -> item.active = false);
            group.items.clear();
            group.subscription = null;
            cancelRetry(group);
        }
        session.getSubscriptionGroups().clear();
    }

    // ========== 同步 ==========

    private void reconcileAll(IotOpcUaClientSession session) {
        Set<Integer> intervals = new HashSet<>(session.getSubscriptionGroups().keySet());
        for (DeviceBinding binding : session.getDevices().values()) {
            for (IotOpcUaPointRespDTO point : CollUtil.emptyIfNull(binding.getConfig().getPoints())) {
                if (isSubscribe(point) && point.getCollectInterval() != null && point.getCollectInterval() > 0) {
                    intervals.add(point.getCollectInterval());
                }
            }
        }
        for (Integer interval : intervals) {
            SubscriptionGroup group = session.getSubscriptionGroups()
                    .computeIfAbsent(interval, SubscriptionGroup::new);
            reconcile(session, group);
        }
    }

    /**
     * 同步一个订阅组：计算期望的 MonitoredItem，与当前的比较后提交到 serviceQueue
     */
    void reconcile(IotOpcUaClientSession session, SubscriptionGroup group) {
        // 1.1 有在途操作：完成后再同步一次
        if (group.busy) {
            group.dirty = true;
            return;
        }
        // 1.2 会话未激活、OperationLimits 未读取完成（完成后 onSessionActive 统一同步）：跳过
        OpcUaClient client = session.getClient();
        if (!session.isActive() || session.isClosed() || client == null || !isCurrent(session, group)
                || session.getMaxMonitoredItemsPerCall() == null) {
            return;
        }
        // 1.3 期望的 MonitoredItem：pointId -> 规格
        Map<Long, ItemBinding> desired = buildDesiredItems(session, group.interval);

        // 2. 情况一：没有订阅点位，删除订阅组（取消重试定时器）
        if (desired.isEmpty()) {
            OpcUaSubscription old = group.subscription;
            deactivateAll(group);
            cancelRetry(group);
            session.getSubscriptionGroups().remove(group.interval);
            if (old != null && !isSubscriptionGone(old)) {
                submitDelete(session, old);
            }
            return;
        }

        // 3. 情况二：重建（首次创建、转移失败、看门狗超时、命名空间变化）
        if (group.rebuild || group.subscription == null || isSubscriptionGone(group.subscription)) {
            OpcUaSubscription old = group.subscription;
            deactivateAll(group);
            OpcUaSubscription subscription = newSubscription(session, group, client);
            List<OpcUaMonitoredItem> items = new ArrayList<>();
            desired.forEach((pointId, item) -> {
                item.monitoredItem = newMonitoredItem(item, group.interval);
                items.add(item.monitoredItem);
                group.items.put(pointId, item);
            });
            group.subscription = subscription;
            group.rebuild = false;
            log.info("[reconcile][会话 {} 创建订阅组 interval={}ms，MonitoredItem {} 个]", session.getKey(),
                    group.interval, items.size());
            execute(session, group, () -> {
                if (old != null) {
                    deleteQuietly(old);
                }
                subscription.create();
                subscription.addMonitoredItems(items);
                return synchronize(subscription);
            });
            return;
        }

        // 4. 情况三：按差异增删 MonitoredItem
        List<OpcUaMonitoredItem> removed = new ArrayList<>();
        List<OpcUaMonitoredItem> added = new ArrayList<>();
        for (Long pointId : new ArrayList<>(group.items.keySet())) {
            ItemBinding current = group.items.get(pointId);
            ItemBinding target = desired.get(pointId);
            if (target != null && current.sameAs(target)) {
                // 点位未变化（设备的其它点位变化）：更新配置版本，继续接收通知
                current.epoch = target.epoch;
                continue;
            }
            current.active = false;
            group.items.remove(pointId);
            removed.add(current.monitoredItem);
        }
        desired.forEach((pointId, item) -> {
            if (group.items.containsKey(pointId)) {
                return;
            }
            item.monitoredItem = newMonitoredItem(item, group.interval);
            added.add(item.monitoredItem);
            group.items.put(pointId, item);
        });
        if (removed.isEmpty() && added.isEmpty()) {
            return;
        }
        OpcUaSubscription subscription = group.subscription;
        log.debug("[reconcile][会话 {} 订阅组 interval={}ms 删除 {} 个、新增 {} 个 MonitoredItem]", session.getKey(),
                group.interval, removed.size(), added.size());
        execute(session, group, () -> {
            subscription.removeMonitoredItems(removed);
            subscription.addMonitoredItems(added);
            return synchronize(subscription);
        });
    }

    private Map<Long, ItemBinding> buildDesiredItems(IotOpcUaClientSession session, int interval) {
        Map<Long, ItemBinding> desired = new LinkedHashMap<>();
        for (DeviceBinding binding : session.getDevices().values()) {
            IotOpcUaDeviceConfigRespDTO deviceConfig = binding.getConfig();
            for (IotOpcUaPointRespDTO point : CollUtil.emptyIfNull(deviceConfig.getPoints())) {
                NodeId nodeId = binding.getResolvedNodeIds().get(point.getId());
                if (nodeId == null || !isSubscribe(point) || !Objects.equals(point.getCollectInterval(), interval)) {
                    continue;
                }
                desired.put(point.getId(), new ItemBinding(deviceConfig.getDeviceId(), point.getIdentifier(),
                        nodeId, binding.getEpoch()));
            }
        }
        return desired;
    }

    /**
     * 在 serviceQueue 上执行订阅操作；完成后回到 context：失败时按 retryInterval 重建，有新的变化时再同步一次
     * <p>
     * 失败、告警只处理提交时的会话代数：期间断线、重连过（新代由 onSessionActive 检查并重建）时忽略旧代的结果
     */
    void execute(IotOpcUaClientSession session, SubscriptionGroup group, Callable<List<String>> task) {
        group.busy = true;
        long generation = session.getGeneration();
        session.getServiceQueue().submit(task).onComplete(ar -> {
            group.busy = false;
            if (!isCurrent(session, group)) {
                return;
            }
            if (ar.failed() && session.getGeneration() != generation) {
                // 旧代的失败（期间断线、重连过）：不给新代的订阅组标记重建、安排重试
                log.debug("[execute][会话 {} 订阅组 interval={}ms 旧代的同步失败，忽略]", session.getKey(),
                        group.interval, ar.cause());
            } else if (ar.failed()) {
                // 订阅级失败（创建 Subscription 失败、同步 MonitoredItem 服务级失败）：重建
                group.rebuild = true;
                log.warn("[execute][会话 {} 订阅组 interval={}ms 同步失败，稍后重建]", session.getKey(),
                        group.interval, ar.cause());
                scheduleRetry(session, group);
            } else if (CollUtil.isNotEmpty(ar.result()) && session.getGeneration() == generation) {
                // MonitoredItem 级失败（例如 NodeId 不存在）：只告警，不重试
                for (String failure : ar.result()) {
                    if (session.warnOnce("monitored-item:" + failure)) {
                        log.warn("[execute][会话 {} 订阅组 interval={}ms MonitoredItem 创建失败: {}]",
                                session.getKey(), group.interval, failure);
                    }
                }
            }
            if (group.dirty) {
                group.dirty = false;
                reconcile(session, group);
            }
        });
    }

    private void cancelRetry(SubscriptionGroup group) {
        if (group.retryTimerId != null) {
            vertx.cancelTimer(group.retryTimerId);
            group.retryTimerId = null;
        }
    }

    private void scheduleRetry(IotOpcUaClientSession session, SubscriptionGroup group) {
        if (group.retryTimerId != null) {
            return;
        }
        IotOpcUaDeviceConfigRespDTO deviceConfig = session.getAnyDeviceConfig();
        long delay = deviceConfig != null ? ObjUtil.defaultIfNull(deviceConfig.getRetryInterval(), 10000) : 10000;
        group.retryTimerId = vertx.setTimer(delay, id -> {
            group.retryTimerId = null;
            reconcile(session, group);
        });
    }

    private void submitDelete(IotOpcUaClientSession session, OpcUaSubscription subscription) {
        session.getServiceQueue().submit(() -> {
            deleteQuietly(subscription);
            return null;
        });
    }

    private static void deleteQuietly(OpcUaSubscription subscription) {
        try {
            subscription.delete();
        } catch (Exception e) {
            // 旧订阅可能已随 Session 失效，删除失败时只重置本地状态
            subscription.reset();
            log.debug("[deleteQuietly][删除旧订阅失败]", e);
        }
    }

    /**
     * 同步 MonitoredItem
     * <p>
     * Milo 把 Create / Modify / DeleteMonitoredItems 的服务级失败（超时、Session / Subscription 失效、通信失败等）
     * 也包在 {@link MonitoredItemSynchronizationException} 中：此时服务端的 MonitoredItem 与本地记录不一致，
     * 原样抛出，由调用方按订阅级失败重建；只有单个 MonitoredItem 的操作失败时返回失败描述
     *
     * @param subscription 订阅
     * @return MonitoredItem 级失败的描述
     * @throws MonitoredItemSynchronizationException 有服务级失败
     */
    static List<String> synchronize(OpcUaSubscription subscription) throws MonitoredItemSynchronizationException {
        try {
            subscription.synchronizeMonitoredItems();
            return Collections.emptyList();
        } catch (MonitoredItemSynchronizationException e) {
            boolean serviceFailed = Stream.of(e.getCreateResults(), e.getModifyResults(), e.getDeleteResults())
                    .flatMap(List::stream)
                    .anyMatch(r -> !r.serviceResult().isGood());
            if (serviceFailed) {
                throw e;
            }
            List<String> failures = new ArrayList<>();
            Stream.of(e.getCreateResults(), e.getModifyResults(), e.getDeleteResults())
                    .flatMap(List::stream)
                    .filter(r -> !r.operationResult().map(StatusCode::isGood).orElse(false))
                    .forEach(r -> failures.add(describe(r)));
            return failures;
        }
    }

    private static String describe(MonitoredItemServiceOperationResult result) {
        Object userObject = result.monitoredItem().getUserObject().orElse(null);
        String target;
        if (userObject instanceof ItemBinding) {
            ItemBinding item = (ItemBinding) userObject;
            target = item.deviceId + "/" + item.identifier + "(" + item.nodeId.toParseableString() + ")";
        } else {
            target = String.valueOf(result.monitoredItem().getReadValueId().getNodeId());
        }
        return target + " " + result.operationResult().orElse(null);
    }

    // ========== Subscription / MonitoredItem ==========

    private OpcUaSubscription newSubscription(IotOpcUaClientSession session, SubscriptionGroup group, OpcUaClient client) {
        int keepAliveCount = config.getSubscriptionKeepAliveCount();
        OpcUaSubscription subscription = new OpcUaSubscription(client, group.interval);
        subscription.setMaxKeepAliveCount(uint(keepAliveCount));
        subscription.setLifetimeCount(uint(Math.max(config.getSubscriptionLifetimeCount(), keepAliveCount * 3)));
        subscription.setMaxMonitoredItemsPerCall(uint(session.getMaxMonitoredItemsPerCall()));
        // 回调在 Milo 的线程上：先记下当时的会话代数，切回 context 后不一致即丢弃（期间断线、重连过）；
        // 丢弃的重建请求不影响恢复：会话重新激活时会检查 Subscription 是否已失效
        subscription.setSubscriptionListener(new OpcUaSubscription.SubscriptionListener() {

            @Override
            public void onDataReceived(OpcUaSubscription s, List<OpcUaMonitoredItem> items, List<DataValue> values) {
                long generation = session.getGeneration();
                context.runOnContext(v -> handleData(session, generation, group, s, items, values));
            }

            @Override
            public void onTransferFailed(OpcUaSubscription s, StatusCode status) {
                long generation = session.getGeneration();
                context.runOnContext(v -> requestRebuild(session, generation, group, s, "Subscription 转移失败 " + status));
            }

            @Override
            public void onWatchdogTimerElapsed(OpcUaSubscription s) {
                long generation = session.getGeneration();
                context.runOnContext(v -> requestRebuild(session, generation, group, s, "Subscription 看门狗超时"));
            }

            @Override
            public void onStatusChanged(OpcUaSubscription s, StatusCode status) {
                if (status != null && status.isBad()) {
                    long generation = session.getGeneration();
                    context.runOnContext(v -> requestRebuild(session, generation, group, s, "Subscription 状态 " + status));
                }
            }

        });
        return subscription;
    }

    private static OpcUaMonitoredItem newMonitoredItem(ItemBinding item, int interval) {
        OpcUaMonitoredItem monitoredItem = OpcUaMonitoredItem.newDataItem(item.nodeId);
        monitoredItem.setSamplingInterval(interval);
        monitoredItem.setQueueSize(uint(1));
        monitoredItem.setDiscardOldest(true);
        monitoredItem.setFilter(new DataChangeFilter(DataChangeTrigger.StatusValue,
                uint(DeadbandType.None.getValue()), 0.0));
        monitoredItem.setUserObject(item);
        return monitoredItem;
    }

    void requestRebuild(IotOpcUaClientSession session, long generation, SubscriptionGroup group,
                        OpcUaSubscription subscription, String reason) {
        if (session.getGeneration() != generation || !isCurrent(session, group) || group.subscription != subscription) {
            log.debug("[requestRebuild][会话 {} 订阅组 interval={}ms 的重建请求已过期，忽略: {}]", session.getKey(),
                    group.interval, reason);
            return;
        }
        log.warn("[requestRebuild][会话 {} 订阅组 interval={}ms 需要重建: {}]", session.getKey(), group.interval, reason);
        group.rebuild = true;
        reconcile(session, group);
    }

    /**
     * 处理订阅通知：丢弃旧通知，按设备合并后上报
     */
    void handleData(IotOpcUaClientSession session, long generation, SubscriptionGroup group,
                    OpcUaSubscription subscription, List<OpcUaMonitoredItem> items, List<DataValue> values) {
        // 1. 会话代数变化（回调后断线、重连过）、会话未激活、订阅组已重建：整批丢弃
        if (session.getGeneration() != generation || !session.isActive() || !isCurrent(session, group)
                || group.subscription != subscription) {
            log.debug("[handleData][会话 {} 订阅组 interval={}ms 的通知已过期，丢弃]", session.getKey(), group.interval);
            return;
        }
        // 2. 按设备合并
        Map<Long, Map<String, Object>> deviceProperties = new LinkedHashMap<>();
        for (int i = 0; i < items.size() && i < values.size(); i++) {
            Object userObject = items.get(i).getUserObject().orElse(null);
            if (!(userObject instanceof ItemBinding)) {
                continue;
            }
            ItemBinding item = (ItemBinding) userObject;
            if (!item.active) {
                continue;
            }
            DeviceBinding binding = session.getDevices().get(item.deviceId);
            if (binding == null || binding.getEpoch() != item.epoch) {
                continue;
            }
            upstreamHandler.collectValue(session, item.deviceId, item.identifier, values.get(i),
                    deviceProperties.computeIfAbsent(item.deviceId, id -> new LinkedHashMap<>()));
        }
        // 3. 上报：提交到会话的 worker 队列
        deviceProperties.forEach((deviceId, properties) -> {
            DeviceBinding binding = session.getDevices().get(deviceId);
            if (properties.isEmpty() || binding == null) {
                return;
            }
            IotOpcUaDeviceConfigRespDTO deviceConfig = binding.getConfig();
            session.getUpstreamQueue().submit(() -> {
                upstreamHandler.handlePropertyPost(deviceConfig, properties);
                return null;
            }).onFailure(e -> log.warn("[handleData][设备 {} 属性上报无法提交，丢弃]", deviceId, e));
        });
    }

    // ========== 工具方法 ==========

    private static boolean isCurrent(IotOpcUaClientSession session, SubscriptionGroup group) {
        return session.getSubscriptionGroups().get(group.interval) == group;
    }

    private static boolean isSubscriptionGone(OpcUaSubscription subscription) {
        return subscription == null || !subscription.getSubscriptionId().isPresent();
    }

    private static void deactivateAll(SubscriptionGroup group) {
        group.items.values().forEach(item -> item.active = false);
        group.items.clear();
    }

    private static boolean isSubscribe(IotOpcUaPointRespDTO point) {
        return IotOpcUaCollectModeEnum.SUBSCRIBE.getMode().equals(point.getCollectMode());
    }

    /**
     * 订阅组：一个会话的一个采集间隔
     */
    public static class SubscriptionGroup {

        private final int interval;
        /**
         * 当前的 Milo Subscription；重建时换成新的对象，旧对象的回调据此丢弃
         */
        private OpcUaSubscription subscription;
        /**
         * 当前的 MonitoredItem：pointId -> 绑定
         */
        private final Map<Long, ItemBinding> items = new HashMap<>();
        /**
         * 是否有在途的订阅操作
         */
        private boolean busy;
        /**
         * 在途操作期间是否有新的变化
         */
        private boolean dirty;
        /**
         * 是否需要重建
         */
        private boolean rebuild;
        private Long retryTimerId;

        SubscriptionGroup(Integer interval) {
            this.interval = interval;
        }

        /**
         * 获取当前的 Subscription（测试、诊断用）
         *
         * @return Subscription
         */
        public OpcUaSubscription getSubscription() {
            return subscription;
        }

        /**
         * 是否有在途的订阅操作（测试、诊断用）
         */
        boolean isBusy() {
            return busy;
        }

        /**
         * 是否等待重建（测试、诊断用）
         */
        boolean isRebuild() {
            return rebuild;
        }

        /**
         * 重试定时器 ID（测试、诊断用）
         */
        Long getRetryTimerId() {
            return retryTimerId;
        }

    }

    /**
     * MonitoredItem 绑定：作为 MonitoredItem 的 userObject，通知回来时据此找到设备、属性
     */
    static class ItemBinding {

        private final Long deviceId;
        private final String identifier;
        private final NodeId nodeId;
        /**
         * 创建（或最近一次确认未变化）时设备的配置版本
         */
        private long epoch;
        /**
         * 是否有效：点位移除、变化、订阅组重建后置为 false，之后的通知丢弃
         */
        private boolean active = true;
        private OpcUaMonitoredItem monitoredItem;

        ItemBinding(Long deviceId, String identifier, NodeId nodeId, long epoch) {
            this.deviceId = deviceId;
            this.identifier = identifier;
            this.nodeId = nodeId;
            this.epoch = epoch;
        }

        boolean sameAs(ItemBinding other) {
            return Objects.equals(deviceId, other.deviceId) && Objects.equals(identifier, other.identifier)
                    && Objects.equals(nodeId, other.nodeId);
        }

    }

}
