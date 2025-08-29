package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttDownstreamHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议：下行消息订阅器
 * <p>
 * 负责接收来自消息总线的下行消息，并委托给下行处理器进行业务处理
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotMqttUpstreamProtocol upstreamProtocol;

    private final IotMqttDownstreamHandler downstreamHandler;

    private final IotMessageBus messageBus;

    public IotMqttDownstreamSubscriber(IotMqttUpstreamProtocol upstreamProtocol,
                                       IotMqttDownstreamHandler downstreamHandler,
                                       IotMessageBus messageBus) {
        this.upstreamProtocol = upstreamProtocol;
        this.downstreamHandler = downstreamHandler;
        this.messageBus = messageBus;
    }

    @PostConstruct
    public void subscribe() {
        messageBus.register(this);
        log.info("[subscribe][MQTT 协议下行消息订阅成功，主题：{}]", getTopic());
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(upstreamProtocol.getServerId());
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

            // 2. 委托给下行处理器处理业务逻辑
            boolean success = downstreamHandler.handleDownstreamMessage(message);
            if (success) {
                log.debug("[onMessage][下行消息处理成功, messageId: {}, method: {}, deviceId: {}]",
                        message.getId(), message.getMethod(), message.getDeviceId());
            } else {
                log.warn("[onMessage][下行消息处理失败, messageId: {}, method: {}, deviceId: {}]",
                        message.getId(), message.getMethod(), message.getDeviceId());
            }
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败, messageId: {}, method: {}, deviceId: {}]",
                    message.getId(), message.getMethod(), message.getDeviceId(), e);
        }
    }
}
