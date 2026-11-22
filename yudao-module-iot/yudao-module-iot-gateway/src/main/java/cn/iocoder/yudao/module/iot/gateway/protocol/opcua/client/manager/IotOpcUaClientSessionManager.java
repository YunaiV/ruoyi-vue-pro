package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotOpcUaUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession.DeviceBinding;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.security.IotOpcUaClientSecurityFactory;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util.IotOpcUaNodeIdResolver;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.OperationLimits;
import org.eclipse.milo.opcua.sdk.client.SessionActivityListener;
import org.eclipse.milo.opcua.sdk.client.UaSession;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * IoT OPC UA 会话管理器
 * <p>
 * 1. 按会话 key（租户 + 端点 + 安全策略 + 模式 + 认证方式 + 凭据别名 + 超时时间 + 重试间隔）分组设备，每组一个 {@link OpcUaClient} 长连接；
 *    超时时间、重试间隔等会话级配置变化后，设备换到新的会话；
 * 2. 创建客户端（发现端点、选择端点）失败时按 retryInterval 重试；创建后由 Milo 负责 KeepAlive、断线重连、
 *    Session 重建和 Subscription 转移，本类只响应会话激活 / 失效；
 * 3. 会话激活：进入新的一代，按最新的命名空间表解析 NodeId，读取 OperationLimits，完成后同步订阅、设备上线；
 *    会话失效：进入新的一代（之前发起的读取、订阅通知全部作废），设备离线；
 * 4. 设备点位变化时只更新该设备的轮询定时器和 MonitoredItem。
 * <p>
 * 只在协议的 context 线程上调用；Milo 的回调切回 context 后再处理
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientSessionManager {

    private final Vertx vertx;
    private final Context context;
    private final IotOpcUaClientConfig config;
    private final IotOpcUaClientSecurityFactory securityFactory;
    private final IotOpcUaDeviceStateTracker stateTracker;
    private final IotOpcUaClientPollScheduler pollScheduler;
    private final IotOpcUaClientSubscriptionManager subscriptionManager;

    /**
     * 会话：会话 key -> 会话
     */
    private final Map<String, IotOpcUaClientSession> sessions = new HashMap<>();
    /**
     * 设备所在的会话：deviceId -> 会话 key
     */
    private final Map<Long, String> deviceSessionKeys = new HashMap<>();
    /**
     * 设备配置被拒绝的原因：deviceId -> 原因（原因变化时才告警）
     */
    private final Map<Long, String> rejectedReasons = new HashMap<>();

    public IotOpcUaClientSessionManager(Vertx vertx, Context context, IotOpcUaClientConfig config,
                                        IotOpcUaClientSecurityFactory securityFactory,
                                        IotOpcUaDeviceStateTracker stateTracker,
                                        IotOpcUaClientPollScheduler pollScheduler,
                                        IotOpcUaClientSubscriptionManager subscriptionManager) {
        this.vertx = vertx;
        this.context = context;
        this.config = config;
        this.securityFactory = securityFactory;
        this.stateTracker = stateTracker;
        this.pollScheduler = pollScheduler;
        this.subscriptionManager = subscriptionManager;
    }

    // ========== 设备配置 ==========

    /**
     * 应用最新的设备配置
     *
     * @param configs          当前有效的设备配置
     * @param removedDeviceIds 已删除（或禁用）的设备
     */
    public void applyConfigs(List<IotOpcUaDeviceConfigRespDTO> configs, Set<Long> removedDeviceIds) {
        // 1. 有效配置：校验通过的绑定到会话，不通过的移除
        for (IotOpcUaDeviceConfigRespDTO device : configs) {
            try {
                String reason = securityFactory.validateDevice(device);
                if (reason != null) {
                    if (!Objects.equals(rejectedReasons.put(device.getDeviceId(), reason), reason)) {
                        log.warn("[applyConfigs][设备 {} 的 OPC UA 配置不可用: {}]", device.getDeviceId(), reason);
                    }
                    removeDevice(device.getDeviceId(), device);
                    continue;
                }
                rejectedReasons.remove(device.getDeviceId());
                bindDevice(device);
            } catch (Exception e) {
                log.error("[applyConfigs][处理设备配置失败, deviceId={}]", device.getDeviceId(), e);
            }
        }
        // 2. 已删除的设备
        for (Long deviceId : removedDeviceIds) {
            rejectedReasons.remove(deviceId);
            removeDevice(deviceId, null);
        }
        // 3. 已创建客户端、未激活、没有在途连接的会话：再调用一次 connectAsync（Milo 内部也会重连，这里兜底）
        for (IotOpcUaClientSession session : sessions.values()) {
            if (session.getClient() != null && !session.isActive() && !session.isConnecting()) {
                connect(session);
            }
        }
    }

    private void bindDevice(IotOpcUaDeviceConfigRespDTO device) {
        Long deviceId = device.getDeviceId();
        String key = IotOpcUaClientSecurityFactory.buildSessionKey(device);
        // 1. 设备换了会话（端点、安全配置变化）：先从原会话移除
        String oldKey = deviceSessionKeys.get(deviceId);
        if (oldKey != null && !oldKey.equals(key)) {
            log.info("[bindDevice][设备 {} 的会话变化: {} -> {}]", deviceId, oldKey, key);
            unbindDevice(deviceId);
        }

        // 2. 绑定到会话
        IotOpcUaClientSession session = sessions.computeIfAbsent(key, k -> new IotOpcUaClientSession(k, context));
        deviceSessionKeys.put(deviceId, key);
        DeviceBinding binding = session.getDevices().get(deviceId);
        boolean changed;
        if (binding == null) {
            binding = new DeviceBinding(device);
            session.getDevices().put(deviceId, binding);
            changed = true;
        } else {
            IotOpcUaDeviceConfigRespDTO old = binding.getConfig();
            changed = !Objects.equals(old.getPoints(), device.getPoints())
                    || !Objects.equals(old.getProductKey(), device.getProductKey())
                    || !Objects.equals(old.getDeviceName(), device.getDeviceName());
            binding.setConfig(device);
        }
        // 3. 点位变化：更新配置版本（之前的读取结果、通知作废），重新解析 NodeId，更新轮询和订阅
        if (changed) {
            binding.setEpoch(binding.getEpoch() + 1);
            if (session.isActive()) {
                resolveNodeIds(session, binding);
            }
            pollScheduler.updateDevice(session, binding);
            subscriptionManager.onDeviceChanged(session);
        }

        // 4. 上下线：与会话状态一致（会话激活后，OperationLimits 读取完成前不上线，完成时由 handleSessionActive 统一上线）
        if (!session.isActive()) {
            stateTracker.offline(device);
        } else if (session.getMaxNodesPerRead() != null) {
            stateTracker.online(device);
        }
        // 5. 确保客户端
        ensureClient(session);
    }

    /**
     * 移除设备：停止轮询、订阅，发送离线消息；会话没有设备时关闭会话
     *
     * @param deviceId   设备 ID
     * @param lastConfig 设备最后一次的配置（用于发送离线消息），可为 null
     */
    private void removeDevice(Long deviceId, IotOpcUaDeviceConfigRespDTO lastConfig) {
        DeviceBinding binding = unbindDevice(deviceId);
        IotOpcUaDeviceConfigRespDTO config = binding != null ? binding.getConfig() : lastConfig;
        if (config != null) {
            stateTracker.remove(config);
        }
    }

    private DeviceBinding unbindDevice(Long deviceId) {
        String key = deviceSessionKeys.remove(deviceId);
        pollScheduler.removeDevice(deviceId);
        IotOpcUaClientSession session = key != null ? sessions.get(key) : null;
        if (session == null) {
            return null;
        }
        DeviceBinding binding = session.getDevices().remove(deviceId);
        if (session.getDevices().isEmpty()) {
            closeSession(session);
        } else {
            subscriptionManager.onDeviceChanged(session);
        }
        return binding;
    }

    // ========== 客户端 ==========

    private void ensureClient(IotOpcUaClientSession session) {
        if (session.isClosed() || session.getClient() != null || session.isCreating() || session.getRetryTimerId() != null) {
            return;
        }
        IotOpcUaDeviceConfigRespDTO device = session.getAnyDeviceConfig();
        if (device == null) {
            return;
        }
        // 发现端点、创建客户端会阻塞：提交到 serviceQueue
        session.setCreating(true);
        session.getServiceQueue().submit(() -> securityFactory.createClient(device)).onComplete(ar -> {
            session.setCreating(false);
            // 1. 会话已关闭：丢弃新建的客户端
            if (session.isClosed()) {
                if (ar.succeeded()) {
                    ar.result().disconnectAsync();
                }
                return;
            }
            // 2. 失败：按 retryInterval 重试
            if (ar.failed()) {
                long retryInterval = ObjUtil.defaultIfNull(device.getRetryInterval(), 10000);
                log.warn("[ensureClient][会话 {} 创建客户端失败，{} ms 后重试: {}]", session.getKey(), retryInterval,
                        ar.cause().getMessage());
                log.debug("[ensureClient][会话 {} 创建客户端失败]", session.getKey(), ar.cause());
                session.setRetryTimerId(vertx.setTimer(retryInterval, id -> {
                    session.setRetryTimerId(null);
                    ensureClient(session);
                }));
                return;
            }
            // 3. 成功：注册会话监听，连接
            OpcUaClient client = ar.result();
            session.setClient(client);
            client.addSessionActivityListener(new SessionActivityListener() {

                @Override
                public void onSessionActive(UaSession uaSession) {
                    // 连接代数在 Milo 的线程上立即加一：切回 context 之前，旧激活期间提交的写入即可感知
                    long connectionEpoch = session.getConnectionEpoch().incrementAndGet();
                    context.runOnContext(v -> handleSessionActive(session, client, connectionEpoch));
                }

                @Override
                public void onSessionInactive(UaSession uaSession) {
                    session.getConnectionEpoch().incrementAndGet();
                    context.runOnContext(v -> handleSessionInactive(session, client));
                }

            });
            log.info("[ensureClient][会话 {} 客户端已创建，端点: {}]", session.getKey(),
                    client.getConfig().getEndpoint().getEndpointUrl());
            connect(session);
        });
    }

    private void connect(IotOpcUaClientSession session) {
        OpcUaClient client = session.getClient();
        session.setConnecting(true);
        client.connectAsync().whenComplete((c, ex) -> context.runOnContext(v -> {
            if (session.getClient() != client) {
                return;
            }
            session.setConnecting(false);
            if (ex != null && session.warnOnce("connect")) {
                // 连接失败后 Milo 会继续重连；同一代只告警一次
                log.warn("[connect][会话 {} 连接失败，等待重连: {}]", session.getKey(), ex.getMessage());
            }
        }));
    }

    // ========== 会话事件 ==========

    /**
     * 会话激活：Milo 在激活前已刷新命名空间表；转移失败的 Subscription 已被重置
     *
     * @param session         会话
     * @param client          回调所属的客户端
     * @param connectionEpoch 激活回调时的连接代数：之后底层会话再失效 / 激活时会变化，写入据此阻断
     */
    void handleSessionActive(IotOpcUaClientSession session, OpcUaClient client, long connectionEpoch) {
        if (session.isClosed() || session.getClient() != client) {
            return;
        }
        // 1. 新的一代；记录本次激活对应的连接代数
        session.nextGeneration();
        session.setActiveConnectionEpoch(connectionEpoch);
        long generation = session.getGeneration();
        // 2. 命名空间表：变化时重新解析所有 NodeId，并重建订阅
        String[] namespaces = client.getNamespaceTable().toArray();
        boolean namespaceChanged = session.getNamespaces() != null && !Arrays.equals(session.getNamespaces(), namespaces);
        session.setNamespaces(namespaces);
        session.setNamespaceTable(IotOpcUaNodeIdResolver.buildNamespaceTable(namespaces));
        for (DeviceBinding binding : session.getDevices().values()) {
            resolveNodeIds(session, binding);
            if (namespaceChanged) {
                warnNamespaceIndexBased(session, binding);
            }
        }
        if (namespaceChanged) {
            log.warn("[handleSessionActive][会话 {} 的命名空间表已变化，重新解析 NodeId 并重建订阅]", session.getKey());
        }
        session.setActive(true);
        log.info("[handleSessionActive][会话 {} 已激活（第 {} 代），设备 {} 个]", session.getKey(), generation,
                session.getDevices().size());

        // 3. OperationLimits：读取完成前不轮询、不同步订阅、不上线；读取失败时使用本地配置
        session.setMaxNodesPerRead(null);
        session.setMaxMonitoredItemsPerCall(null);
        session.setMaxNodesPerWrite(null);
        session.getServiceQueue().submit(client::readOperationLimits).onComplete(ar -> {
            if (session.getGeneration() != generation) {
                return;
            }
            OperationLimits limits = null;
            if (ar.succeeded()) {
                limits = ar.result();
            } else {
                log.warn("[handleSessionActive][会话 {} 读取 OperationLimits 失败，使用本地配置]", session.getKey(), ar.cause());
            }
            session.setMaxNodesPerRead(minLimit(config.getMaxNodesPerRead(),
                    limits != null ? limits.maxNodesPerRead() : Optional.empty()));
            session.setMaxMonitoredItemsPerCall(minLimit(config.getMaxMonitoredItemsPerCall(),
                    limits != null ? limits.maxMonitoredItemsPerCall() : Optional.empty()));
            session.setMaxNodesPerWrite(minLimit(config.getMaxNodesPerWrite(),
                    limits != null ? limits.maxNodesPerWrite() : Optional.empty()));
            // 4. 订阅：按本次的 MaxMonitoredItemsPerCall 转移或重建（与一期激活顺序一致：OperationLimits 之后）
            subscriptionManager.onSessionActive(session, namespaceChanged);
            // 5. 设备上线：OperationLimits 读取完成（成功或回退本地配置）后
            session.getDevices().values().forEach(binding -> stateTracker.online(binding.getConfig()));
        });
    }

    /**
     * 本地上限与服务端 OperationLimits 取较小值；服务端缺失或为 0 时使用本地上限
     */
    private static int minLimit(int local, Optional<UInteger> serverLimit) {
        return serverLimit.map(UInteger::intValue).filter(v -> v > 0).map(v -> Math.min(local, v)).orElse(local);
    }

    /**
     * 会话失效（断线、Session 超时）：进入新的一代，之前发起的读取、订阅通知全部作废，设备离线
     */
    void handleSessionInactive(IotOpcUaClientSession session, OpcUaClient client) {
        if (session.isClosed() || session.getClient() != client || !session.isActive()) {
            return;
        }
        session.setActive(false);
        session.nextGeneration();
        session.setMaxNodesPerRead(null);
        session.setMaxMonitoredItemsPerCall(null);
        session.setMaxNodesPerWrite(null);
        log.warn("[handleSessionInactive][会话 {} 已失效，等待 Milo 重连]", session.getKey());
        session.getDevices().values().forEach(binding -> stateTracker.offline(binding.getConfig()));
    }

    private void resolveNodeIds(IotOpcUaClientSession session, DeviceBinding binding) {
        binding.getResolvedNodeIds().clear();
        for (IotOpcUaPointRespDTO point : CollUtil.emptyIfNull(binding.getConfig().getPoints())) {
            NodeId nodeId = IotOpcUaNodeIdResolver.resolve(point.getNodeId(), session.getNamespaceTable());
            if (nodeId == null) {
                if (session.warnOnce("resolve:" + point.getId())) {
                    log.warn("[resolveNodeIds][设备 {} 属性 {} 的 NodeId {} 无法解析（格式不正确或命名空间不存在），跳过]",
                            binding.getConfig().getDeviceId(), point.getIdentifier(), point.getNodeId());
                }
                continue;
            }
            binding.getResolvedNodeIds().put(point.getId(), nodeId);
        }
    }

    private static void warnNamespaceIndexBased(IotOpcUaClientSession session, DeviceBinding binding) {
        for (IotOpcUaPointRespDTO point : CollUtil.emptyIfNull(binding.getConfig().getPoints())) {
            if (IotOpcUaUtils.isNamespaceIndexNodeId(point.getNodeId())
                    && session.warnOnce("ns-index:" + point.getId())) {
                log.warn("[warnNamespaceIndexBased][设备 {} 属性 {} 的 NodeId {} 使用 ns= 索引，服务端命名空间表已变化，"
                                + "索引可能指向其它命名空间，建议改为 nsu= 写法]",
                        binding.getConfig().getDeviceId(), point.getIdentifier(), point.getNodeId());
            }
        }
    }

    // ========== 关闭 ==========

    private CompletableFuture<?> closeSession(IotOpcUaClientSession session) {
        session.setClosed(true);
        session.setActive(false);
        session.nextGeneration();
        sessions.remove(session.getKey(), session);
        if (session.getRetryTimerId() != null) {
            vertx.cancelTimer(session.getRetryTimerId());
            session.setRetryTimerId(null);
        }
        subscriptionManager.closeSession(session);
        OpcUaClient client = session.getClient();
        session.setClient(null);
        log.info("[closeSession][会话 {} 已关闭]", session.getKey());
        // 关闭 Session（同时删除服务端的 Subscription）并断开连接
        return client != null ? client.disconnectAsync() : CompletableFuture.completedFuture(null);
    }

    /**
     * 关闭所有会话（协议停止）：不逐个发送设备离线消息
     *
     * @return 所有客户端断开连接的 Future
     */
    public CompletableFuture<Void> closeAll() {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (IotOpcUaClientSession session : new ArrayList<>(sessions.values())) {
            futures.add(closeSession(session));
        }
        pollScheduler.stopAll();
        deviceSessionKeys.clear();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * 获取设备所在的会话（属性写入使用）
     *
     * @param deviceId 设备 ID
     * @return 会话；设备未绑定（没有配置、配置被拒绝或已删除）时返回 null
     */
    public IotOpcUaClientSession getDeviceSession(Long deviceId) {
        String key = deviceSessionKeys.get(deviceId);
        return key != null ? sessions.get(key) : null;
    }

    /**
     * 获取所有会话（测试、诊断用）
     *
     * @return 会话
     */
    public Collection<IotOpcUaClientSession> getSessions() {
        return sessions.values();
    }

}
