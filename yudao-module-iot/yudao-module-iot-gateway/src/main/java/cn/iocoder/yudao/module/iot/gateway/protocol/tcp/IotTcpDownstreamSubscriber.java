package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router.IotTcpDownstreamHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 下游订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpDownstreamSubscriber extends IotProtocolDownstreamSubscriber {

    private final IotTcpDownstreamHandler downstreamHandler;

    public IotTcpDownstreamSubscriber(IotTcpUpstreamProtocol protocol,
                                      IotTcpDownstreamHandler downstreamHandler,
                                      IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = downstreamHandler;
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
