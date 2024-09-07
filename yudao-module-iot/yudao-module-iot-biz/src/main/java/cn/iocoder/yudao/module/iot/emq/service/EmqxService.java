package cn.iocoder.yudao.module.iot.emq.service;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

// TODO @芋艿：在瞅瞅
/**
 * 用于处理MQTT消息的具体业务逻辑，如订阅回调
 *
 * @author ahh
 */
public interface EmqxService {

    /**
     * 订阅回调
     *
     * @param topic       主题
     * @param mqttMessage 消息
     */
    void subscribeCallback(String topic, MqttMessage mqttMessage);

    /**
     * 订阅主题
     *
     * @param client MQTT 客户端
     */
    void subscribe(MqttClient client);
}
