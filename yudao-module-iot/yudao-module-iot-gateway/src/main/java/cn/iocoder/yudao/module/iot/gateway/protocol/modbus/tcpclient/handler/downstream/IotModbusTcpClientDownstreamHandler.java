package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusTcpClientUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConfigCacheService;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * IoT Modbus TCP Client 下行消息处理器
 * <p>
 * 负责：
 * 1. 处理下行消息（如属性设置 thing.service.property.set）
 * 2. 将属性值转换为 Modbus 写指令，通过 TCP 连接发送给设备
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
     */
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    public void handle(IotDeviceMessage message) {
        // 1.1 检查是否是属性设置消息
        if (ObjUtil.equals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), message.getMethod())) {
            return;
        }
        if (ObjUtil.notEqual(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), message.getMethod())) {
            log.warn("[handle][忽略非属性设置消息: {}]", message.getMethod());
            return;
        }
        // 1.2 获取设备配置
        IotModbusDeviceConfigRespDTO config = configCacheService.getConfig(message.getDeviceId());
        if (config == null) {
            log.warn("[handle][设备 {} 没有 Modbus 配置]", message.getDeviceId());
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
            writeProperty(config, point, value);
        }
    }

    /**
     * 写入属性值
     */
    private void writeProperty(IotModbusDeviceConfigRespDTO config, IotModbusPointRespDTO point, Object value) {
        // 1.1 获取连接
        IotModbusTcpClientConnectionManager.ModbusConnection connection = connectionManager.getConnection(config.getDeviceId());
        if (connection == null) {
            log.warn("[writeProperty][设备 {} 没有连接]", config.getDeviceId());
            return;
        }
        // 1.2 获取 slave ID
        Integer slaveId = connectionManager.getSlaveId(config.getDeviceId());
        if (slaveId == null) {
            log.warn("[writeProperty][设备 {} 没有 slaveId]", config.getDeviceId());
            return;
        }

        // 2.1 转换属性值为原始值
        int[] rawValues = IotModbusCommonUtils.convertToRawValues(value, point);
        // 2.2 执行 Modbus 写入
        IotModbusTcpClientUtils.write(connection, slaveId, point, rawValues)
                .onSuccess(success -> log.info("[writeProperty][写入成功, deviceId={}, identifier={}, value={}]",
                        config.getDeviceId(), point.getIdentifier(), value))
                .onFailure(e -> log.error("[writeProperty][写入失败, deviceId={}, identifier={}]",
                        config.getDeviceId(), point.getIdentifier(), e));
    }

}
