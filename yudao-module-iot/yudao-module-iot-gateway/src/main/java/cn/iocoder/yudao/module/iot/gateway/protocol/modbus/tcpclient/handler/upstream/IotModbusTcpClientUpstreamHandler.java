package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.upstream;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * IoT Modbus TCP 上行数据处理器：将原始值转换为物模型属性值并上报
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpClientUpstreamHandler {

    private final IotDeviceMessageService messageService;

    private final String serverId;

    public IotModbusTcpClientUpstreamHandler(IotDeviceMessageService messageService,
                                             String serverId) {
        this.messageService = messageService;
        this.serverId = serverId;
    }

    /**
     * 处理 Modbus 读取结果
     *
     * @param config   设备配置
     * @param point    点位配置
     * @param rawValue 原始值（int 数组）
     */
    public void handleReadResult(IotModbusDeviceConfigRespDTO config,
                                 IotModbusPointRespDTO point,
                                 int[] rawValue) {
        try {
            // 1.1 转换原始值为物模型属性值（点位翻译）
            Object convertedValue = IotModbusCommonUtils.convertToPropertyValue(rawValue, point);
            log.debug("[handleReadResult][设备={}, 属性={}, 原始值={}, 转换值={}]",
                    config.getDeviceId(), point.getIdentifier(), rawValue, convertedValue);
            // 1.2 构造属性上报消息
            Map<String, Object> params = MapUtil.of(point.getIdentifier(), convertedValue);
            IotDeviceMessage message = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);

            // 2. 发送到消息总线
            messageService.sendDeviceMessage(message, config.getProductKey(),
                    config.getDeviceName(), serverId);
        } catch (Exception e) {
            log.error("[handleReadResult][处理读取结果失败, deviceId={}, identifier={}]",
                    config.getDeviceId(), point.getIdentifier(), e);
        }
    }

}
