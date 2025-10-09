package cn.iocoder.yudao.module.iot.gateway.protocol.mqttws;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.router.IotMqttWsDownstreamHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT MQTT WebSocket 下行消息订阅器
 * <p>
 * 订阅消息总线的设备下行消息，并通过 WebSocket 发送到设备
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttWsDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotMqttWsUpstreamProtocol upstreamProtocol;
    private final IotMqttWsDownstreamHandler downstreamHandler;
    private final IotMessageBus messageBus;

    public IotMqttWsDownstreamSubscriber(IotMqttWsUpstreamProtocol upstreamProtocol,
                                         IotMqttWsDownstreamHandler downstreamHandler,
                                         IotMessageBus messageBus) {
        this.upstreamProtocol = upstreamProtocol;
        this.downstreamHandler = downstreamHandler;
        this.messageBus = messageBus;
    }

    @PostConstruct
    public void init() {
        messageBus.register(this);
        log.info("[init][MQTT WebSocket 下行消息订阅器已启动，topic: {}]", getTopic());
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
        log.debug("[onMessage][收到下行消息，deviceId: {}，method: {}]",
                message.getDeviceId(), message.getMethod());
        try {
            // 1. 校验
            String method = message.getMethod();
            if (StrUtil.isBlank(method)) {
                log.warn("[onMessage][消息方法为空，deviceId: {}]", message.getDeviceId());
                return;
            }

            // 2. 委托给下行处理器处理业务逻辑
            boolean success = downstreamHandler.handleDownstreamMessage(message);
            if (success) {
                log.debug("[onMessage][下行消息处理成功，deviceId: {}，method: {}]",
                        message.getDeviceId(), message.getMethod());
            } else {
                log.warn("[onMessage][下行消息处理失败，deviceId: {}，method: {}]",
                        message.getDeviceId(), message.getMethod());
            }
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败，deviceId: {}，method: {}]",
                    message.getDeviceId(), message.getMethod(), e);
        }
    }

}

