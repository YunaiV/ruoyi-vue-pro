package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Context;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IoT OPC UA 会话：租户、端点、安全策略、模式、认证方式、凭据别名、超时时间、重试间隔都相同的设备共用一个 {@link OpcUaClient} 长连接
 * <p>
 * 除标注 volatile 的字段外，只在协议的 context 线程上读写；
 * Milo 的回调（会话激活 / 失效、读取结果、订阅通知）都切回 context 后再处理，并用 {@link #generation} 丢弃过期的结果
 *
 * @author 芋道源码
 */
@Getter
@Setter
public class IotOpcUaClientSession {

    /**
     * 会话 key
     */
    private final String key;
    /**
     * 阻塞的 Milo 操作（创建客户端、读取 OperationLimits、创建 / 同步 / 删除订阅）的有序 worker 队列
     */
    private final IotWorkerTaskQueue serviceQueue;
    /**
     * 属性上报（发送到消息总线会阻塞）的有序 worker 队列
     */
    private final IotWorkerTaskQueue upstreamQueue;

    /**
     * 会话内的设备：deviceId -> 设备绑定
     */
    private final Map<Long, DeviceBinding> devices = new HashMap<>();
    /**
     * 订阅组：采集间隔 -> 订阅组
     */
    private final Map<Integer, IotOpcUaClientSubscriptionManager.SubscriptionGroup> subscriptionGroups = new HashMap<>();

    /**
     * Milo 客户端；创建成功前为 null。创建后由 Milo 负责断线重连、Session 重建和订阅转移
     */
    private OpcUaClient client;
    /**
     * 是否正在创建客户端（发现端点、选择端点）
     */
    private boolean creating;
    /**
     * 是否正在执行 connectAsync
     */
    private boolean connecting;
    /**
     * 创建客户端失败后的重试定时器
     */
    private Long retryTimerId;
    /**
     * 是否已关闭：会话内没有设备，或协议停止
     */
    private boolean closed;

    /**
     * 会话是否处于激活状态：激活后才轮询、处理订阅通知
     */
    private volatile boolean active;
    /**
     * 代数：每次会话激活、失效、关闭时加一。读取、订阅等异步结果回到 context 时，代数不一致即丢弃
     */
    private volatile long generation;
    /**
     * 连接代数：Milo 回调会话激活 / 失效时，在 Milo 的线程上立即加一（不等切回 context）
     * <p>
     * {@link #generation}、{@link #active} 要等回调切回 context 才更新；物理断开、重连（Milo 复用同一个 {@link OpcUaClient}）
     * 与切回 context 之间，写入以它判断底层会话是否已经变化
     */
    private final AtomicLong connectionEpoch = new AtomicLong();
    /**
     * 本次激活对应的连接代数：在 context 上处理会话激活时记录（取自激活回调当时的 {@link #connectionEpoch}）；
     * 与 {@link #connectionEpoch} 不相等表示底层会话在本次激活之后已失效或重新激活
     */
    private volatile long activeConnectionEpoch = -1;
    /**
     * 本次激活读取的命名空间数组（nsu= 写法的 NodeId 据此解析）
     */
    private String[] namespaces;
    private NamespaceTable namespaceTable;
    /**
     * 本次激活每次 Read 的最大节点数：min(本地配置, 服务端 OperationLimits)；读取完成前为 null，不轮询
     */
    private Integer maxNodesPerRead;
    /**
     * 本次激活每次创建、修改、删除 MonitoredItem 的最大数量：min(本地配置, 服务端 OperationLimits)；
     * 读取完成前为 null，不同步订阅（分批由 Milo 的 OpcUaSubscription 按该值执行）
     */
    private Integer maxMonitoredItemsPerCall;
    /**
     * 本次激活每次 Write 的最大节点数：min(本地配置, 服务端 OperationLimits)；读取完成前为 null，不写入
     */
    private Integer maxNodesPerWrite;

    /**
     * 本代已告警过的 key：同一问题每代只告警一次，避免每次轮询都刷日志
     */
    private final Set<String> warnedKeys = new HashSet<>();

    public IotOpcUaClientSession(String key, Context context) {
        this.key = key;
        this.serviceQueue = new IotWorkerTaskQueue(context);
        this.upstreamQueue = new IotWorkerTaskQueue(context);
    }

    /**
     * 进入新的一代：激活、失效、关闭时调用
     */
    public void nextGeneration() {
        generation++;
        warnedKeys.clear();
    }

    /**
     * 本代是否第一次出现该问题
     *
     * @param warnKey 问题 key
     * @return 第一次出现时返回 true（调用方记日志）
     */
    public boolean warnOnce(String warnKey) {
        return warnedKeys.add(warnKey);
    }

    /**
     * 获取会话内任一设备的配置：创建客户端、会话级重试时使用
     * <p>
     * 会话 key 已包含端点、安全配置、超时时间、重试间隔，同一会话的设备这些配置都相同，取哪个设备的结果一致
     *
     * @return 设备配置；没有设备时返回 null
     */
    public IotOpcUaDeviceConfigRespDTO getAnyDeviceConfig() {
        return devices.values().stream().map(DeviceBinding::getConfig).findFirst().orElse(null);
    }

    /**
     * 设备绑定：设备配置 + 本代解析出的 NodeId
     */
    @Getter
    @Setter
    public static class DeviceBinding {

        private IotOpcUaDeviceConfigRespDTO config;
        /**
         * 配置版本：设备的点位、标识变化时加一
         */
        private long epoch;
        /**
         * 本代解析出的 NodeId：pointId -> NodeId；解析失败的点位不在其中
         */
        private final Map<Long, NodeId> resolvedNodeIds = new HashMap<>();

        public DeviceBinding(IotOpcUaDeviceConfigRespDTO config) {
            this.config = config;
        }

    }

}
