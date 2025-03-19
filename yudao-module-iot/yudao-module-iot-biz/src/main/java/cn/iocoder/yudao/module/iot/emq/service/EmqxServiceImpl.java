package cn.iocoder.yudao.module.iot.emq.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

// TODO @芋艿：在瞅瞅

/**
 * 用于处理MQTT消息的具体业务逻辑，如订阅回调
 *
 * @author ahh
 */
@Slf4j
@Service
public class EmqxServiceImpl implements EmqxService {

    // TODO 多线程处理消息
    @Override
    public void subscribeCallback(String topic, MqttMessage mqttMessage) {
        log.info("收到消息，主题: {}, 内容: {}", topic, new String(mqttMessage.getPayload()));
        // 根据不同的主题，处理不同的业务逻辑
        if (topic.contains("/property/post")) {
            // 设备上报数据
        }
    }

    @Override
    public void subscribe(MqttClient client) {
        try {
            // 订阅默认主题，可以根据需要修改
//            client.subscribe("$share/yudao/+/+/#", 1);
            log.info("订阅默认主题成功");
        } catch (Exception e) {
            log.error("订阅默认主题失败", e);
        }
    }
}
