package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttProtocol;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotMqttDownstreamHandler downstreamHandler;

    public IotMqttDownstreamSubscriber(IotMqttProtocol protocol,
                                       IotMqttDownstreamHandler downstreamHandler,
                                       IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

    @Override
    public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes, IotScriptSource source) {
        return downstreamHandler.handleRawMessage(message, bytes);
    }

    @Override
    public boolean supportsRawDownstream() {
        return true;
    }

}
