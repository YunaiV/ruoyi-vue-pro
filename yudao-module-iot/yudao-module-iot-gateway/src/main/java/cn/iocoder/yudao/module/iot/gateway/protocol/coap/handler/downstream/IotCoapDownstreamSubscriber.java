package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.downstream;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapProtocol;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 CoAP 订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    public IotCoapDownstreamSubscriber(IotCoapProtocol protocol, IotMessageBus messageBus) {
        super(protocol, messageBus);
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        // 如需支持，可通过 CoAP Observe 模式实现（设备订阅资源，服务器推送变更）
        log.warn("[handleMessage][IoT 网关 CoAP 协议暂不支持下行消息，忽略消息：{}]", message);
    }

}
