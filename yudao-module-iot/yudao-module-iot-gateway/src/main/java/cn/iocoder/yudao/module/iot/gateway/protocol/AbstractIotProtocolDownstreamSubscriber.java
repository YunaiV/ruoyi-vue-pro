package cn.iocoder.yudao.module.iot.gateway.protocol;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.dto.IotDownstreamEncodeRespDTO;
import io.vertx.core.Future;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.RAW_NOT_SUPPORTED;

/**
 * IoT 协议下行消息订阅者抽象类
 *
 * 负责接收来自消息总线的下行消息，并委托给子类进行业务处理
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Slf4j
public abstract class AbstractIotProtocolDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotProtocol protocol;

    private final IotMessageBus messageBus;

    /**
     * 有排队 / 执行中脚本指令的设备：设备编号 → 待执行的指令（第一条已在 worker 上执行时，队列可为空）
     * <p>
     * 总线的消费线程与 worker 都会访问，用它自身加锁；设备的指令全部执行完后移除
     */
    private final Map<Long, Deque<Runnable>> deviceCommandQueues = new HashMap<>();

    /**
     * @return 协议类型：encode 的入参与字节规则按协议区分
     */
    public IotProtocolTypeEnum getProtocolType() {
        return protocol.getType();
    }

    /**
     * @return 是否支持透传下发：不支持时不执行编码脚本，指令直接走标准路径
     */
    public boolean supportsRawDownstream() {
        return false;
    }

    /**
     * @return 本协议实例的 serverId（可信来源）：回复设备响应时显式传入
     */
    public String getServerId() {
        return protocol.getServerId();
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
    }

    /**
     * 保证点对点消费，需要保证独立的 Group，所以使用 Topic 作为 Group
     */
    @Override
    public String getGroup() {
        return getTopic();
    }

    @Override
    public void start() {
        messageBus.register(this);
        log.info("[start][{} 下行消息订阅成功，Topic：{}]", protocol.getType().name(), getTopic());
    }

    @Override
    public void stop() {
        messageBus.unregister(this);
        // 丢弃排队中的指令：执行中的那一条照常结束，之后不再执行
        synchronized (deviceCommandQueues) {
            deviceCommandQueues.clear();
        }
        log.info("[stop][{} 下行消息订阅已停止，Topic：{}]", protocol.getType().name(), getTopic());
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        log.debug("[onMessage][接收到下行消息, messageId: {}, method: {}, deviceId: {}]",
                message.getId(), message.getMethod(), message.getDeviceId());
        try {
            // 1. 校验
            String method = message.getMethod();
            if (StrUtil.isBlank(method)) {
                log.warn("[onMessage][消息方法为空, messageId: {}, deviceId: {}]",
                        message.getId(), message.getDeviceId());
                return;
            }

            // 2.1 透传下发（raw.push）：字节在 raw 字段，不经过脚本，直接交给协议写出（功能开关关闭时也照常）；
            //     协议自己的字节规则（例如请求 PDU）由协议写出前校验
            if (IotDeviceMessageMethodEnum.RAW_PUSH.getMethod().equals(method)
                    && !IotDeviceMessageUtils.isReplyMessage(message)) {
                writeRawMessage(message, IotDeviceMessageUtils.decodeRaw(message.getRaw(),
                        IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES), null);
                return;
            }
            // 2.2 指令（property.set / service.invoke）：功能开关开启、协议支持透传下发时，按设备排队，切到协议的 worker 上
            //     按产品的数据解析脚本 encode（加载设备信息、执行脚本会阻塞），再透传写出或走标准路径。
            //     同一设备的指令（包括 encode 后走标准路径的）按收到的顺序执行，不同设备之间并行
            IotGatewayProperties.ScriptProperties script = SpringUtil.getBean(IotGatewayProperties.class).getScript();
            if (script.isEnabled() && supportsRawDownstream() && IotDeviceMessageUtils.isCommandRequest(message)) {
                // 截止时间从收到（按设备排队之前）开始计算
                IotDeviceMessageService deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
                long receivedNanos = System.nanoTime();
                enqueueDeviceCommand(message, () -> handleScriptCommand(deviceMessageService, message, receivedNanos),
                        script.getMaxPendingCommandsPerDevice());
                return;
            }
            // 2.3 其它（OTA、影子、配置、回复等，功能开关关闭，或协议不支持透传下发）：标准路径
            handleMessage(message);
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败, messageId: {}, method: {}, deviceId: {}]",
                    message.getId(), message.getMethod(), message.getDeviceId(), e);
        }
    }

    /**
     * 按设备排队脚本指令：同一设备按提交顺序在 worker 上逐条执行，不同设备之间并行；
     * 每个设备最多排队 maxPending 条（不含执行中的一条），超过时丢弃并记日志
     *
     * @param message    下行指令
     * @param task       阻塞任务
     * @param maxPending 每个设备最多排队的指令数
     */
    private void enqueueDeviceCommand(IotDeviceMessage message, Runnable task, int maxPending) {
        // 1. 没有设备编号（不应出现）：无法保序，直接执行
        Long deviceId = message.getDeviceId();
        if (deviceId == null) {
            executeCommands(null, new ArrayDeque<>(Collections.singletonList(task)));
            return;
        }
        ArrayDeque<Runnable> tasks;
        synchronized (deviceCommandQueues) {
            // 2. 设备还有排队 / 执行中的指令：排在它们之后，由正在执行的 worker 依次执行；队列已满时丢弃
            Deque<Runnable> pending = deviceCommandQueues.get(deviceId);
            if (pending != null) {
                if (pending.size() >= maxPending) {
                    log.error("[enqueueDeviceCommand][设备({}) 待处理的下行指令已达上限 {}，丢弃 messageId: {}, method: {}]",
                            deviceId, maxPending, message.getId(), message.getMethod());
                } else {
                    pending.add(task);
                }
                return;
            }
            // 3. 设备没有在途的指令：新建队列，交给一个 worker 执行
            tasks = new ArrayDeque<>();
            tasks.add(task);
            deviceCommandQueues.put(deviceId, tasks);
        }
        executeCommands(deviceId, tasks);
    }

    /**
     * 在一个 worker 上依次执行设备的指令队列，直到队列为空（或已被 {@link #stop()} 清空）；单条失败只记日志，不影响后续指令
     *
     * @param deviceId 设备编号（为 null 时只执行给定的任务）
     * @param tasks    设备的指令队列
     */
    private void executeCommands(Long deviceId, Deque<Runnable> tasks) {
        try {
            protocol.getVertx().executeBlocking(() -> {
                while (true) {
                    // 1. 取下一条：队列空了就移除（之后的指令新建队列）；队列已被 stop 清掉或换成新的一代，不再执行
                    Runnable task;
                    synchronized (deviceCommandQueues) {
                        task = tasks.poll();
                        if (task == null) {
                            if (deviceId != null) {
                                deviceCommandQueues.remove(deviceId, tasks);
                            }
                            return null;
                        }
                        // 已停止
                        if (deviceId != null && deviceCommandQueues.get(deviceId) != tasks) {
                            return null;
                        }
                    }
                    // 2. 执行：单条失败只记日志，不影响后续指令
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error("[executeCommands][设备({}) 处理下行指令失败]", deviceId, e);
                    }
                }
            }, false).onFailure(e -> {
                // 3. 提交到 worker 失败：丢弃排队的指令，移除队列
                log.error("[executeCommands][设备({}) 下行指令无法提交到 worker，丢弃排队的指令]", deviceId, e);
                removeDeviceCommandQueue(deviceId, tasks);
            });
        } catch (Exception e) {
            // Vertx 已关闭时 worker 池会同步拒绝
            log.error("[executeCommands][设备({}) 下行指令无法提交到 worker，丢弃排队的指令]", deviceId, e);
            removeDeviceCommandQueue(deviceId, tasks);
        }
    }

    /**
     * 移除设备的指令队列：只移除给定的这一代（已被 stop 清掉或换成新一代时不影响新队列）；排队中的指令随之丢弃
     *
     * @param deviceId 设备编号（为 null 时没有队列，直接返回）
     * @param tasks    设备的指令队列
     */
    private void removeDeviceCommandQueue(Long deviceId, Deque<Runnable> tasks) {
        if (deviceId == null) {
            return;
        }
        synchronized (deviceCommandQueues) {
            deviceCommandQueues.remove(deviceId, tasks);
        }
    }

    /**
     * 在 worker 上处理脚本指令：按 encode 的结果走标准路径、透传写出，或者不下发（设备消息 Service 已记日志）
     *
     * @param deviceMessageService 设备消息 Service（在总线消费线程获取）
     * @param message              下行指令
     * @param receivedNanos        收到指令的时刻（System.nanoTime）：按设备排队之前取得；设备消息 Service 据此计算截止，
     *                             返回结果之前已核对，拿到结果立即下发
     */
    private void handleScriptCommand(IotDeviceMessageService deviceMessageService, IotDeviceMessage message,
                                     long receivedNanos) {
        // 1. encode：null 表示不下发（设备消息 Service 已记日志）
        IotDownstreamEncodeRespDTO result = deviceMessageService.encodeDownstreamCommand(message, getProtocolType(),
                receivedNanos);
        if (result == null) {
            return;
        }
        // 2.1 走标准路径：与未启用脚本一致，由子类按协议的标准格式下发
        if (result.isStandard()) {
            handleMessage(message);
            return;
        }
        // 2.2 透传写出 encode 的字节，带上 encode 时固定的脚本（设备响应用它解析）
        writeRawMessage(message, result.getBytes(), result.getSource());
    }

    /**
     * 处理下行消息
     *
     * @param message 下行消息
     */
    protected abstract void handleMessage(IotDeviceMessage message);

    /**
     * 透传写出：交给协议写出（{@link #handleRawMessage}），写出失败只记日志
     *
     * @param message 下行消息（raw.push，或 encode 的 property.set / service.invoke）
     * @param bytes   要写出的字节
     * @param source  encode 时使用的脚本（固定 hash）；raw.push 为 null
     */
    private void writeRawMessage(IotDeviceMessage message, byte[] bytes, IotScriptSource source) {
        Future<Void> write = handleRawMessage(message, bytes, source);
        if (write == null) {
            log.warn("[writeRawMessage][下行({}) 设备不在线或写入失败]", message.getRequestId());
            return;
        }
        write.onFailure(e -> log.warn("[writeRawMessage][下行({}) 写出失败：{}]", message.getRequestId(), e.getMessage()));
    }

    /**
     * 透传写出。约定：只返回 Future，自己不发消息给 biz；设备的实际响应由协议经 {@link #replyRawResult} 回复
     * <p>
     * 失败 = ServiceException(ErrorCode)，只记日志；协议自己的字节规则（例如请求 PDU）在写出之前校验，不合法返回失败。
     * 成功：一般为本地异步写出已完成（MQTT 为 publish 已提交，晚到的写出失败由协议记日志），不代表设备已确认；有请求-响应事务的协议，提交事务之前的失败返回失败 Future，
     * 提交之后 Future 在事务终态（响应 / 超时 / 断线 / 写失败）成功完成，网关判定的失败只记日志
     *
     * @param message 下行消息（raw.push，或 encode 的 property.set / service.invoke）
     * @param bytes   要写出的字节
     * @param source  encode 时使用的脚本（固定 hash）；raw.push 为 null
     * @return 写出结果
     */
    public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes, IotScriptSource source) {
        return Future.failedFuture(exception(RAW_NOT_SUPPORTED));
    }

    /**
     * 回复透传下行的设备实际响应：切到协议的 worker，交给设备消息 Service 回复给 biz
     * <p>
     * 只用于设备的实际响应（包括协议的异常响应）；网关判定的失败（超时、断线、写失败等）由协议只记日志，
     * 不调用本方法，也不给 biz 构造回复
     *
     * @param request     原请求
     * @param source      encode 时固定的脚本；raw.push 为 null
     * @param payload     设备的响应字节
     * @param deviceError 协议判定的设备错误（例如异常响应）；正常响应为 null
     */
    protected final void replyRawResult(IotDeviceMessage request, IotScriptSource source,
                                        byte[] payload, ErrorCode deviceError) {
        String serverId = getServerId();
        protocol.getVertx().executeBlocking(() -> {
            SpringUtil.getBean(IotDeviceMessageService.class).sendRawReply(request, source, payload, deviceError, serverId);
            return null;
        }, false).onFailure(e -> log.error("[replyRawResult][下行({}) 回复失败]", request.getRequestId(), e));
    }

}
