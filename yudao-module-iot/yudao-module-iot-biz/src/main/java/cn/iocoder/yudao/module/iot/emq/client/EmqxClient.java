package cn.iocoder.yudao.module.iot.emq.client;

import cn.iocoder.yudao.module.iot.emq.callback.EmqxCallback;
import cn.iocoder.yudao.module.iot.emq.config.MqttConfig;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

/**
 * MQTT客户端类，负责建立与MQTT服务器的连接，提供发布消息和订阅主题的功能
 *
 * @author ahh
 */
@Slf4j
@Data
@Component
public class EmqxClient {

    @Resource
    private EmqxCallback emqxCallback;
    @Resource
    private MqttConfig mqttConfig;

    private MqttClient mqttClient;

    public void connect() {
        if (mqttClient == null) {
            createMqttClient();
        }
        try {
            mqttClient.connect(createMqttOptions());
            log.info("MQTT客户端连接成功");
        } catch (MqttException e) {
            log.error("MQTT客户端连接失败", e);
        }
    }

    private void createMqttClient() {
        try {
            mqttClient = new MqttClient(mqttConfig.getHostUrl(), "yudao" + mqttConfig.getClientId(), new MemoryPersistence());
            mqttClient.setCallback(emqxCallback);
        } catch (MqttException e) {
            log.error("创建MQTT客户端失败", e);
        }
    }

    private MqttConnectOptions createMqttOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqttConfig.getUsername());
        options.setPassword(mqttConfig.getPassword().toCharArray());
        options.setConnectionTimeout(mqttConfig.getTimeout());
        options.setKeepAliveInterval(mqttConfig.getKeepalive());
        options.setCleanSession(mqttConfig.isClearSession());
        return options;
    }

    public void publish(String topic, String message) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                connect();
            }
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
            log.info("消息已发布到主题: {}", topic);
        } catch (MqttException e) {
            log.error("消息发布失败", e);
        }
    }

    public void subscribe(String topic) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                connect();
            }
            mqttClient.subscribe(topic);
            log.info("订阅了主题: {}", topic);
        } catch (MqttException e) {
            log.error("订阅主题失败", e);
        }
    }
}