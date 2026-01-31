package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.router.IotUdpDownstreamHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 UDP 下游订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpDownstreamSubscriber extends IotProtocolDownstreamSubscriber {

    private final IotUdpDownstreamHandler downstreamHandler;

    public IotUdpDownstreamSubscriber(IotUdpUpstreamProtocol protocol,
                                      IotUdpDownstreamHandler downstreamHandler,
                                      IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
