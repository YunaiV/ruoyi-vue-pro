package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.handler.downstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec.IotModbusFrameEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager.IotModbusTcpServerConnectionManager.ConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IoT Modbus TCP Server 下行消息处理器
 * <p>
 * 负责：
 * 1. 处理下行消息（如属性设置 thing.service.property.set）
 * 2. 将属性值转换为 Modbus 写指令，通过 TCP 连接发送给设备
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerDownstreamHandler {

    private final IotModbusTcpServerConnectionManager connectionManager;
    private final IotModbusTcpServerConfigCacheService configCacheService;
    private final IotModbusFrameEncoder frameEncoder;

    /**
     * TCP 事务 ID 自增器（与 PollScheduler 共享）
     */
    private final AtomicInteger transactionIdCounter;

    public IotModbusTcpServerDownstreamHandler(IotModbusTcpServerConnectionManager connectionManager,
                                              IotModbusTcpServerConfigCacheService configCacheService,
                                              IotModbusFrameEncoder frameEncoder,
                                              AtomicInteger transactionIdCounter) {
        this.connectionManager = connectionManager;
        this.configCacheService = configCacheService;
        this.frameEncoder = frameEncoder;
        this.transactionIdCounter = transactionIdCounter;
    }

    /**
     * 处理下行消息
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public void handle(IotDeviceMessage message) {
        // 1.1 检查是否是属性设置消息
        if (ObjUtil.equals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), message.getMethod())) {
            return;
        }
        if (ObjUtil.notEqual(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), message.getMethod())) {
            log.debug("[handle][忽略非属性设置消息: {}]", message.getMethod());
            return;
        }
        // 1.2 获取设备配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(message.getDeviceId());
        if (config == null) {
            log.warn("[handle][设备 {} 没有 Modbus 配置]", message.getDeviceId());
            return;
        }
        // 1.3 获取连接信息
        ConnectionInfo connInfo = connectionManager.getConnectionInfoByDeviceId(message.getDeviceId());
        if (connInfo == null) {
            log.warn("[handle][设备 {} 没有连接]", message.getDeviceId());
            return;
        }

        // 2. 解析属性值并写入
        Object params = message.getParams();
        if (!(params instanceof Map)) {
            log.warn("[handle][params 不是 Map 类型: {}]", params);
            return;
        }
        Map<String, Object> propertyMap = (Map<String, Object>) params;
        for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
            String identifier = entry.getKey();
            Object value = entry.getValue();
            // 2.1 查找对应的点位配置
            IotModbusPointRespDTO point = IotModbusCommonUtils.findPoint(config, identifier);
            if (point == null) {
                log.warn("[handle][设备 {} 没有点位配置: {}]", message.getDeviceId(), identifier);
                continue;
            }
            // 2.2 检查是否支持写操作
            if (!IotModbusCommonUtils.isWritable(point.getFunctionCode())) {
                log.warn("[handle][点位 {} 不支持写操作, 功能码={}]", identifier, point.getFunctionCode());
                continue;
            }

            // 2.3 执行写入
            writeProperty(config.getDeviceId(), connInfo, point, value);
        }
    }

    /**
     * 写入属性值
     */
    private void writeProperty(Long deviceId, ConnectionInfo connInfo,
                                IotModbusPointRespDTO point, Object value) {
        // 1.1 转换属性值为原始值
        int[] rawValues = IotModbusCommonUtils.convertToRawValues(value, point);

        // 1.2 确定帧格式和事务 ID
        IotModbusFrameFormatEnum frameFormat = connInfo.getFrameFormat();
        Assert.notNull(frameFormat, "连接帧格式不能为空");
        Integer transactionId = frameFormat == IotModbusFrameFormatEnum.MODBUS_TCP
                ? (transactionIdCounter.incrementAndGet() & 0xFFFF)
                : null;
        int slaveId = connInfo.getSlaveId() != null ? connInfo.getSlaveId() : 1;
        // 1.3 编码写请求
        byte[] data;
        int readFunctionCode = point.getFunctionCode();
        Integer writeSingleCode = IotModbusCommonUtils.getWriteSingleFunctionCode(readFunctionCode);
        Integer writeMultipleCode = IotModbusCommonUtils.getWriteMultipleFunctionCode(readFunctionCode);
        if (rawValues.length == 1 && writeSingleCode != null) {
            // 单个值：使用单写功能码（FC05/FC06）
            data = frameEncoder.encodeWriteSingleRequest(slaveId, writeSingleCode,
                    point.getRegisterAddress(), rawValues[0], frameFormat, transactionId);
        } else if (writeMultipleCode != null) {
            // 多个值：使用多写功能码（FC15/FC16）
            if (writeMultipleCode == IotModbusCommonUtils.FC_WRITE_MULTIPLE_COILS) {
                data = frameEncoder.encodeWriteMultipleCoilsRequest(slaveId,
                        point.getRegisterAddress(), rawValues, frameFormat, transactionId);
            } else {
                data = frameEncoder.encodeWriteMultipleRegistersRequest(slaveId,
                        point.getRegisterAddress(), rawValues, frameFormat, transactionId);
            }
        } else {
            log.warn("[writeProperty][点位 {} 不支持写操作, 功能码={}]", point.getIdentifier(), readFunctionCode);
            return;
        }

        // 2. 发送消息
        connectionManager.sendToDevice(deviceId, data).onSuccess(v ->
                log.info("[writeProperty][写入成功, deviceId={}, identifier={}, value={}]",
                        deviceId, point.getIdentifier(), value)
        ).onFailure(e ->
                log.error("[writeProperty][写入失败, deviceId={}, identifier={}, value={}]",
                        deviceId, point.getIdentifier(), value, e)
        );
    }

}
