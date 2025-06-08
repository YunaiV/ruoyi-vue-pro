package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议的路由处理器抽象基类
 * <p>
 * 提供通用的处理方法，所有 MQTT 消息处理器都应继承此类
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class IotMqttAbstractHandler {

    /**
     * 处理 MQTT 消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    public abstract void handle(String topic, String payload);

    /**
     * 解析主题，获取主题各部分
     *
     * @param topic 主题
     * @return 主题各部分数组，如果解析失败返回 null
     */
    protected String[] parseTopic(String topic) {
        String[] topicParts = topic.split("/");
        if (topicParts.length < 7) {
            log.warn("[parseTopic][主题格式不正确][topic: {}]", topic);
            return null;
        }
        return topicParts;
    }

}