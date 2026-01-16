package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * IoT Modbus TCP 下行消息处理器
 *
 * 负责：
 * 1. 处理下行消息（如属性设置 thing.service.property.set）
 * 2. 执行 Modbus 写入操作
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusTcpDownstreamHandler {

    private final IotModbusTcpConnectionManager connectionManager;
    private final IotModbusTcpClient modbusClient;
    private final IotModbusDataConverter dataConverter;
    private final IotModbusTcpConfigCacheService configCacheService;

    /**
     * 处理下行消息
     */
    @SuppressWarnings("unchecked")
    public void handle(IotDeviceMessage message) {
        // 1.1 检查是否是属性设置消息
        if (!"thing.service.property.set".equals(message.getMethod())) {
            log.debug("[handle][忽略非属性设置消息: {}]", message.getMethod());
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
            // 2.1.1 查找对应的点位配置
            IotModbusPointRespDTO point = findPoint(config, identifier);
            if (point == null) {
                log.warn("[handle][设备 {} 没有点位配置: {}]", message.getDeviceId(), identifier);
                continue;
            }
            // 2.1.2 检查是否支持写操作
            if (!isWritable(point.getFunctionCode())) {
                log.warn("[handle][点位 {} 不支持写操作, 功能码={}]", identifier, point.getFunctionCode());
                continue;
            }

            // 2.2 执行写入
            writeProperty(config, point, value);
        }
    }

    /**
     * 写入属性值
     */
    private void writeProperty(IotModbusDeviceConfigRespDTO config, IotModbusPointRespDTO point, Object value) {
        // 1.1 获取连接
        IotModbusTcpConnectionManager.ModbusConnection connection = connectionManager.getConnection(config.getDeviceId());
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
        int[] rawValues = dataConverter.convertToRawValues(value, point);
        // 2.2 执行 Modbus 写入
        modbusClient.write(connection, slaveId, point, rawValues)
                .onSuccess(success -> log.info("[writeProperty][写入成功, deviceId={}, identifier={}, value={}]",
                        config.getDeviceId(), point.getIdentifier(), value))
                .onFailure(e -> log.error("[writeProperty][写入失败, deviceId={}, identifier={}]",
                        config.getDeviceId(), point.getIdentifier(), e));
    }

    /**
     * 查找点位配置
     */
    private IotModbusPointRespDTO findPoint(IotModbusDeviceConfigRespDTO config, String identifier) {
        if (config.getPoints() == null) {
            return null;
        }
        // TODO @AI：hutool findOne？
        return config.getPoints().stream()
                .filter(p -> identifier.equals(p.getIdentifier()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查功能码是否支持写操作
     */
    private boolean isWritable(Integer functionCode) {
        // TODO @AI：能不能通过 枚举优化下？
        // 功能码 1（ReadCoils）和 3（ReadHoldingRegisters）支持写操作
        return functionCode != null && (functionCode == 1 || functionCode == 3);
    }

}
