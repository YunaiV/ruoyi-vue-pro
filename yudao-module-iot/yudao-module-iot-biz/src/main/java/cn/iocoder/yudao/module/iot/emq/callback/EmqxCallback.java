package cn.iocoder.yudao.module.iot.emq.callback;

import cn.iocoder.yudao.module.iot.emq.client.EmqxClient;
import cn.iocoder.yudao.module.iot.emq.service.EmqxService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// TODO @芋艿：详细再瞅瞅
/**
 * 用于处理MQTT连接的回调，如连接断开、消息到达、消息发布完成、连接完成等事件。
 *
 * @author ahh
 */
@Slf4j
@Component
public class EmqxCallback implements MqttCallbackExtended {

    @Lazy
    @Resource
    private EmqxService emqxService;

    @Lazy
    @Resource
    private EmqxClient emqxClient;

    @Override
    public void connectionLost(Throwable throwable) {
        log.info("MQTT 连接断开", throwable);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        emqxService.subscribeCallback(topic, mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("消息发送成功: {}", iMqttDeliveryToken.getMessageId());
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT 已连接到服务器: {}", serverURI);
        emqxService.subscribe(emqxClient.getMqttClient());
    }
}



