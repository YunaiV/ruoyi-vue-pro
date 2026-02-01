package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.router.IotEmqxDownstreamHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 EMQX 订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxDownstreamSubscriber extends IotProtocolDownstreamSubscriber {

    private final IotEmqxDownstreamHandler downstreamHandler;

    public IotEmqxDownstreamSubscriber(IotEmqxUpstreamProtocol protocol, IotMessageBus messageBus) {
        super(protocol, messageBus);
        this.downstreamHandler = new IotEmqxDownstreamHandler(protocol);
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        downstreamHandler.handle(message);
    }

}
