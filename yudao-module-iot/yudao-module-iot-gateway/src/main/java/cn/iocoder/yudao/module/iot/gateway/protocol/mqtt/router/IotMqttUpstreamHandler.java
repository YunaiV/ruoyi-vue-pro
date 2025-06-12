package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * IoT 网关 MQTT 协议的【上行】处理器
 * <p>
 * 处理设备上行消息，包括事件上报、属性上报、服务调用响应等
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttUpstreamHandler {

    private final IotDeviceMessageProducer deviceMessageProducer;
    private final IotDeviceMessageService deviceMessageService;
    private final String serverId;

    public IotMqttUpstreamHandler(cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol protocol) {
        this.deviceMessageProducer = SpringUtil.getBean(IotDeviceMessageProducer.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.serverId = protocol.getServerId();
    }

    /**
     * 处理 MQTT 发布消息
     */
    public void handle(MqttPublishMessage message) {
        String topic = message.topicName();
        String payload = message.payload().toString(StandardCharsets.UTF_8);

        if (StrUtil.isBlank(topic)) {
            log.warn("[handle][主题为空，忽略消息]");
            return;
        }

        if (StrUtil.isBlank(payload)) {
            log.warn("[handle][消息内容为空][topic: {}]", topic);
            return;
        }

        log.debug("[handle][收到 MQTT 消息][topic: {}]", topic);
        handle(topic, payload);
    }

    /**
     * 处理 MQTT 消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public void handle(String topic, String payload) {
        try {
            // 1. 识别并验证消息类型
            String messageType = getMessageType(topic);
            if (messageType == null) {
                log.warn("[handle][未知的消息类型][topic: {}]", topic);
                return;
            }

            // 2. 处理消息
            processMessage(topic, payload, messageType);

        } catch (Exception e) {
            log.error("[handle][处理消息失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    /**
     * 处理消息的统一逻辑
     */
    private void processMessage(String topic, String payload, String messageType) {
        log.info("[processMessage][接收到{}][topic: {}]", messageType, topic);

        // 解析主题获取设备信息
        String[] topicParts = parseTopic(topic);
        if (topicParts == null) {
            return;
        }

        String productKey = topicParts[2];
        String deviceName = topicParts[3];

        // 解码消息
        byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
        IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(
                messageBytes, productKey, deviceName);

        // 发送消息到队列（需要补充设备信息）
        deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);

        // 记录成功日志
        log.info("[processMessage][处理{}成功][topic: {}]", messageType, topic);
    }

    /**
     * 识别消息类型
     *
     * @param topic 主题
     * @return 消息类型描述，如果不支持返回 null
     */
    private String getMessageType(String topic) {
        if (StrUtil.isBlank(topic)) {
            return null;
        }

        // 按优先级匹配主题类型，避免误匹配

        // 1. 设备属性上报: /sys/{productKey}/{deviceName}/thing/event/property/post
        if (isPropertyPostTopic(topic)) {
            return IotDeviceTopicEnum.PROPERTY_POST_TOPIC.getDescription();
        }

        // 2. 设备事件上报: /sys/{productKey}/{deviceName}/thing/event/{eventIdentifier}/post
        if (isEventPostTopic(topic)) {
            return "设备事件上报";
        }

        // 3. 设备属性设置响应: /sys/{productKey}/{deviceName}/thing/service/property/set_reply
        if (isPropertySetReplyTopic(topic)) {
            return "设备属性设置响应";
        }

        // 4. 设备属性获取响应: /sys/{productKey}/{deviceName}/thing/service/property/get_reply
        if (isPropertyGetReplyTopic(topic)) {
            return "设备属性获取响应";
        }

        // 5. 设备配置设置响应: /sys/{productKey}/{deviceName}/thing/service/config/set_reply
        if (isConfigSetReplyTopic(topic)) {
            return IotDeviceTopicEnum.CONFIG_SET_TOPIC.getDescription() + "响应";
        }

        // 6. 设备 OTA 升级响应:
        // /sys/{productKey}/{deviceName}/thing/service/ota/upgrade_reply
        if (isOtaUpgradeReplyTopic(topic)) {
            return IotDeviceTopicEnum.OTA_UPGRADE_TOPIC.getDescription() + "响应";
        }

        // 7. 其他服务调用响应: 通用服务调用响应
        if (isServiceReplyTopic(topic)) {
            return "设备服务调用响应";
        }

        // 不支持的消息类型
        return null;
    }

    /**
     * 判断是否为属性上报主题
     */
    private boolean isPropertyPostTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.PROPERTY_POST_TOPIC.getTopic());
    }

    /**
     * 判断是否为事件上报主题
     */
    private boolean isEventPostTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.EVENT_POST_TOPIC_PREFIX.getTopic())
                && topic.endsWith(IotDeviceTopicEnum.EVENT_POST_TOPIC_SUFFIX.getTopic())
                && !topic.contains("property"); // 排除属性上报主题
    }

    /**
     * 判断是否为属性设置响应主题
     */
    private boolean isPropertySetReplyTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.PROPERTY_SET_TOPIC.getTopic())
                && topic.endsWith(IotDeviceTopicEnum.REPLY_SUFFIX);
    }

    /**
     * 判断是否为属性获取响应主题
     */
    private boolean isPropertyGetReplyTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.PROPERTY_GET_TOPIC.getTopic())
                && topic.endsWith(IotDeviceTopicEnum.REPLY_SUFFIX);
    }

    /**
     * 判断是否为配置设置响应主题
     */
    private boolean isConfigSetReplyTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.CONFIG_SET_TOPIC.getTopic())
                && topic.endsWith(IotDeviceTopicEnum.REPLY_SUFFIX);
    }

    /**
     * 判断是否为 OTA 升级响应主题
     */
    private boolean isOtaUpgradeReplyTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.OTA_UPGRADE_TOPIC.getTopic())
                && topic.endsWith(IotDeviceTopicEnum.REPLY_SUFFIX);
    }

    /**
     * 判断是否为服务调用响应主题（排除已处理的特殊服务）
     */
    private boolean isServiceReplyTopic(String topic) {
        return topic.contains(IotDeviceTopicEnum.SERVICE_TOPIC_PREFIX)
                && topic.endsWith(IotDeviceTopicEnum.REPLY_SUFFIX)
                && !topic.contains("property")
                && !topic.contains("config")
                && !topic.contains("ota");
    }

    /**
     * 解析主题，获取主题各部分
     *
     * @param topic 主题
     * @return 主题各部分数组，如果解析失败返回 null
     */
    private String[] parseTopic(String topic) {
        String[] topicParts = topic.split("/");
        if (topicParts.length < 7) {
            log.warn("[parseTopic][主题格式不正确][topic: {}]", topic);
            return null;
        }
        return topicParts;
    }
}