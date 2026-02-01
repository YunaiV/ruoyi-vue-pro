package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttDownstreamHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议：下行消息订阅器
 * <p>
 * 负责接收来自消息总线的下行消息，并委托给下行处理器进行业务处理
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttDownstreamSubscriber extends IotProtocolDownstreamSubscriber {

    private final IotMqttDownstreamHandler downstreamHandler;

    public IotMqttDownstreamSubscriber(IotMqttUpstreamProtocol protocol,
                                       IotMqttDownstreamHandler downstreamHandler,
                                       IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        boolean success = downstreamHandler.handleDownstreamMessage(message);
        if (success) {
            log.debug("[handleMessage][下行消息处理成功, messageId: {}, method: {}, deviceId: {}]",
                    message.getId(), message.getMethod(), message.getDeviceId());
        } else {
            log.warn("[handleMessage][下行消息处理失败, messageId: {}, method: {}, deviceId: {}]",
                    message.getId(), message.getMethod(), message.getDeviceId());
        }
    }

}
