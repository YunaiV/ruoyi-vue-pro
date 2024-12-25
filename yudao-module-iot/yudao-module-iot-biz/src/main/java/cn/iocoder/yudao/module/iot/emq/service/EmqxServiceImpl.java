package cn.iocoder.yudao.module.iot.emq.service;

import cn.iocoder.yudao.module.iot.service.device.IotDevicePropertyDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Async;
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

    @Resource
    private IotDevicePropertyDataService iotDeviceDataService;

    // TODO 多线程处理消息
    @Override
    @Async
    public void subscribeCallback(String topic, MqttMessage mqttMessage) {
        log.info("收到消息，主题: {}, 内容: {}", topic, new String(mqttMessage.getPayload()));
        // 根据不同的主题，处理不同的业务逻辑
        if (topic.contains("/property/post")) {
            // 设备上报数据 topic /sys/f13f57c63e9/dianbiao1/thing/event/property/post
            // TODO @hao：这块未来可能，搞个 IotTopicUrls 之类？把拼接和解析的逻辑，收敛
            String productKey = topic.split("/")[2];
            String deviceName = topic.split("/")[3];
            String message = new String(mqttMessage.getPayload());
            iotDeviceDataService.saveDeviceData(productKey, deviceName, message);
        }
    }

    @Override
    public void subscribe(MqttClient client) {
        try {
            // 订阅默认主题，可以根据需要修改
            client.subscribe("/sys/+/+/#", 1);
            log.info("订阅默认主题成功");
        } catch (Exception e) {
            log.error("订阅默认主题失败", e);
        }
    }

}
