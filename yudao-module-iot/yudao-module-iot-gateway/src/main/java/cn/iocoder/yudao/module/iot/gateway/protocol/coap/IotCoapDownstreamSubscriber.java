package cn.iocoder.yudao.module.iot.gateway.protocol.coap;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 CoAP 订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotCoapDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotCoapUpstreamProtocol protocol;

    private final IotMessageBus messageBus;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
    }

    @Override
    public String getGroup() {
        // 保证点对点消费，需要保证独立的 Group，所以使用 Topic 作为 Group
        return getTopic();
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        // 如需支持，可通过 CoAP Observe 模式实现（设备订阅资源，服务器推送变更）
        log.warn("[onMessage][IoT 网关 CoAP 协议暂不支持下行消息，忽略消息：{}]", message);
    }

}
