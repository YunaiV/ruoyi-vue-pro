package cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 HTTP 订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */

@Slf4j
public class IotHttpDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    public IotHttpDownstreamSubscriber(IotProtocol protocol, IotMessageBus messageBus) {
        super(protocol, messageBus);
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        log.info("[handleMessage][IoT 网关 HTTP 协议不支持下行消息，忽略消息：{}]", message);
    }

}
