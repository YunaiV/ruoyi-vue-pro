package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerTransactionChannel;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;

/**
 * IoT Modbus TCP Server 下行消息处理器
 * <p>
 * 负责：
 * 1. 处理下行消息（属性设置 thing.property.set）
 * 2. 将属性值转换为 Modbus 写请求 PDU（FC05 / FC06 / FC15 / FC16），每个属性一个事务，提交到连接的事务通道：
 *    与透传共用排队、在途上限、超时、断线清理和响应关联（TCP 按事务号、RTU 同一时刻只有 1 个在途），等待设备的回显或异常响应；
 * 3. 返回每个属性的结果（单个失败不影响其它属性），由订阅器回复 thing.property.set_reply
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerDownstreamHandler {

    private final IotModbusTcpServerConnectionManager connectionManager;
    private final IotModbusTcpServerConfigCacheService configCacheService;

    public IotModbusTcpServerDownstreamHandler(IotModbusTcpServerConnectionManager connectionManager,
                                               IotModbusTcpServerConfigCacheService configCacheService) {
        this.connectionManager = connectionManager;
        this.configCacheService = configCacheService;
    }

    /**
     * 处理下行消息
     *
     * @param message 下行消息
     * @return 属性设置时返回每个属性的结果（失败 = 整条消息失败，例如参数不合法）；其它消息返回 null（不回复）
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public Future<Map<String, IotDevicePropertySetResultDTO>> handle(IotDeviceMessage message) {
        // 1.1 检查是否是属性设置消息
        if (ObjUtil.equals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), message.getMethod())) {
            return null;
        }
        if (ObjUtil.notEqual(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), message.getMethod())
                || IotDeviceMessageUtils.isReplyMessage(message)) {
            log.debug("[handle][忽略非属性设置消息: {}]", message.getMethod());
            return null;
        }
        // 1.2 校验参数
        Object params = message.getParams();
        if (!(params instanceof Map) || MapUtil.isEmpty((Map<?, ?>) params)) {
            log.warn("[handle][params 不是非空的 Map: {}]", params);
            return Future.failedFuture(exception(PROPERTY_SET_PARAMS_INVALID, "params 需要是非空的属性 Map"));
        }
        Map<String, Object> propertyMap = (Map<String, Object>) params;
        // 事务终态回调在连接的 owner 上执行，与本方法（总线消费线程）可能并发：加锁
        Map<String, IotDevicePropertySetResultDTO> results = Collections.synchronizedMap(new LinkedHashMap<>());
        // 1.3 获取设备配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(message.getDeviceId());
        if (config == null) {
            log.warn("[handle][设备 {} 没有 Modbus 配置]", message.getDeviceId());
            return Future.succeededFuture(failAll(propertyMap, results, PROPERTY_SET_DEVICE_NOT_CONFIGURED));
        }
        // 1.4 获取连接（未连接、未认证的连接没有事务通道）
        ConnectionInfo connInfo = connectionManager.getConnectionInfoByDeviceId(message.getDeviceId());
        if (connInfo == null || connInfo.getChannel() == null) {
            log.warn("[handle][设备 {} 没有连接]", message.getDeviceId());
            return Future.succeededFuture(failAll(propertyMap, results, DEVICE_OFFLINE_OR_WRITE_FAILED));
        }
        IotModbusTcpServerTransactionChannel channel = connInfo.getChannel();

        // 2. 逐个属性提交写事务：校验、转换失败的直接记录结果，不影响其它属性
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
            String identifier = entry.getKey();
            results.put(identifier, null); // 先占位，保持参数顺序
            byte[] pdu;
            try {
                pdu = buildWritePdu(config, identifier, entry.getValue());
            } catch (PropertySetException e) {
                results.put(identifier, e.result);
                continue;
            }
            // 2.1 发送之前（owner 上、真正写出之前）核对点位：仍启用，写入目标（地址、类型等）与构造 PDU 时一致
            IotModbusPointRespDTO point = IotModbusCommonUtils.findPoint(config, identifier);
            Supplier<ErrorCode> checkPoint = () -> isPointUnchanged(config.getDeviceId(), point)
                    ? null : PROPERTY_SET_POINT_CHANGED;
            // 2.2 提交事务：同步异常（例如连接的 owner 已关闭）只记为该属性失败，继续其它属性
            Promise<IotDevicePropertySetResultDTO> promise = Promise.promise();
            try {
                channel.submitRaw(IotModbusTx.ofServer(channel, pdu, message, checkPoint,
                        (responsePdu, failure) -> promise.tryComplete(toResult(pdu, responsePdu, failure))));
            } catch (RuntimeException e) {
                log.warn("[handle][设备 {} 属性 {} 无法提交写入]", config.getDeviceId(), identifier, e);
                results.put(identifier, IotDevicePropertySetResultDTO.failure(DEVICE_OFFLINE_OR_WRITE_FAILED));
                continue;
            }
            // 2.3 结果先写入 results 再参与聚合（map 在 join 之前完成），避免汇总时漏掉结果
            futures.add(promise.future().map(result -> {
                log.info("[handle][设备 {} 属性 {} 写入结果: {}]", config.getDeviceId(), identifier, result.getMsg());
                results.put(identifier, result);
                return result;
            }));
        }
        // 3. 全部事务结束后返回
        return Future.join(futures).transform(ar -> Future.succeededFuture(new LinkedHashMap<>(results)));
    }

    /**
     * 构造一个属性的写请求 PDU：单个值使用单写功能码（FC05 / FC06），多个值使用多写功能码（FC15 / FC16）
     *
     * @throws PropertySetException 点位不存在、不可写、是寄存器位点位，或者值不合法
     */
    private static byte[] buildWritePdu(IotModbusDeviceConfigRespDTO config, String identifier, Object value)
            throws PropertySetException {
        // 1.1 查找对应的点位配置
        IotModbusPointRespDTO point = IotModbusCommonUtils.findPoint(config, identifier);
        if (point == null) {
            log.warn("[buildWritePdu][设备 {} 没有点位配置: {}]", config.getDeviceId(), identifier);
            throw new PropertySetException(IotDevicePropertySetResultDTO.failure(PROPERTY_SET_POINT_NOT_EXISTS));
        }
        // 1.2 检查是否支持写操作
        int readFunctionCode = point.getFunctionCode();
        if (!IotModbusCommonUtils.isWritable(readFunctionCode)) {
            log.warn("[buildWritePdu][点位 {} 不支持写操作, 功能码={}]", identifier, readFunctionCode);
            throw new PropertySetException(IotDevicePropertySetResultDTO.failure(PROPERTY_SET_POINT_NOT_WRITABLE,
                    "功能码 " + readFunctionCode + " 只读"));
        }
        // 1.3 寄存器位点位（保持寄存器 + bitIndex）：整字写会冲掉同一寄存器的其它位，本期直接返回不支持
        // TODO【需求】Modbus TCP Server 的寄存器位点位写入。背景：TCP Client 已用 j2mod 在同一连接上同步执行 FC03 读 → 改位 → FC06 写；
        //  TCP Server 的写入是事务通道里的异步事务（与轮询、透传共用在途上限和超时），没有“两个事务之间独占”的能力。
        //  方案二选一：1）事务通道支持组合事务：FC03 读到后在 owner 上改位，再提交 FC06，期间不放行同一寄存器的其它写；
        //  2）设备支持时改用 FC22（Mask Write Register），一个事务完成，无需读取。
        //  边界：线圈（FC01/FC05）、普通寄存器点位不受影响；在实现之前，位点位写入一律失败，不做整字写
        if (IotModbusCommonUtils.isBitPoint(point)) {
            log.warn("[buildWritePdu][点位 {} 是寄存器位点位, Modbus TCP Server 暂不支持位写入]", identifier);
            throw new PropertySetException(IotDevicePropertySetResultDTO.failure(MODBUS_BIT_WRITE_NOT_SUPPORTED));
        }
        // 2.1 转换属性值为原始值
        int[] rawValues;
        try {
            rawValues = IotModbusCommonUtils.convertToRawValues(value, point);
        } catch (IllegalArgumentException e) {
            log.warn("[buildWritePdu][设备 {} 属性 {} 的值 {} 不合法: {}]", config.getDeviceId(), identifier, value, e.getMessage());
            throw new PropertySetException(IotDevicePropertySetResultDTO.failure(PROPERTY_SET_VALUE_INVALID, e.getMessage()));
        }
        // 2.2 构造 PDU
        Integer writeSingleCode = IotModbusCommonUtils.getWriteSingleFunctionCode(readFunctionCode);
        Integer writeMultipleCode = IotModbusCommonUtils.getWriteMultipleFunctionCode(readFunctionCode);
        if (rawValues.length == 1 && writeSingleCode != null) {
            return IotModbusFrameEncoder.buildWriteSinglePdu(writeSingleCode, point.getRegisterAddress(), rawValues[0]);
        }
        if (writeMultipleCode != null && writeMultipleCode == IotModbusCommonUtils.FC_WRITE_MULTIPLE_COILS) {
            return IotModbusFrameEncoder.buildWriteMultipleCoilsPdu(point.getRegisterAddress(), rawValues);
        }
        return IotModbusFrameEncoder.buildWriteMultipleRegistersPdu(point.getRegisterAddress(), rawValues);
    }

    /**
     * 点位仍在本地缓存的配置中（启用），且写入目标与构造 PDU 时一致（在 owner 上调用：只读本地缓存，不访问远程）
     */
    private boolean isPointUnchanged(Long deviceId, IotModbusPointRespDTO point) {
        IotModbusPointRespDTO current = IotModbusCommonUtils.findPoint(configCacheService.getCachedConfig(deviceId),
                point.getIdentifier());
        return IotModbusCommonUtils.isSameWriteTarget(point, current);
    }

    /**
     * 写事务的终态转为结果：
     * 1. 网关判定的失败（超时、断线、队列满、点位已变化等）→ 对应错误码；
     * 2. 异常响应 [fc | 0x80, 异常码] → 带回异常码；长度不足 2（缺少异常码）→ 响应不合法；
     * 3. 正常响应：与请求一致的回显 → 成功；格式不一致 → 响应不合法（不当作成功）
     *
     * @param requestPdu  请求 PDU
     * @param responsePdu 设备响应 PDU；失败时为 null
     * @param failure     网关判定的失败
     * @return 结果
     */
    static IotDevicePropertySetResultDTO toResult(byte[] requestPdu, byte[] responsePdu, ErrorCode failure) {
        if (failure != null || responsePdu == null || responsePdu.length == 0) {
            ErrorCode errorCode = failure != null ? failure : MODBUS_RESPONSE_INVALID;
            return new IotDevicePropertySetResultDTO(errorCode.getCode(), errorCode.getMsg(), null);
        }
        if (IotModbusCommonUtils.isExceptionResponse(responsePdu[0] & 0xFF)) {
            if (responsePdu.length < 2) {
                return IotDevicePropertySetResultDTO.failure(MODBUS_RESPONSE_INVALID);
            }
            int exceptionCode = responsePdu[1] & 0xFF;
            return IotDevicePropertySetResultDTO.failureWithStatus(MODBUS_EXCEPTION, String.valueOf(exceptionCode),
                    exceptionCode);
        }
        if (!IotModbusCommonUtils.isResponseShapeValid(requestPdu, responsePdu)) {
            return IotDevicePropertySetResultDTO.failure(MODBUS_RESPONSE_INVALID);
        }
        return IotDevicePropertySetResultDTO.success();
    }

    /**
     * 整条消息无法写入（没有配置、没有连接）：每个属性都记为同一个失败结果
     *
     * @return 结果副本（按参数顺序）
     */
    private static Map<String, IotDevicePropertySetResultDTO> failAll(Map<String, Object> propertyMap,
                                                                      Map<String, IotDevicePropertySetResultDTO> results,
                                                                      ErrorCode errorCode) {
        propertyMap.keySet().forEach(identifier -> results.put(identifier, IotDevicePropertySetResultDTO.failure(errorCode)));
        return new LinkedHashMap<>(results);
    }

    /**
     * 单个属性在提交事务之前的失败
     */
    private static class PropertySetException extends Exception {

        private final transient IotDevicePropertySetResultDTO result;

        PropertySetException(IotDevicePropertySetResultDTO result) {
            super(result.getMsg(), null, false, false);
            this.result = result;
        }

    }

}
