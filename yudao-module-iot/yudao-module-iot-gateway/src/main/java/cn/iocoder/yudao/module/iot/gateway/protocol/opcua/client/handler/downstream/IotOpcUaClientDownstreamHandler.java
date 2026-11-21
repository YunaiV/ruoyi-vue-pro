package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.downstream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaPointRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession.DeviceBinding;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSessionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util.IotOpcUaValueConverter;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.OpcUaDataType;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessLevelType;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;

/**
 * IoT OPC UA 下行属性写入处理器：thing.property.set 写节点的 Value 属性，逐项回复 thing.property.set_reply
 * <p>
 * 1. 在协议的 context 上校验：参数、设备绑定、会话激活、每设备排队上限，以及每个属性的点位（启用、可写、NodeId 已解析）和值
 *    （不能为空、数组或结构体）；
 * 2. 通过校验的属性提交到会话的有序 serviceQueue（与创建客户端、订阅操作同一个队列，同一会话的写入按收到的顺序执行）：
 *    先 Read 节点的 DataType、ValueRank、AccessLevel、UserAccessLevel，按实际的 DataType 转换值，再按 MaxNodesPerWrite 分批 Write；
 * 3. 读取节点属性之前、每批 Write 之前，都切回 context 核对（{@link #checkAlive}）：截止时间、会话代数，
 *    以及设备仍绑定在提交时的会话、绑定对象未被替换、配置版本 epoch 未变化（与轮询的 epoch 语义一致：点位启用 / 删除、
 *    可写、NodeId 变化都会递增 epoch；端点、安全配置、凭据变化会换会话）；不一致时不再使用旧的 NodeId 写入；
 *    每批 writeAsync 调用前，还在 worker 上直接核对连接代数（{@link IotOpcUaClientSession#getConnectionEpoch()}，
 *    Milo 回调会话失效 / 激活时立即加一）：覆盖物理断开、重连之后、回调切回 context 之前的窗口；
 * 4. 每个属性一个结果：网关判定的失败（点位不可写、节点不可写、类型不支持、值不合法、超时、会话过期等），
 *    或服务端返回的 StatusCode（十六进制原样带回）；结果回到 context 后释放排队名额，切到 worker 回复 biz。
 * <p>
 * {@link #handle} 可以在任意线程调用（切回 context）；{@link #execute} 在 worker 上执行（核对时阻塞等待 context），
 * 其余只在 context 上执行
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientDownstreamHandler {

    /**
     * 每个节点写入前读取的属性：DataType、ValueRank、AccessLevel、UserAccessLevel
     */
    private static final List<AttributeId> WRITE_CHECK_ATTRIBUTES = Arrays.asList(AttributeId.DataType,
            AttributeId.ValueRank, AttributeId.AccessLevel, AttributeId.UserAccessLevel);
    /**
     * 按标量写入的 ValueRank：-1 标量、-2 任意、-3 标量或一维；其它（数组、-4 等未定义值）不支持
     */
    private static final Set<Integer> SCALAR_VALUE_RANKS = new HashSet<>(Arrays.asList(-1, -2, -3));

    private final Vertx vertx;
    private final Context context;
    private final IotOpcUaClientConfig config;
    private final IotOpcUaClientSessionManager sessionManager;
    private final IotDeviceMessageService messageService;
    private final String serverId;

    /**
     * 设备排队中（含执行中）的写入消息数：deviceId -> 数量
     */
    private final Map<Long, Integer> pendingWrites = new HashMap<>();

    public IotOpcUaClientDownstreamHandler(Vertx vertx, Context context, IotOpcUaClientConfig config,
                                           IotOpcUaClientSessionManager sessionManager,
                                           IotDeviceMessageService messageService, String serverId) {
        this.vertx = vertx;
        this.context = context;
        this.config = config;
        this.sessionManager = sessionManager;
        this.messageService = messageService;
        this.serverId = serverId;
    }

    /**
     * 处理属性设置：切回协议的 context
     *
     * @param message thing.property.set 请求
     */
    public void handle(IotDeviceMessage message) {
        context.runOnContext(v -> handle0(message));
    }

    /**
     * 处理属性设置（context）：校验参数、绑定、会话和每个属性，通过的属性提交到会话的有序队列
     *
     * @param message thing.property.set 请求
     */
    @SuppressWarnings("unchecked")
    void handle0(IotDeviceMessage message) {
        // 1. 校验参数：属性标识符 → 属性值
        if (!(message.getParams() instanceof Map) || MapUtil.isEmpty((Map<?, ?>) message.getParams())) {
            reply(message, null, PROPERTY_SET_PARAMS_INVALID, "params 需要是非空的属性 Map");
            return;
        }
        Map<String, Object> params = (Map<String, Object>) message.getParams();
        Long deviceId = message.getDeviceId();
        Map<String, IotDevicePropertySetResultDTO> results = new LinkedHashMap<>();
        // 2.1 设备绑定、会话已激活（OperationLimits 已读取）
        IotOpcUaClientSession session = sessionManager.getDeviceSession(deviceId);
        DeviceBinding binding = session != null ? session.getDevices().get(deviceId) : null;
        if (binding == null) {
            failAll(params.keySet(), results, IotDevicePropertySetResultDTO.failure(PROPERTY_SET_DEVICE_NOT_CONFIGURED));
            reply(message, results, null);
            return;
        }
        OpcUaClient client = session.getClient();
        Integer maxNodesPerRead = session.getMaxNodesPerRead();
        Integer maxNodesPerWrite = session.getMaxNodesPerWrite();
        if (client == null || !session.isActive() || maxNodesPerRead == null || maxNodesPerWrite == null) {
            failAll(params.keySet(), results, IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_SESSION_INACTIVE));
            reply(message, results, null);
            return;
        }
        // 2.2 每设备排队上限
        if (pendingWrites.getOrDefault(deviceId, 0) >= config.getMaxPendingWritesPerDevice()) {
            failAll(params.keySet(), results, IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_QUEUE_FULL));
            reply(message, results, null);
            return;
        }

        // 3. 逐个属性校验点位和值：不通过的直接记录结果，不影响其它属性
        List<WriteItem> items = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String identifier = entry.getKey();
            IotDevicePropertySetResultDTO failure = validateItem(binding, identifier, entry.getValue(), items);
            results.put(identifier, failure); // 通过校验的先占位（null），保持参数顺序
        }
        if (items.isEmpty()) {
            reply(message, results, null);
            return;
        }

        // 4. 提交到会话的有序队列：截止时间从现在开始计算（含排队）；记下提交时的会话代数、绑定和配置版本
        WriteTask task = new WriteTask(deviceId, session, binding, binding.getEpoch(), session.getGeneration(),
                session.getActiveConnectionEpoch(), client, maxNodesPerRead, maxNodesPerWrite,
                System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(config.getWriteTimeout()));
        pendingWrites.merge(deviceId, 1, Integer::sum);
        session.getServiceQueue().submit(() -> execute(task, items))
                .onComplete(ar -> {
                    pendingWrites.computeIfPresent(deviceId, (k, count) -> count > 1 ? count - 1 : null);
                    if (ar.succeeded()) {
                        results.putAll(ar.result());
                    } else {
                        IotDevicePropertySetResultDTO failure = ar.cause() instanceof RejectedExecutionException
                                ? IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_QUEUE_FULL)
                                : IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_FAILED, ar.cause().getMessage());
                        items.forEach(item -> results.put(item.identifier, failure));
                    }
                    reply(message, results, null);
                });
    }

    /**
     * 校验单个属性：点位存在、可写、NodeId 已解析，值不能为空、数组或结构体
     *
     * @return 失败结果；通过时返回 null，并加入 items
     */
    private static IotDevicePropertySetResultDTO validateItem(DeviceBinding binding, String identifier, Object value,
                                                              List<WriteItem> items) {
        IotOpcUaPointRespDTO point = findPoint(binding.getConfig(), identifier);
        if (point == null) {
            return IotDevicePropertySetResultDTO.failure(PROPERTY_SET_POINT_NOT_EXISTS);
        }
        if (!Boolean.TRUE.equals(point.getWritable())) {
            return IotDevicePropertySetResultDTO.failure(PROPERTY_SET_POINT_NOT_WRITABLE, "点位未开启可写");
        }
        NodeId nodeId = binding.getResolvedNodeIds().get(point.getId());
        if (nodeId == null) {
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_NODE_ID_UNRESOLVED);
        }
        if (value == null || value instanceof Map || value instanceof Collection || value.getClass().isArray()) {
            return IotDevicePropertySetResultDTO.failure(PROPERTY_SET_VALUE_INVALID,
                    value == null ? "值不能为空" : "不支持数组或结构体");
        }
        items.add(new WriteItem(identifier, nodeId, value));
        return null;
    }

    /**
     * 按属性标识符查找点位：网关配置接口已按物模型属性去重，同一标识符最多一个点位
     */
    private static IotOpcUaPointRespDTO findPoint(IotOpcUaDeviceConfigRespDTO config, String identifier) {
        return CollUtil.findOne(CollUtil.emptyIfNull(config.getPoints()),
                point -> StrUtil.equals(point.getIdentifier(), identifier));
    }

    // ========== worker 上执行 ==========

    /**
     * 在会话的有序队列（worker）上执行写入：核对 → 读取节点属性 → 校验、转换 → 按 MaxNodesPerWrite 分批核对、写入
     *
     * @param task  提交时的会话、绑定、配置版本和截止时间
     * @param items 通过校验的属性
     * @return 每个属性的结果
     */
    Map<String, IotDevicePropertySetResultDTO> execute(WriteTask task, List<WriteItem> items) {
        Map<String, IotDevicePropertySetResultDTO> results = new LinkedHashMap<>();
        // 1. 排队期间已超时、会话失效或重连、设备配置变化：不再读取、写入
        IotDevicePropertySetResultDTO aliveFailure = checkAliveOnContext(task, "排队超过写入超时，未写入");
        if (aliveFailure != null) {
            failAll(identifiers(items), results, aliveFailure);
            return results;
        }

        // 2. 读取节点的 DataType、ValueRank、AccessLevel、UserAccessLevel
        List<DataValue> attributes;
        try {
            attributes = readAttributes(task.client, items, task.maxNodesPerRead, task.deadlineNanos);
        } catch (Exception e) {
            failAll(identifiers(items), results, toFailure(e, OPC_UA_WRITE_ATTRIBUTE_READ_FAILED, "读取节点属性超时，未写入"));
            return results;
        }

        // 3. 逐个属性校验节点属性、按实际的 DataType 转换值
        List<WriteItem> writeItems = new ArrayList<>();
        List<WriteValue> writeValues = new ArrayList<>();
        int attributeCount = WRITE_CHECK_ATTRIBUTES.size();
        for (int i = 0; i < items.size(); i++) {
            WriteItem item = items.get(i);
            try {
                Variant variant = convertValue(item, attributes.subList(i * attributeCount, (i + 1) * attributeCount));
                writeItems.add(item);
                writeValues.add(new WriteValue(item.nodeId, AttributeId.Value.uid(), null, DataValue.valueOnly(variant)));
            } catch (WriteItemException e) {
                results.put(item.identifier, e.result);
            }
        }

        // 4. 按 MaxNodesPerWrite 分批写入：每批发出之前再核对；服务端返回的 StatusCode 逐项原样带回
        for (int start = 0; start < writeItems.size(); start += task.maxNodesPerWrite) {
            int end = Math.min(start + task.maxNodesPerWrite, writeItems.size());
            List<WriteItem> chunkItems = writeItems.subList(start, end);
            // 4.1 核对：失败时本批及之后的批次都不再写入（已写入批次的结果保留）
            aliveFailure = checkAliveOnContext(task, "读取节点属性后已超过写入超时，未写入");
            if (aliveFailure != null) {
                failAll(identifiers(writeItems.subList(start, writeItems.size())), results, aliveFailure);
                break;
            }
            // 4.2 发出之前在 worker 上直接核对连接代数：底层会话已失效或重连、回调还没切回 context 时，不写旧会话 / 旧命名空间
            if (isConnectionChanged(task)) {
                failAll(identifiers(writeItems.subList(start, writeItems.size())), results,
                        IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_SESSION_EXPIRED));
                break;
            }
            // 4.3 写入：缺失的 StatusCode 只记该项失败（结果未知）
            try {
                StatusCode[] statusCodes = task.client.writeAsync(new ArrayList<>(writeValues.subList(start, end)))
                        .get(remainingMillis(task.deadlineNanos), TimeUnit.MILLISECONDS).getResults();
                for (int i = 0; i < chunkItems.size(); i++) {
                    StatusCode statusCode = statusCodes != null && i < statusCodes.length ? statusCodes[i] : null;
                    results.put(chunkItems.get(i).identifier, statusCode != null ? toResult(statusCode)
                            : IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_FAILED, "写入结果缺失，结果未知"));
                }
            } catch (Exception e) {
                failAll(identifiers(chunkItems), results, toFailure(e, OPC_UA_WRITE_FAILED, "等待写入结果超时，结果未知"));
            }
        }
        return results;
    }

    /**
     * 读取每个节点写入前需要的属性：按本次激活的 maxNodesPerRead 分批，每批的结果数必须与请求数一致
     *
     * @return 与 items × {@link #WRITE_CHECK_ATTRIBUTES} 顺序一致的结果
     */
    private static List<DataValue> readAttributes(OpcUaClient client, List<WriteItem> items, int maxNodesPerRead,
                                                  long deadlineNanos) throws Exception {
        List<ReadValueId> readValueIds = new ArrayList<>(items.size() * WRITE_CHECK_ATTRIBUTES.size());
        for (WriteItem item : items) {
            for (AttributeId attributeId : WRITE_CHECK_ATTRIBUTES) {
                readValueIds.add(new ReadValueId(item.nodeId, attributeId.uid(), null, null));
            }
        }
        List<DataValue> attributes = new ArrayList<>(readValueIds.size());
        for (List<ReadValueId> chunk : CollUtil.split(readValueIds, maxNodesPerRead)) {
            DataValue[] values = client.readAsync(0, TimestampsToReturn.Neither, chunk)
                    .get(remainingMillis(deadlineNanos), TimeUnit.MILLISECONDS).getResults();
            if (values == null || values.length != chunk.size()) {
                throw new IllegalStateException("读取结果数与请求数不一致");
            }
            attributes.addAll(Arrays.asList(values));
        }
        return attributes;
    }

    /**
     * 校验节点属性并转换值
     *
     * @param item       属性
     * @param attributes 该节点的 DataType、ValueRank、AccessLevel、UserAccessLevel
     * @return 写入的值
     * @throws WriteItemException 节点不可写、类型不支持、值不合法
     */
    private static Variant convertValue(WriteItem item, List<DataValue> attributes) throws WriteItemException {
        // 1. 属性读取失败（例如节点不存在、不是变量节点）：带回服务端的 StatusCode
        for (int i = 0; i < attributes.size(); i++) {
            StatusCode statusCode = attributes.get(i).getStatusCode();
            if (statusCode != null && !statusCode.isGood()) {
                throw new WriteItemException(IotDevicePropertySetResultDTO.failureWithStatus(OPC_UA_WRITE_ATTRIBUTE_READ_FAILED,
                        formatStatusCode(statusCode), WRITE_CHECK_ATTRIBUTES.get(i).name() + " " + describeStatusCode(statusCode)));
            }
        }
        Object dataTypeId = attributes.get(0).getValue().getValue();
        Object valueRank = attributes.get(1).getValue().getValue();
        Object accessLevel = attributes.get(2).getValue().getValue();
        Object userAccessLevel = attributes.get(3).getValue().getValue();
        // 2. 访问级别：AccessLevel、UserAccessLevel 都要包含 CurrentWrite
        if (!isCurrentWrite(accessLevel)) {
            throw new WriteItemException(IotDevicePropertySetResultDTO.failureWithStatus(OPC_UA_WRITE_NODE_NOT_WRITABLE,
                    formatStatusCode(new StatusCode(StatusCodes.Bad_NotWritable)), "AccessLevel 不包含 CurrentWrite"));
        }
        if (!isCurrentWrite(userAccessLevel)) {
            throw new WriteItemException(IotDevicePropertySetResultDTO.failureWithStatus(OPC_UA_WRITE_NODE_NOT_WRITABLE,
                    formatStatusCode(new StatusCode(StatusCodes.Bad_UserAccessDenied)), "当前用户没有写权限（UserAccessLevel）"));
        }
        // 3. 只支持标量：ValueRank 为 -1（标量）、-2（任意）、-3（标量或一维）时写标量，其它（数组、未定义的负值）不支持
        if (!(valueRank instanceof Integer) || !SCALAR_VALUE_RANKS.contains(valueRank)) {
            throw new WriteItemException(IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_DATA_TYPE_NOT_SUPPORTED,
                    "非标量（ValueRank=" + valueRank + "）"));
        }
        // 4. 数据类型：只支持内置的标量类型
        OpcUaDataType dataType = dataTypeId instanceof NodeId
                ? IotOpcUaValueConverter.resolveWritableDataType((NodeId) dataTypeId) : null;
        if (dataType == null) {
            throw new WriteItemException(IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_DATA_TYPE_NOT_SUPPORTED,
                    dataTypeId instanceof NodeId ? ((NodeId) dataTypeId).toParseableString() : String.valueOf(dataTypeId)));
        }
        // 5. 转换值：类型不匹配、超出范围、需要截断时失败
        try {
            return IotOpcUaValueConverter.toVariant(item.value, dataType);
        } catch (IllegalArgumentException e) {
            throw new WriteItemException(IotDevicePropertySetResultDTO.failure(PROPERTY_SET_VALUE_INVALID,
                    dataType.name() + " " + e.getMessage()));
        }
    }

    /**
     * AccessLevel / UserAccessLevel 是否包含 CurrentWrite（读取到的值为 UByte）
     */
    private static boolean isCurrentWrite(Object accessLevel) {
        return accessLevel instanceof UByte && new AccessLevelType((UByte) accessLevel).getCurrentWrite();
    }

    /**
     * 在 worker 上切回 context 执行 {@link #checkAlive}，阻塞等待结果（最多等到截止时间）
     * <p>
     * 会话的设备绑定、配置版本只在 context 上读写，所以核对必须在 context 上执行；context 已关闭、没有及时执行时按超时处理
     *
     * @param task          写入任务
     * @param timeoutDetail 超时时的说明
     * @return 失败结果；仍然有效时返回 null
     */
    private IotDevicePropertySetResultDTO checkAliveOnContext(WriteTask task, String timeoutDetail) {
        CompletableFuture<IotDevicePropertySetResultDTO> future = new CompletableFuture<>();
        try {
            long remaining = remainingMillis(task.deadlineNanos);
            context.runOnContext(v -> future.complete(checkAlive(task, timeoutDetail)));
            return future.get(remaining, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_TIMEOUT, timeoutDetail);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_FAILED, "等待核对时被中断，未写入");
        } catch (Exception e) {
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_FAILED, StrUtil.blankToDefault(e.getMessage(),
                    e.getClass().getSimpleName()));
        }
    }

    /**
     * 核对写入任务是否仍然有效（context）：
     * 1. 截止时间：已超时 → 超时；
     * 2. 会话：已关闭、未激活、代数或客户端变化 → 会话已过期（底层断线、重连但回调尚未切回 context 的窗口，
     *    由 {@link #execute} 在每批 writeAsync 前直接核对连接代数）；
     * 3. 设备配置：设备不再绑定到该会话（解绑、端点 / 安全配置 / 凭据变化换了会话）、绑定对象被替换，
     *    或配置版本 epoch 变化（点位启用 / 禁用 / 删除、可写、NodeId 等变化）→ 配置已过期
     *
     * @param task          写入任务
     * @param timeoutDetail 超时时的说明
     * @return 失败结果；仍然有效时返回 null
     */
    IotDevicePropertySetResultDTO checkAlive(WriteTask task, String timeoutDetail) {
        if (System.nanoTime() - task.deadlineNanos >= 0) {
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_TIMEOUT, timeoutDetail);
        }
        IotOpcUaClientSession session = task.session;
        if (session.isClosed() || !session.isActive() || session.getGeneration() != task.generation
                || session.getClient() != task.client) {
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_SESSION_EXPIRED);
        }
        if (sessionManager.getDeviceSession(task.deviceId) != session
                || session.getDevices().get(task.deviceId) != task.binding || task.binding.getEpoch() != task.epoch) {
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_CONFIG_EXPIRED);
        }
        return null;
    }

    /**
     * 底层会话是否在提交之后失效或重新激活：连接代数由 Milo 回调立即更新（任意线程可读），与提交时本次激活的连接代数比较
     *
     * @param task 写入任务
     * @return 是否已变化
     */
    private static boolean isConnectionChanged(WriteTask task) {
        return task.session.getConnectionEpoch().get() != task.connectionEpoch;
    }

    /**
     * 距离截止时间的剩余毫秒数，作为 Milo 请求的等待时间
     *
     * @throws TimeoutException 已经超过截止时间
     */
    private static long remainingMillis(long deadlineNanos) throws TimeoutException {
        long remaining = TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
        if (remaining <= 0) {
            throw new TimeoutException();
        }
        return remaining;
    }

    /**
     * 服务端的 StatusCode 转为结果：Good 为成功，其它（包括 Uncertain）为失败，十六进制原样带回
     */
    private static IotDevicePropertySetResultDTO toResult(StatusCode statusCode) {
        if (statusCode.isGood()) {
            return IotDevicePropertySetResultDTO.success(formatStatusCode(statusCode));
        }
        return IotDevicePropertySetResultDTO.failureWithStatus(OPC_UA_WRITE_STATUS_BAD,
                formatStatusCode(statusCode), describeStatusCode(statusCode));
    }

    /**
     * 读取、写入的异常转为结果：超时 → 超时；服务端的错误（UaException）→ 带回 StatusCode；其它 → errorCode
     */
    private static IotDevicePropertySetResultDTO toFailure(Exception e, ErrorCode errorCode, String timeoutDetail) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        if (e instanceof TimeoutException) {
            return IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_TIMEOUT, timeoutDetail);
        }
        Throwable cause = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
        if (cause instanceof UaException) {
            StatusCode statusCode = ((UaException) cause).getStatusCode();
            return IotDevicePropertySetResultDTO.failureWithStatus(errorCode, formatStatusCode(statusCode),
                    describeStatusCode(statusCode));
        }
        return IotDevicePropertySetResultDTO.failure(errorCode, StrUtil.blankToDefault(cause.getMessage(),
                cause.getClass().getSimpleName()));
    }

    /**
     * StatusCode 格式化为 8 位十六进制（例如 0x803B0000），与 OPC UA 规范、服务端日志的写法一致
     */
    static String formatStatusCode(StatusCode statusCode) {
        return String.format("0x%08X", statusCode.getValue());
    }

    /**
     * StatusCode 的可读描述：名称（十六进制），例如 Bad_NotWritable(0x803B0000)；未知的状态码名称为 Unknown
     */
    static String describeStatusCode(StatusCode statusCode) {
        String name = StatusCodes.lookup(statusCode.getValue()).map(nameAndDesc -> nameAndDesc[0]).orElse("Unknown");
        return name + "(" + formatStatusCode(statusCode) + ")";
    }

    // ========== 回复 ==========

    /**
     * 回复属性设置（在 context 上调用）：补齐缺失的结果后切到 worker，由 {@link IotDeviceMessageService} 发送回复
     *
     * @param request 属性设置请求
     * @param results 每个属性的结果；整条消息失败时为 null
     * @param failure 整条消息失败的错误码；逐项回复时为 null
     * @param params  错误码文案的参数
     */
    private void reply(IotDeviceMessage request, Map<String, IotDevicePropertySetResultDTO> results,
                       ErrorCode failure, Object... params) {
        ErrorCode error = failure != null
                ? new ErrorCode(failure.getCode(), StrUtil.format(failure.getMsg(), params)) : null;
        if (results != null) {
            // 兜底：每个属性都要有结果
            results.replaceAll((identifier, result) -> result != null ? result
                    : IotDevicePropertySetResultDTO.failure(OPC_UA_WRITE_FAILED, "结果缺失"));
            long failed = results.values().stream().filter(result -> !result.succeeded()).count();
            log.info("[reply][设备 {} 属性设置({}) 完成，{} 个属性，{} 个失败]", request.getDeviceId(),
                    request.getRequestId(), results.size(), failed);
        } else {
            log.warn("[reply][设备 {} 属性设置({}) 失败：{}]", request.getDeviceId(), request.getRequestId(), error.getMsg());
        }
        vertx.executeBlocking(() -> {
            messageService.sendPropertySetReply(request, results, error, serverId);
            return null;
        }, false).onFailure(e -> log.error("[reply][设备 {} 属性设置({}) 回复失败]", request.getDeviceId(),
                request.getRequestId(), e));
    }

    /**
     * 把这些属性都记为同一个失败结果
     */
    private static void failAll(Collection<String> identifiers, Map<String, IotDevicePropertySetResultDTO> results,
                                IotDevicePropertySetResultDTO failure) {
        identifiers.forEach(identifier -> results.put(identifier, failure));
    }

    /**
     * 属性标识符列表（保持顺序）
     */
    private static List<String> identifiers(List<WriteItem> items) {
        List<String> identifiers = new ArrayList<>(items.size());
        items.forEach(item -> identifiers.add(item.identifier));
        return identifiers;
    }

    /**
     * 写入任务：提交时的会话、绑定、配置版本、会话代数、连接代数、客户端、本次激活的上限和截止时间
     */
    @AllArgsConstructor
    static class WriteTask {

        private final Long deviceId;
        private final IotOpcUaClientSession session;
        private final DeviceBinding binding;
        private final long epoch;
        private final long generation;
        /**
         * 提交时本次激活对应的连接代数
         */
        private final long connectionEpoch;
        private final OpcUaClient client;
        private final int maxNodesPerRead;
        private final int maxNodesPerWrite;
        private final long deadlineNanos;

    }

    /**
     * 通过校验、待写入的属性
     */
    @AllArgsConstructor
    static class WriteItem {

        private final String identifier;
        private final NodeId nodeId;
        private final Object value;

    }

    /**
     * 单个属性的失败：只用于 {@link #convertValue} 返回失败结果
     */
    private static class WriteItemException extends Exception {

        private final transient IotDevicePropertySetResultDTO result;

        WriteItemException(IotDevicePropertySetResultDTO result) {
            super(result.getMsg(), null, false, false);
            this.result = result;
        }

    }

}
