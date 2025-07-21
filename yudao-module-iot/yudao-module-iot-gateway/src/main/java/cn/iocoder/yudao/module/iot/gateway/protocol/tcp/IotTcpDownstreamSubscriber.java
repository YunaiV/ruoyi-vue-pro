package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router.IotTcpDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 下游订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotTcpDownstreamHandler downstreamHandler;

    private final IotMessageBus messageBus;

    private final IotTcpUpstreamProtocol protocol;

    public IotTcpDownstreamSubscriber(IotTcpUpstreamProtocol protocol,
            IotDeviceMessageService messageService,
            IotMessageBus messageBus) {
        this.protocol = protocol;
        this.messageBus = messageBus;
        this.downstreamHandler = new IotTcpDownstreamHandler(messageService);
    }

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
        log.debug("[onMessage][接收到下行消息, messageId: {}, method: {}, deviceId: {}]",
                message.getId(), message.getMethod(), message.getDeviceId());
        try {
            // 1. 校验
            String method = message.getMethod();
            if (method == null) {
                log.warn("[onMessage][消息方法为空, messageId: {}, deviceId: {}]",
                        message.getId(), message.getDeviceId());
                return;
            }

            // 2. 处理下行消息
            downstreamHandler.handle(message);
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败, messageId: {}, method: {}, deviceId: {}]",
                    message.getId(), message.getMethod(), message.getDeviceId(), e);
        }
    }
}