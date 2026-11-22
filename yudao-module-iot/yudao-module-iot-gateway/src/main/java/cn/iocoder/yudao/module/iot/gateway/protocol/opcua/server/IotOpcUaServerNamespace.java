package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerNodeRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.security.IotOpcUaServerIdentityValidator;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.util.IotOpcUaServerValueConverter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.AccessContext;
import org.eclipse.milo.opcua.sdk.server.AttributeReader;
import org.eclipse.milo.opcua.sdk.server.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.sdk.server.items.DataItem;
import org.eclipse.milo.opcua.sdk.server.items.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.ViewDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT OPC UA Server 地址空间：每个 OPC UA Server 配置一个文件夹（挂在 Objects 下），每个节点一个只读变量
 * <ol>
 *     <li>只读：变量的 AccessLevel、UserAccessLevel 只有 CurrentRead，Milo 访问控制对写入返回 Bad_UserAccessDenied；
 *         本命名空间的 Write 兜底一律返回 Bad_NotWritable（不可见节点返回 Bad_NodeIdUnknown）；</li>
 *     <li>租户隔离：会话身份上的凭据别名决定可见的配置，只能看到该配置下的节点；Browse 过滤其它配置的文件夹、变量，
 *         Read（包括 MonitoredItem 的创建和采样）对其它配置的节点返回 Bad_NodeIdUnknown；本命名空间内没有登记所属配置的节点
 *         对会话一律不可见（fail-closed），其它命名空间的节点不受影响；</li>
 *     <li>MonitoredItem：由 Milo 的 {@link SubscriptionModel} 按采样间隔读取变量的值；</li>
 *     <li>值：网关收到设备的属性上报后，按 deviceId + 属性标识符更新变量，设备消息的租户必须与配置的租户一致；
 *         收到前状态为 Bad_WaitingForInitialData；SourceTimestamp 为设备消息的上报时间，配置刷新重建变量时沿用最后一次真实上报的值和时间戳。</li>
 * </ol>
 * NodeId 为 ns=本命名空间;s=cfg-配置编号/浏览名称，配置刷新后保持不变
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaServerNamespace extends ManagedNamespaceWithLifecycle {

    public static final String NAMESPACE_URI = "urn:yudao:iot:gateway:opc-ua-server";

    private final SubscriptionModel subscriptionModel;

    /**
     * 当前生效的配置：configId -> 配置（applyConfigs 串行调用，并发读取使用不可变快照）
     */
    private volatile Map<Long, IotOpcUaServerConfigRespDTO> configs = Collections.emptyMap();
    /**
     * 当前生效的配置：凭据别名 -> 配置
     */
    private volatile Map<String, IotOpcUaServerConfigRespDTO> configsByAlias = Collections.emptyMap();
    /**
     * 本命名空间节点所属的配置：NodeId -> configId（文件夹和变量）
     */
    private final Map<NodeId, Long> nodeOwners = new ConcurrentHashMap<>();
    /**
     * 设备的变量：deviceId -> 变量绑定；在 valueLock 内发布、读取
     */
    private volatile Map<Long, List<NodeBinding>> deviceNodes = Collections.emptyMap();
    /**
     * 设备属性的最近一次上报：tenantId|deviceId|identifier -> 值和时间戳；只记录与绑定租户一致的上报，配置变化重建变量时用于初始化
     */
    private final Map<String, ReportedValue> lastValues = new ConcurrentHashMap<>();
    /**
     * 变量值的写入锁：属性上报与配置刷新的值同步互斥，避免以旧缓存覆盖并发上报的新值
     */
    private final Object valueLock = new Object();

    public IotOpcUaServerNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);
        this.subscriptionModel = new SubscriptionModel(server, this);
        getLifecycleManager().addLifecycle(subscriptionModel);
    }

    // ========== 配置 ==========

    /**
     * 应用配置：新增、变化的配置重建文件夹和变量，删除的配置移除文件夹和变量；未变化的配置保持不变
     *
     * @param newConfigs 启用的配置（已按凭据允许的租户过滤）
     */
    public synchronized void applyConfigs(List<IotOpcUaServerConfigRespDTO> newConfigs) {
        Map<Long, IotOpcUaServerConfigRespDTO> newConfigMap = new LinkedHashMap<>();
        Map<String, IotOpcUaServerConfigRespDTO> newAliasMap = new HashMap<>();
        for (IotOpcUaServerConfigRespDTO config : CollUtil.emptyIfNull(newConfigs)) {
            // 同一凭据别名只取第一个（biz 已保证唯一，这里兜底）
            if (config.getId() == null || newAliasMap.putIfAbsent(config.getCredentialAlias(), config) != null) {
                continue;
            }
            newConfigMap.put(config.getId(), config);
        }
        Map<Long, IotOpcUaServerConfigRespDTO> oldConfigMap = this.configs;
        // 1. 删除、变化的配置：移除节点
        for (IotOpcUaServerConfigRespDTO old : oldConfigMap.values()) {
            if (!Objects.equals(newConfigMap.get(old.getId()), old)) {
                removeConfigNodes(old.getId());
            }
        }
        // 2. 新增、变化的配置：创建节点
        for (IotOpcUaServerConfigRespDTO config : newConfigMap.values()) {
            if (!Objects.equals(oldConfigMap.get(config.getId()), config)) {
                addConfigNodes(config);
            }
        }
        // 3. 设备 -> 变量绑定
        Map<Long, List<NodeBinding>> newDeviceNodes = new HashMap<>();
        Set<String> valueKeys = new HashSet<>();
        for (IotOpcUaServerConfigRespDTO config : newConfigMap.values()) {
            for (IotOpcUaServerNodeRespDTO node : CollUtil.emptyIfNull(config.getNodes())) {
                UaNode uaNode = getNodeManager().get(buildVariableNodeId(config.getId(), node.getBrowseName()));
                if (!(uaNode instanceof UaVariableNode)) {
                    continue;
                }
                NodeBinding binding = new NodeBinding(config.getTenantId(), node.getIdentifier(), node.getDataType(),
                        (UaVariableNode) uaNode);
                newDeviceNodes.computeIfAbsent(node.getDeviceId(), id -> new ArrayList<>()).add(binding);
                valueKeys.add(buildValueKey(config.getTenantId(), node.getDeviceId(), node.getIdentifier()));
            }
        }
        this.configs = Collections.unmodifiableMap(newConfigMap);
        this.configsByAlias = Collections.unmodifiableMap(newAliasMap);
        // 4. 发布绑定并同步变量的值：与属性上报互斥，上报要么已写入最近值（这里同步），要么在发布之后写入新绑定
        synchronized (valueLock) {
            this.deviceNodes = Collections.unmodifiableMap(newDeviceNodes);
            // 4.1 清理已不再暴露的设备属性的最近值
            lastValues.keySet().retainAll(valueKeys);
            // 4.2 同步变量的值：覆盖步骤 2 ~ 3 之间仍写入旧绑定的上报；沿用最后一次上报的值和时间戳，不生成新的时间戳
            newDeviceNodes.forEach((deviceId, bindings) -> {
                for (NodeBinding binding : bindings) {
                    ReportedValue reported = lastValues.get(buildValueKey(binding.tenantId, deviceId, binding.identifier));
                    if (reported == null) {
                        continue;
                    }
                    DataValue dataValue = toDataValue(reported, binding.dataType);
                    if (!dataValue.equals(binding.node.getValue())) {
                        binding.node.setValue(dataValue);
                    }
                }
            });
        }
    }

    /**
     * 按凭据别名获取当前生效的配置
     *
     * @param alias 凭据别名
     * @return 配置；不存在时返回 null
     */
    public IotOpcUaServerConfigRespDTO getConfigByAlias(String alias) {
        return alias != null ? configsByAlias.get(alias) : null;
    }

    /**
     * 获取当前生效的配置（限额计算、测试用）
     *
     * @return 配置
     */
    public List<IotOpcUaServerConfigRespDTO> getConfigs() {
        return new ArrayList<>(configs.values());
    }

    private void addConfigNodes(IotOpcUaServerConfigRespDTO config) {
        // 1. 配置的文件夹：挂在 Objects 下；先登记所属配置再加入地址空间，节点可见前已有租户归属
        NodeId folderId = buildFolderNodeId(config.getId());
        UaFolderNode folder = new UaFolderNode(getNodeContext(), folderId,
                newQualifiedName(config.getName()), LocalizedText.english(config.getName()));
        nodeOwners.put(folderId, config.getId());
        getNodeManager().addNode(folder);
        folder.addReference(new Reference(folderId, NodeIds.Organizes, NodeIds.ObjectsFolder.expanded(), false));
        // 2. 每个节点一个只读变量：同一配置下重复的浏览名称只创建一次
        for (IotOpcUaServerNodeRespDTO node : CollUtil.emptyIfNull(config.getNodes())) {
            NodeId nodeId = buildVariableNodeId(config.getId(), node.getBrowseName());
            if (getNodeManager().containsNode(nodeId)) {
                continue;
            }
            UaVariableNode variable = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(nodeId)
                    .setBrowseName(newQualifiedName(node.getBrowseName()))
                    .setDisplayName(LocalizedText.english(node.getBrowseName()))
                    .setDescription(LocalizedText.english(node.getDeviceName() + "." + node.getIdentifier()))
                    .setDataType(IotOpcUaServerValueConverter.toDataTypeId(node.getDataType()))
                    .setTypeDefinition(NodeIds.BaseDataVariableType)
                    .setAccessLevel(AccessLevel.READ_ONLY)
                    .setUserAccessLevel(AccessLevel.READ_ONLY)
                    .build();
            // 2.1 初始值：只取本配置租户下的最近值，不会带入其它租户的上报
            variable.setValue(toDataValue(lastValues.get(buildValueKey(config.getTenantId(), node.getDeviceId(),
                    node.getIdentifier())), node.getDataType()));
            // 2.2 先登记所属配置，再加入地址空间
            nodeOwners.put(nodeId, config.getId());
            getNodeManager().addNode(variable);
            folder.addOrganizes(variable);
        }
    }

    private void removeConfigNodes(Long configId) {
        List<NodeId> owned = new ArrayList<>();
        nodeOwners.forEach((nodeId, owner) -> {
            if (configId.equals(owner)) {
                owned.add(nodeId);
            }
        });
        for (NodeId nodeId : owned) {
            UaNode node = getNodeManager().get(nodeId);
            if (node != null) {
                node.delete();
            }
            nodeOwners.remove(nodeId);
        }
    }

    NodeId buildFolderNodeId(Long configId) {
        return newNodeId("cfg-" + configId);
    }

    NodeId buildVariableNodeId(Long configId, String browseName) {
        return newNodeId("cfg-" + configId + "/" + browseName);
    }

    // ========== 值 ==========

    /**
     * 设备属性上报：更新该设备已暴露的变量
     *
     * @param deviceId   设备编号
     * @param tenantId   设备的租户编号：只更新租户一致的绑定，并且只为它们记录最近值（防止跨租户串值）
     * @param properties 属性：标识符 -> 值
     * @param sourceTime 上报时间，作为 SourceTimestamp；为空时使用网关收到的时间
     */
    public void updateProperties(Long deviceId, Long tenantId, Map<String, Object> properties, DateTime sourceTime) {
        if (tenantId == null || properties == null) {
            return;
        }
        DateTime serverTime = DateTime.now();
        DateTime reportTime = sourceTime != null ? sourceTime : serverTime;
        synchronized (valueLock) {
            // 在锁内读取绑定：配置刷新发布新绑定后，上报写入新绑定
            List<NodeBinding> bindings = deviceNodes.get(deviceId);
            if (CollUtil.isEmpty(bindings)) {
                return;
            }
            properties.forEach((identifier, value) -> {
                if (value == null) {
                    return;
                }
                String valueKey = buildValueKey(tenantId, deviceId, identifier);
                // 乱序上报：带时间戳的报告早于已记录的 SourceTimestamp 时丢弃，相同时间允许更新；
                // 没有时间戳的报告沿用兼容语义，以网关当前时间为准直接更新
                if (sourceTime != null && isOlder(sourceTime, lastValues.get(valueKey))) {
                    return;
                }
                ReportedValue reported = null;
                for (NodeBinding binding : bindings) {
                    if (!Objects.equals(binding.identifier, identifier) || !Objects.equals(binding.tenantId, tenantId)) {
                        continue;
                    }
                    // 先记录最近值，再更新变量：配置刷新重建变量时可以拿到
                    if (reported == null) {
                        reported = new ReportedValue(value, reportTime, serverTime);
                        lastValues.put(valueKey, reported);
                    }
                    binding.node.setValue(toDataValue(reported, binding.dataType));
                }
            });
        }
    }

    private static boolean isOlder(DateTime reportTime, ReportedValue previous) {
        return previous != null && previous.sourceTime != null
                && reportTime.getUtcTime() < previous.sourceTime.getUtcTime();
    }

    private static DataValue toDataValue(ReportedValue reported, String dataType) {
        if (reported == null) {
            return new DataValue(new StatusCode(StatusCodes.Bad_WaitingForInitialData));
        }
        Variant variant = IotOpcUaServerValueConverter.toVariant(reported.value, dataType);
        if (variant == null) {
            return new DataValue(Variant.NULL_VALUE, new StatusCode(StatusCodes.Bad_TypeMismatch),
                    reported.sourceTime, reported.serverTime);
        }
        return new DataValue(variant, StatusCode.GOOD, reported.sourceTime, reported.serverTime);
    }

    private static String buildValueKey(Long tenantId, Long deviceId, String identifier) {
        return tenantId + "|" + deviceId + "|" + identifier;
    }

    // ========== 租户隔离 ==========

    /**
     * 节点对当前会话是否可见：其它命名空间的节点（Objects 等标准节点）、没有会话的内部访问可见；
     * 本命名空间的节点只对凭据别名对应所属配置的会话可见，没有登记所属配置时不可见（fail-closed）
     *
     * @param context 访问上下文
     * @param nodeId  节点
     * @return 是否可见
     */
    boolean isVisible(AccessContext context, NodeId nodeId) {
        if (nodeId == null || !getNamespaceIndex().equals(nodeId.getNamespaceIndex())) {
            return true;
        }
        Session session = context.getSession().orElse(null);
        if (session == null) {
            return true;
        }
        Long owner = nodeOwners.get(nodeId);
        if (owner == null) {
            return false;
        }
        IotOpcUaServerConfigRespDTO config = getConfigByAlias(IotOpcUaServerIdentityValidator.getCredentialAlias(session));
        return config != null && owner.equals(config.getId());
    }

    private List<Reference> filterReferences(AccessContext context, List<Reference> references) {
        List<Reference> result = new ArrayList<>(references.size());
        for (Reference reference : references) {
            NodeId target = reference.getTargetNodeId().toNodeId(getServer().getNamespaceTable()).orElse(null);
            if (isVisible(context, target)) {
                result.add(reference);
            }
        }
        return result;
    }

    @Override
    public List<ReferenceResult> browse(BrowseContext context, ViewDescription view, List<NodeId> nodeIds) {
        List<ReferenceResult> results = new ArrayList<>(nodeIds.size());
        for (NodeId nodeId : nodeIds) {
            if (!getNodeManager().containsNode(nodeId) || !isVisible(context, nodeId)) {
                results.add(ReferenceResult.unknown());
                continue;
            }
            results.add(ReferenceResult.of(filterReferences(context, getNodeManager().getReferences(nodeId))));
        }
        return results;
    }

    @Override
    public ReferenceResult.ReferenceList gather(BrowseContext context, ViewDescription view, NodeId nodeId) {
        if (!isVisible(context, nodeId)) {
            return ReferenceResult.of(Collections.<Reference>emptyList());
        }
        return ReferenceResult.of(filterReferences(context, getNodeManager().getReferences(nodeId)));
    }

    @Override
    public List<DataValue> read(ReadContext context, Double maxAge, TimestampsToReturn timestamps,
                                List<ReadValueId> readValueIds) {
        List<DataValue> results = new ArrayList<>(readValueIds.size());
        for (ReadValueId readValueId : readValueIds) {
            UaNode node = getNodeManager().get(readValueId.getNodeId());
            if (node == null || !isVisible(context, readValueId.getNodeId())) {
                results.add(new DataValue(new StatusCode(StatusCodes.Bad_NodeIdUnknown)));
                continue;
            }
            results.add(AttributeReader.readAttribute(context, node, readValueId.getAttributeId(), timestamps,
                    readValueId.getIndexRange(), readValueId.getDataEncoding()));
        }
        return results;
    }

    /**
     * 写入一律拒绝：节点只读，不提供任何写入（包括 Value 以外的属性）
     */
    @Override
    public List<StatusCode> write(WriteContext context, List<WriteValue> writeValues) {
        List<StatusCode> results = new ArrayList<>(writeValues.size());
        for (WriteValue writeValue : writeValues) {
            boolean known = getNodeManager().containsNode(writeValue.getNodeId())
                    && isVisible(context, writeValue.getNodeId());
            results.add(new StatusCode(known ? StatusCodes.Bad_NotWritable : StatusCodes.Bad_NodeIdUnknown));
        }
        return results;
    }

    // ========== MonitoredItem ==========

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

    /**
     * 变量绑定
     */
    private static final class NodeBinding {

        private final Long tenantId;
        private final String identifier;
        private final String dataType;
        private final UaVariableNode node;

        private NodeBinding(Long tenantId, String identifier, String dataType, UaVariableNode node) {
            this.tenantId = tenantId;
            this.identifier = identifier;
            this.dataType = dataType;
            this.node = node;
        }

    }

    /**
     * 设备属性的一次上报：值和时间戳
     */
    private static final class ReportedValue {

        private final Object value;
        /**
         * 上报时间：SourceTimestamp
         */
        private final DateTime sourceTime;
        /**
         * 网关收到的时间：ServerTimestamp
         */
        private final DateTime serverTime;

        private ReportedValue(Object value, DateTime sourceTime, DateTime serverTime) {
            this.value = value;
            this.sourceTime = sourceTime;
            this.serverTime = serverTime;
        }

    }

}
