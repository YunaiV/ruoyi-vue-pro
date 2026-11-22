package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.script.IotScriptException;
import cn.iocoder.yudao.module.iot.core.script.IotScriptExecutor;
import cn.iocoder.yudao.module.iot.core.script.IotScriptUtils;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.core.script.rhino.IotRhinoScriptExecutor;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.dto.IotDownstreamEncodeRespDTO;
import cn.iocoder.yudao.module.iot.gateway.service.device.spool.IotDeviceMessageSpoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;

/**
 * IoT 设备消息 Service 实现类
 * <p>
 * 透传（raw）上行、下行指令的 encode、透传回复会阻塞（加载设备信息、执行脚本、发送总线），
 * 由各协议在自己的 Vert.x worker 上调用；本类只做编排，不切线程，也不写设备（下行由协议的订阅者按 encode 结果下发）
 *
 * @author 芋道源码
 */
@Service("iotGatewayDeviceMessageServiceImpl") // 显式 bean 名：避免与 biz 的 IotDeviceMessageServiceImpl 同名冲突
@Slf4j
public class IotDeviceMessageServiceImpl implements IotDeviceMessageService {

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;
    @Resource
    private IotDeviceMessageSpoolService deviceMessageSpoolService;

    @Resource
    private IotMessageSerializerManager messageSerializerManager;

    @Resource
    private IotGatewayProperties gatewayProperties;

    /**
     * 数据解析脚本执行器：JS 线程池与计时器（执行预算兜底、卡住扫描）由它自己创建和关闭
     */
    private IotScriptExecutor scriptExecutor;

    @PostConstruct
    public void init() {
        IotGatewayProperties.ScriptProperties script = gatewayProperties.getScript();
        scriptExecutor = new IotRhinoScriptExecutor(new IotRhinoScriptExecutor.Options()
                .setExecBudgetMillis(script.getExecBudget().toMillis())
                .setMaxInstructions(script.getMaxInstructions()));
        // 功能开关关闭：不预热 JS 线程
        if (script.isEnabled()) {
            scriptExecutor.warmUp();
        }
    }

    @PreDestroy
    public void destroy() {
        scriptExecutor.shutdown();
    }

    @Override
    public byte[] serializeDeviceMessage(IotDeviceMessage message,
                                         String productKey, String deviceName) {
        // 1.1 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }
        // 1.2 获取序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(device.getSerializeType());
        Assert.notNull(serializeType, "设备序列化类型不能为空");

        // 2. 序列化消息
        return serializeDeviceMessage(message, serializeType);
    }

    @Override
    public byte[] serializeDeviceMessage(IotDeviceMessage message,
                                         IotSerializeTypeEnum serializeType) {
        // 1. 获取序列化器
        IotMessageSerializer serializer = messageSerializerManager.get(serializeType);
        if (serializer == null) {
            throw new IllegalArgumentException(StrUtil.format("序列化器({}) 不存在", serializeType));
        }

        // 2. 序列化消息
        return serializer.serialize(message);
    }

    @Override
    public IotDeviceMessage deserializeDeviceMessage(byte[] bytes,
                                                     String productKey, String deviceName) {
        // 1.1 获取设备信息
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }
        // 1.2 获取序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(device.getSerializeType());
        Assert.notNull(serializeType, "设备序列化类型不能为空");

        // 2. 反序列化消息
        return deserializeDeviceMessage(bytes, serializeType);
    }

    @Override
    public IotDeviceMessage deserializeDeviceMessage(byte[] bytes, IotSerializeTypeEnum serializeType) {
        // 1. 获取序列化器
        IotMessageSerializer serializer = messageSerializerManager.get(serializeType);
        if (serializer == null) {
            throw new IllegalArgumentException(StrUtil.format("序列化器({}) 不存在", serializeType));
        }

        // 2. 反序列化消息
        return serializer.deserialize(bytes);
    }

    @Override
    public void sendDeviceMessage(IotDeviceMessage message,
                                  String productKey, String deviceName, String serverId, IotProtocolTypeEnum protocol) {
        // 0. 标准 JSON 路径不接受透传方法：thing.raw.* 只能由网关的透传入口创建
        if (IotDeviceMessageUtils.isRawMethod(message.getMethod())) {
            throw exception(DEVICE_MESSAGE_RAW_METHOD_RESERVED);
        }
        // 1. 获取设备信息
        IotDeviceRespDTO device = getDevice(productKey, deviceName);

        // 2. 发送消息：raw 只能由服务端可信赋值，设备自己带上的一律清空
        message.setRaw(null);
        sendDeviceMessage(message, device, serverId, protocol);
    }

    @Override
    public void sendDeviceStateMessage(IotDeviceMessage message, String productKey, String deviceName, String serverId) {
        // 1.1 只接受状态消息：其它消息的发送失败不能吞掉
        Assert.equals(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod(), message.getMethod(), "只能发送状态消息");
        // 1.2 获取设备信息：设备不存在时照常抛出
        IotDeviceRespDTO device = getDevice(productKey, deviceName);

        // 2. 发送消息（不走离线暂存）：离线暂存已启用时，消息总线不可用只记日志，不阻断调用方（例如说设备认证）；
        //    未启用时与原来一致，失败照常抛出
        try {
            sendDeviceMessage(message, device, serverId, null);
        } catch (Exception ex) {
            if (!deviceMessageSpoolService.isEnabled()) {
                throw ex;
            }
            log.warn("[sendDeviceStateMessage][设备({}/{}) 状态消息发送消息总线失败，不阻断：{}]",
                    productKey, deviceName, ex.getMessage());
        }
    }

    /**
     * 发送消息：编号、时间由服务端生成，身份由可信的 device 注入；不清空 raw（调用方是网关的可信路径）
     *
     * @param message  消息
     * @param device   设备信息
     * @param serverId 设备连接的 serverId
     * @param protocol 协议：协议入口的上行数据可以走离线暂存；为空时直发
     */
    private void sendDeviceMessage(IotDeviceMessage message, IotDeviceRespDTO device, String serverId,
                                   IotProtocolTypeEnum protocol) {
        // 1.1 补全消息：接收时刻只取样一次，reportTime 与离线暂存的 spoolTime 一致；
        //     spoolTime 只由离线暂存赋值，设备、脚本带上的一律清空
        long now = System.currentTimeMillis();
        message.setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTimeUtil.of(now))
                .setDeviceId(device.getId()).setTenantId(device.getTenantId()).setServerId(serverId)
                .setSpoolTime(null);
        // 1.2 特殊：如果设备没有指定 requestId，则使用 messageId
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(message.getId());
        }

        // 2.1 情况一：协议入口的属性、事件、批量上报，经离线暂存发送（消息总线不可用时写入暂存，暂存失败时抛出）
        if (deviceMessageSpoolService.isSpoolableMessage(message, protocol)) {
            deviceMessageSpoolService.sendSpoolableMessage(message, now);
            return;
        }
        // 2.2 情况二：其它消息，直接发送到消息总线
        deviceMessageProducer.sendDeviceMessage(message);
    }

    // ========== 透传上行 ==========

    @Override
    public String sendRawMessage(byte[] payload, String productKey, String deviceName, String serverId,
                                 IotProtocolTypeEnum protocol, long receivedNanos) {
        // 截止从协议收到时开始计算：排队的时间也计入
        long deadlineNanos = buildDeadlineNanos(receivedNanos);
        // 1.1 校验载荷长度
        if (payload == null || payload.length < 1 || payload.length > IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES) {
            throw invalidParamException("raw 载荷长度必须在 1~{} 字节之间", IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES);
        }
        // 1.2 获取设备信息
        IotDeviceRespDTO device = getDevice(productKey, deviceName);
        String raw = IotDeviceMessageUtils.encodeRaw(payload);
        // 1.3 排队已超过截止：不执行脚本，不投递，只记错误日志
        if (System.nanoTime() >= deadlineNanos) {
            log.error("[sendRawMessage][设备({}/{}) 排队超过截止时间，不投递]", productKey, deviceName);
            throw exception(SCRIPT_TIMEOUT);
        }

        // 2.1 功能开关关闭：不执行脚本，只记录原始消息
        if (!gatewayProperties.getScript().isEnabled()) {
            return sendRawPostMessage(device, serverId, raw);
        }
        // 2.2 产品脚本（随设备信息缓存）：停用或从未配置时只记录原始消息
        if (!Boolean.TRUE.equals(device.getScriptEnabled())) {
            return sendRawPostMessage(device, serverId, raw);
        }
        // 2.3 已启用但内容为空（配置不合法）：不投递，只记错误日志
        if (StrUtil.isBlank(device.getScriptContent())) {
            log.error("[sendRawMessage][设备({}/{}) 数据解析脚本已启用但内容为空，不投递]", productKey, deviceName);
            throw exception(SCRIPT_EXECUTE_FAILED, "已启用但脚本内容为空");
        }

        // 3.1 执行 decode：失败时不投递，只记错误日志
        IotScriptSource source = IotScriptSource.of(device.getProductId(), device.getScriptContent());
        String ctx = IotScriptUtils.buildDecodeContext(protocol.getType(), device.getProductKey(),
                device.getDeviceName(), System.currentTimeMillis(), payload, null);
        IotScriptResult result = scriptExecutor.execute(source, IotScriptUtils.FUNCTION_DECODE, ctx, deadlineNanos);
        if (!result.isSuccess()) {
            log.error("[sendRawMessage][设备({}/{}) 脚本执行失败({})：{}，不投递]", productKey, deviceName,
                    result.getStatus(), result.getMessage());
            throw scriptException(result.getStatus(), result.getMessage());
        }
        // 3.2 校验输出：不合法时不投递，只记错误日志
        IotDeviceMessage message;
        try {
            message = IotScriptUtils.validateDecode(result.getJson(), null);
        } catch (IotScriptException e) {
            log.error("[sendRawMessage][设备({}/{}) 脚本输出不合法：{}，不投递]", productKey, deviceName, e.getMessage());
            throw exception(SCRIPT_OUTPUT_INVALID, StrUtil.maxLength(e.getMessage(), 200));
        }
        // 3.3 投递之前核对截止：迟到的结果（包括 decode 返回 null）不投递，只记错误日志
        if (System.nanoTime() >= deadlineNanos) {
            log.error("[sendRawMessage][设备({}/{}) 脚本完成时已超过截止时间，不投递]", productKey, deviceName);
            throw exception(SCRIPT_TIMEOUT);
        }
        // 3.4 decode 返回 null：没有业务消息，只记录原始消息
        if (message == null) {
            return sendRawPostMessage(device, serverId, raw);
        }

        // 4. 投递解析出的业务消息，带上原始字节
        // 注意：截止只核对到这里。之后的直发、写入离线暂存可能晚于截止完成：HTTP 已先应答超时（101）时，消息仍会投递或补传，
        // 设备重试会产生重复；写入离线暂存失败时，抛出 DEVICE_MESSAGE_SPOOL_REJECTED；直发被永久拒绝时，抛出 DEVICE_MESSAGE_REJECTED
        message.setRaw(raw);
        sendDeviceMessage(message, device, serverId, protocol);
        return message.getId();
    }

    /**
     * 投递一条没有解析为业务消息的原始消息 thing.raw.post：只带原始帧
     *
     * @return 消息编号
     */
    private String sendRawPostMessage(IotDeviceRespDTO device, String serverId, String raw) {
        IotDeviceMessage message = IotDeviceMessage.of(null, IotDeviceMessageMethodEnum.RAW_POST.getMethod(),
                null, null, null, null).setRaw(raw);
        sendDeviceMessage(message, device, serverId, null);
        return message.getId();
    }

    // ========== 下行指令 ==========

    @Override
    public IotDownstreamEncodeRespDTO encodeDownstreamCommand(IotDeviceMessage message, IotProtocolTypeEnum protocol,
                                                              long receivedNanos) {
        // 截止从订阅者收到指令时开始计算：排队的时间也计入
        long deadlineNanos = buildDeadlineNanos(receivedNanos);
        // 1.1 获取设备：不存在（或查询失败）时与未启用脚本一致，走标准路径
        IotDeviceRespDTO device = getDeviceQuietly(message.getDeviceId());
        if (device == null) {
            return IotDownstreamEncodeRespDTO.standard();
        }
        // 1.2 产品脚本（随设备信息缓存）：停用或从未配置，与未启用脚本一致；已启用但内容为空，不写设备，也不改发 JSON
        if (!Boolean.TRUE.equals(device.getScriptEnabled())) {
            return IotDownstreamEncodeRespDTO.standard();
        }
        if (StrUtil.isBlank(device.getScriptContent())) {
            log.warn("[encodeDownstreamCommand][下行({}) 数据解析脚本已启用但内容为空，不写设备]", message.getRequestId());
            return null;
        }

        // 2.1 执行 encode
        IotScriptSource source = IotScriptSource.of(device.getProductId(), device.getScriptContent());
        String ctx = IotScriptUtils.buildEncodeContext(protocol.getType(), device.getProductKey(),
                device.getDeviceName(), message);
        IotScriptResult result = scriptExecutor.execute(source, IotScriptUtils.FUNCTION_ENCODE, ctx, deadlineNanos);
        if (!result.isSuccess()) {
            log.warn("[encodeDownstreamCommand][下行({}) 脚本执行失败({})：{}]", message.getRequestId(),
                    result.getStatus(), result.getMessage());
            return null;
        }
        // 2.2 校验输出：结构，以及字节是否符合协议的规则（Modbus 必须是合法的请求 PDU）
        IotScriptUtils.EncodeOutput output;
        try {
            output = IotScriptUtils.validateEncode(result.getJson(), protocol);
        } catch (IotScriptException e) {
            log.warn("[encodeDownstreamCommand][下行({}) 脚本输出不合法：{}]", message.getRequestId(), e.getMessage());
            return null;
        }
        if (output == null) {
            log.warn("[encodeDownstreamCommand][下行({}) 脚本未生成下行数据，不写设备]", message.getRequestId());
            return null;
        }

        // 3. 下发之前核对截止：迟到的结果不写设备，也不改走标准路径
        if (System.nanoTime() >= deadlineNanos) {
            log.warn("[encodeDownstreamCommand][下行({}) 超过截止，不写设备]", message.getRequestId());
            return null;
        }
        return output.isStandard() ? IotDownstreamEncodeRespDTO.standard()
                : IotDownstreamEncodeRespDTO.raw(output.getBytes(), source);
    }

    // ========== 透传回复 ==========

    @Override
    public void sendRawReply(IotDeviceMessage request, IotScriptSource source, byte[] payload, ErrorCode deviceError, String serverId) {
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(request.getDeviceId());
        if (device == null) {
            log.warn("[sendRawReply][设备({}) 不存在]", request.getDeviceId());
            return;
        }
        // 情况一：raw.push，直接回复设备的实际响应；结果码由协议判定（例如 Modbus 异常响应）
        if (source == null) {
            ErrorCode code = deviceError != null ? deviceError : SUCCESS;
            IotDeviceMessage reply = IotDeviceMessage.replyOf(request.getRequestId(), request.getMethod(), null,
                    code.getCode(), code.getMsg()).setRaw(IotDeviceMessageUtils.encodeRaw(payload));
            sendDeviceMessage(reply, device, serverId, null);
            return;
        }

        // 情况二：encode 的指令，用 encode 时固定的脚本执行 decode；功能开关已关闭，或设备缓存里已看到脚本停用时，不解析响应
        if (!gatewayProperties.getScript().isEnabled()) {
            log.warn("[sendRawReply][下行({}) 期间数据解析脚本功能已关闭，不解析响应]", request.getRequestId());
            return;
        }
        if (!Boolean.TRUE.equals(device.getScriptEnabled())) {
            log.warn("[sendRawReply][下行({}) 期间数据解析脚本已停用，不解析响应]", request.getRequestId());
            return;
        }
        long deadlineNanos = buildDeadlineNanos(System.nanoTime());
        String ctx = IotScriptUtils.buildDecodeContext(device.getProtocolType(), device.getProductKey(),
                device.getDeviceName(), System.currentTimeMillis(), payload, request);
        IotScriptResult result = scriptExecutor.execute(source, IotScriptUtils.FUNCTION_DECODE, ctx, deadlineNanos);
        if (!result.isSuccess()) {
            log.warn("[sendRawReply][下行({}) 脚本执行失败({})：{}]", request.getRequestId(),
                    result.getStatus(), result.getMessage());
            return;
        }
        IotDeviceMessage reply;
        try {
            reply = IotScriptUtils.validateDecode(result.getJson(), request);
        } catch (IotScriptException e) {
            log.warn("[sendRawReply][下行({}) 脚本输出不合法：{}]", request.getRequestId(), e.getMessage());
            return;
        }
        reply.setRaw(IotDeviceMessageUtils.encodeRaw(payload));
        sendDeviceMessage(reply, device, serverId, null);
    }

    // ========== 属性设置回复 ==========

    @Override
    public void sendPropertySetReply(IotDeviceMessage request, Map<String, IotDevicePropertySetResultDTO> results,
                                     ErrorCode failure, String serverId) {
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(request.getDeviceId());
        if (device == null) {
            log.warn("[sendPropertySetReply][设备({}) 不存在]", request.getDeviceId());
            return;
        }
        // 1.1 整条消息失败：没有逐项结果；逐项回复却没有任何结果时，同样按整条失败回复，避免误回成功
        if (failure == null && MapUtil.isEmpty(results)) {
            log.warn("[sendPropertySetReply][设备({}) 属性设置({}) 没有结果]", request.getDeviceId(), request.getRequestId());
            failure = DEVICE_OFFLINE_OR_WRITE_FAILED;
        }
        IotDeviceMessage reply;
        if (failure != null) {
            reply = IotDeviceMessage.replyOf(request.getRequestId(), request.getMethod(), null,
                    failure.getCode(), failure.getMsg());
        } else {
            // 1.2 逐项回复：全部成功时为成功，有失败（含缺失的结果）时整体为 PROPERTY_SET_FAILED
            long failedCount = results.values().stream().filter(result -> result == null || !result.succeeded()).count();
            Integer code = failedCount > 0 ? PROPERTY_SET_FAILED.getCode() : null;
            String msg = failedCount > 0 ? StrUtil.format(PROPERTY_SET_FAILED.getMsg(), results.size(), failedCount) : null;
            reply = IotDeviceMessage.replyOf(request.getRequestId(), request.getMethod(), results, code, msg);
        }
        // 2. 发送
        sendDeviceMessage(reply, device, serverId, null);
    }

    // ========== 工具方法 ==========

    /**
     * 计算截止时间（System.nanoTime）：开始时刻 + gateway.script.deadline
     *
     * @param startNanos 开始计时的时刻（System.nanoTime）
     */
    private long buildDeadlineNanos(long startNanos) {
        return startNanos + gatewayProperties.getScript().getDeadline().toNanos();
    }

    private IotDeviceRespDTO getDevice(String productKey, String deviceName) {
        IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceName);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS, productKey, deviceName);
        }
        return device;
    }

    /**
     * 下行获取设备：查询失败时按设备不存在处理（走标准路径，由标准路径自己处理），不丢弃指令
     */
    private IotDeviceRespDTO getDeviceQuietly(Long deviceId) {
        try {
            return deviceService.getDeviceFromCache(deviceId);
        } catch (RuntimeException e) {
            log.warn("[getDeviceQuietly][设备({}) 查询失败，按标准路径下发]", deviceId, e);
            return null;
        }
    }

    private static ServiceException scriptException(IotScriptResult.Status status, String detail) {
        detail = StrUtil.maxLength(StrUtil.nullToEmpty(detail), 200);
        switch (status) {
            case TIMEOUT:
                return exception(SCRIPT_TIMEOUT);
            case REJECTED:
                return exception(SCRIPT_REJECTED);
            case OUTPUT_INVALID:
                return exception(SCRIPT_OUTPUT_INVALID, detail);
            default:
                return exception(SCRIPT_EXECUTE_FAILED, detail);
        }
    }

}
