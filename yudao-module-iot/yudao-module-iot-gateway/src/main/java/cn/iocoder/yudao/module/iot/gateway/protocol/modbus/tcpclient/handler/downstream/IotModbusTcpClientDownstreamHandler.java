package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertySetResultDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusTcpClientUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.*;

/**
 * IoT Modbus TCP Client 下行消息处理器
 * <p>
 * 负责：
 * 1. 处理下行消息（属性设置 thing.property.set）
 * 2. 将属性值转换为 Modbus 写指令，通过 TCP 连接发送给设备；每个属性单独写入，单个失败不影响其它属性
 * 3. 返回每个属性的结果（设备响应、异常响应或网关判定的失败），由订阅器回复 thing.property.set_reply
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpClientDownstreamHandler {

    private final IotModbusTcpClientConnectionManager connectionManager;
    private final IotModbusTcpClientConfigCacheService configCacheService;

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
            log.warn("[handle][忽略非属性设置消息: {}]", message.getMethod());
            return null;
        }
        // 1.2 校验参数
        Object params = message.getParams();
        if (!(params instanceof Map) || MapUtil.isEmpty((Map<?, ?>) params)) {
            log.warn("[handle][params 不是非空的 Map: {}]", params);
            return Future.failedFuture(exception(PROPERTY_SET_PARAMS_INVALID, "params 需要是非空的属性 Map"));
        }
        Map<String, Object> propertyMap = (Map<String, Object>) params;
        // 回调在连接的 context 上执行，与本方法（总线消费线程）可能并发：加锁
        Map<String, IotDevicePropertySetResultDTO> results = Collections.synchronizedMap(new LinkedHashMap<>());
        // 1.3 获取设备配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(message.getDeviceId());
        if (config == null) {
            log.warn("[handle][设备 {} 没有 Modbus 配置]", message.getDeviceId());
            return Future.succeededFuture(failAll(propertyMap, results, PROPERTY_SET_DEVICE_NOT_CONFIGURED));
        }
        // 1.4 获取连接和 slave ID
        IotModbusTcpClientConnectionManager.ModbusConnection connection = connectionManager.getConnection(config.getDeviceId());
        Integer slaveId = connection != null ? connectionManager.getSlaveId(config.getDeviceId()) : null;
        if (slaveId == null) {
            log.warn("[handle][设备 {} 没有连接或 slaveId]", config.getDeviceId());
            return Future.succeededFuture(failAll(propertyMap, results, DEVICE_OFFLINE_OR_WRITE_FAILED));
        }

        // 2. 逐个属性写入：校验、转换失败的直接记录结果，不影响其它属性
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
            String identifier = entry.getKey();
            results.put(identifier, null); // 先占位，保持参数顺序
            Future<Boolean> write;
            try {
                write = writeProperty(connection, slaveId, config, identifier, entry.getValue());
            } catch (PropertySetException e) {
                results.put(identifier, e.result);
                continue;
            } catch (RuntimeException e) { // 例如连接的 Context 已关闭，提交被同步拒绝
                log.warn("[handle][设备 {} 属性 {} 无法提交写入]", config.getDeviceId(), identifier, e);
                results.put(identifier, IotDevicePropertySetResultDTO.failure(DEVICE_OFFLINE_OR_WRITE_FAILED));
                continue;
            }
            futures.add(write.transform(ar -> {
                if (ar.succeeded()) {
                    log.info("[handle][写入成功, deviceId={}, identifier={}, value={}]", config.getDeviceId(),
                            identifier, entry.getValue());
                    results.put(identifier, IotDevicePropertySetResultDTO.success());
                } else {
                    log.error("[handle][写入失败, deviceId={}, identifier={}]", config.getDeviceId(), identifier, ar.cause());
                    results.put(identifier, toWriteFailure(ar.cause()));
                }
                return Future.succeededFuture();
            }));
        }
        // 3. 全部写入结束后返回
        return Future.join(futures).transform(ar -> Future.succeededFuture(new LinkedHashMap<>(results)));
    }

    /**
     * 写入一个属性
     *
     * @return 写入结果
     * @throws PropertySetException 点位不存在、不可写，或者值不合法
     */
    private Future<Boolean> writeProperty(IotModbusTcpClientConnectionManager.ModbusConnection connection, Integer slaveId,
                                          IotModbusDeviceConfigRespDTO config, String identifier, Object value)
            throws PropertySetException {
        // 1.1 查找对应的点位配置
        IotModbusPointRespDTO point = IotModbusCommonUtils.findPoint(config, identifier);
        if (point == null) {
            log.warn("[writeProperty][设备 {} 没有点位配置: {}]", config.getDeviceId(), identifier);
            throw new PropertySetException(IotDevicePropertySetResultDTO.failure(PROPERTY_SET_POINT_NOT_EXISTS));
        }
        // 1.2 检查是否支持写操作
        if (!IotModbusCommonUtils.isWritable(point.getFunctionCode())) {
            log.warn("[writeProperty][点位 {} 不支持写操作, 功能码={}]", identifier, point.getFunctionCode());
            throw new PropertySetException(IotDevicePropertySetResultDTO.failure(PROPERTY_SET_POINT_NOT_WRITABLE,
                    "功能码 " + point.getFunctionCode() + " 只读"));
        }
        // 2. 执行 Modbus 写入：位点位只修改寄存器中的这一位（读-改-写）；其它点位转换为原始值后整体写入。
        //    在有序队列里轮到本任务时，先核对连接（IP、端口、从站号）和点位（启用、地址、类型等）都未变化，避免写到旧目标
        Runnable checkPoint = () -> checkWriteTargetUnchanged(config.getDeviceId(), connection, slaveId, point);
        try {
            return IotModbusCommonUtils.isBitPoint(point)
                    ? IotModbusTcpClientUtils.writeBit(connection, slaveId, point, IotModbusCommonUtils.toBitValue(value), checkPoint)
                    : IotModbusTcpClientUtils.write(connection, slaveId, point,
                    IotModbusCommonUtils.convertToRawValues(value, point), checkPoint);
        } catch (IllegalArgumentException e) {
            log.warn("[writeProperty][设备 {} 属性 {} 的值 {} 不合法: {}]", config.getDeviceId(), identifier, value, e.getMessage());
            throw new PropertySetException(IotDevicePropertySetResultDTO.failure(PROPERTY_SET_VALUE_INVALID, e.getMessage()));
        }
    }

    /**
     * 核对写入目标仍与下发时一致：
     * 1. 连接：设备仍使用该连接（IP、端口未变），从站号未变；否则抛出 {@link ErrorCodeConstants#PROPERTY_SET_CONNECTION_CHANGED}；
     * 2. 点位：仍在当前配置中（启用），地址、类型等未变；否则抛出 {@link ErrorCodeConstants#PROPERTY_SET_POINT_CHANGED}
     * <p>
     * 在连接的有序任务（worker）上执行：配置缓存、连接映射都是 ConcurrentHashMap，只读本地状态，不访问远程
     */
    private void checkWriteTargetUnchanged(Long deviceId, IotModbusTcpClientConnectionManager.ModbusConnection connection,
                                           Integer slaveId, IotModbusPointRespDTO point) {
        IotModbusDeviceConfigRespDTO current = configCacheService.getConfig(deviceId);
        if (!connectionManager.isCurrentTarget(current, connection, slaveId)) {
            throw exception(PROPERTY_SET_CONNECTION_CHANGED);
        }
        if (!IotModbusCommonUtils.isSameWriteTarget(point, IotModbusCommonUtils.findPoint(current, point.getIdentifier()))) {
            throw exception(PROPERTY_SET_POINT_CHANGED);
        }
    }

    /**
     * 写入失败转为结果：设备异常响应 → 带回异常码；其它按透传的规则（超时 110、其它 106）
     */
    private static IotDevicePropertySetResultDTO toWriteFailure(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ModbusSlaveException) {
                int exceptionCode = ((ModbusSlaveException) cause).getType();
                return IotDevicePropertySetResultDTO.failureWithStatus(MODBUS_EXCEPTION, String.valueOf(exceptionCode),
                        exceptionCode);
            }
        }
        ErrorCode errorCode = IotModbusTcpClientUtils.toRawFailure(e);
        return new IotDevicePropertySetResultDTO(errorCode.getCode(), errorCode.getMsg(), null);
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
     * 单个属性在写出之前的失败
     */
    private static class PropertySetException extends Exception {

        private final transient IotDevicePropertySetResultDTO result;

        PropertySetException(IotDevicePropertySetResultDTO result) {
            super(result.getMsg(), null, false, false);
            this.result = result;
        }

    }

}
