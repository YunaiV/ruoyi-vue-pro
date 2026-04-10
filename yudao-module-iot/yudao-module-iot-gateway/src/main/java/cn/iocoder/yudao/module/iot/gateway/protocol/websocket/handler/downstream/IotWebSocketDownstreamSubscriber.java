package cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketProtocol;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 WebSocket 下游订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotWebSocketDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    private final IotWebSocketDownstreamHandler downstreamHandler;

    public IotWebSocketDownstreamSubscriber(IotWebSocketProtocol protocol,
                                            IotWebSocketDownstreamHandler downstreamHandler,
                                            IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
