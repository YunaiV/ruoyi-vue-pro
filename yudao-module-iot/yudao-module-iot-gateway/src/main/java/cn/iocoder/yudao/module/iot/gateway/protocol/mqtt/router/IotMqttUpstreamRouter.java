package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.message.IotDeviceMessageService;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 上行路由器
 * <p>
 * 根据消息主题路由到不同的处理器
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttUpstreamRouter {

    private final IotMqttUpstreamProtocol protocol;
    private final IotDeviceMessageProducer deviceMessageProducer;
    private final IotDeviceMessageService deviceMessageService;

    // 处理器实例
    private IotMqttPropertyHandler propertyHandler;
    private IotMqttEventHandler eventHandler;
    private IotMqttServiceHandler serviceHandler;

    public IotMqttUpstreamRouter(IotMqttUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceMessageProducer = SpringUtil.getBean(IotDeviceMessageProducer.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        // 初始化处理器
        this.propertyHandler = new IotMqttPropertyHandler(protocol, deviceMessageProducer, deviceMessageService);
        this.eventHandler = new IotMqttEventHandler(protocol, deviceMessageProducer, deviceMessageService);
        this.serviceHandler = new IotMqttServiceHandler(protocol, deviceMessageProducer, deviceMessageService);
    }

    /**
     * 路由 MQTT 消息
     *
     * @param message MQTT 发布消息
     */
    public void route(MqttPublishMessage message) {
        String topic = message.topicName();
        String payload = message.payload().toString();
        log.info("[route][接收到 MQTT 消息][topic: {}][payload: {}]", topic, payload);

        try {
            if (StrUtil.isEmpty(payload)) {
                log.warn("[route][消息内容为空][topic: {}]", topic);
                return;
            }

            // 根据主题路由到相应的处理器
            if (isPropertyTopic(topic)) {
                propertyHandler.handle(topic, payload);
            } else if (isEventTopic(topic)) {
                eventHandler.handle(topic, payload);
            } else if (isServiceTopic(topic)) {
                serviceHandler.handle(topic, payload);
            } else {
                log.warn("[route][未知的消息类型][topic: {}]", topic);
            }
        } catch (Exception e) {
            log.error("[route][处理 MQTT 消息失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    /**
     * 判断是否为属性相关主题
     *
     * @param topic 主题
     * @return 是否为属性主题
     */
    private boolean isPropertyTopic(String topic) {
        return topic.endsWith(IotDeviceTopicEnum.PROPERTY_POST_TOPIC.getTopic()) ||
                topic.contains(IotDeviceTopicEnum.PROPERTY_SET_TOPIC.getTopic()) ||
                topic.contains(IotDeviceTopicEnum.PROPERTY_GET_TOPIC.getTopic());
    }

    /**
     * 判断是否为事件相关主题
     *
     * @param topic 主题
     * @return 是否为事件主题
     */
    private boolean isEventTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.EVENT_POST_TOPIC_PREFIX.getTopic()) &&
                topic.endsWith(IotDeviceTopicEnum.EVENT_POST_TOPIC_SUFFIX.getTopic());
    }

    /**
     * 判断是否为服务相关主题
     *
     * @param topic 主题
     * @return 是否为服务主题
     */
    private boolean isServiceTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.SERVICE_TOPIC_PREFIX.getTopic()) &&
                !isPropertyTopic(topic); // 排除属性相关的服务调用
    }

}