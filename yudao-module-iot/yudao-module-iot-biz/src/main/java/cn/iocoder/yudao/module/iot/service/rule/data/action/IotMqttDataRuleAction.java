package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkMqttConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 的 {@link IotDataRuleAction} 实现类
 *
 * @author HUIHUI
 */
@ConditionalOnClass(name = "org.eclipse.paho.client.mqttv3.MqttClient")
@Component
@Slf4j
public class IotMqttDataRuleAction extends
        IotDataRuleCacheableAction<IotDataSinkMqttConfig, MqttClient> {

    /**
     * 默认 QoS 等级（至少一次）
     */
    private static final int DEFAULT_QOS = 1;

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.MQTT.getType();
    }

    @Override
    public void execute(IotDeviceMessage message, IotDataSinkMqttConfig config) throws Exception {
        try {
            // 1. 获取或创建 MqttClient
            MqttClient mqttClient = getProducer(config);

            // 2.1 检查连接状态，如果断开则踢出缓存并重新创建
            if (!mqttClient.isConnected()) {
                log.warn("[execute][MQTT 连接已断开，重新创建客户端，服务器: {}]", config.getUrl());
                invalidateProducer(config); // 踢出旧的断连客户端，触发 closeProducer
                mqttClient = getProducer(config); // 触发 initProducer 创建全新连接
            }

            // 2.2 构建并发送消息
            MqttMessage mqttMessage = new MqttMessage(JsonUtils.toJsonString(message).getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(DEFAULT_QOS);
            mqttClient.publish(config.getTopic(), mqttMessage);
            log.info("[execute][message({}) 发送成功，MQTT 服务器: {}，topic: {}]",
                    message.getId(), config.getUrl(), config.getTopic());
        } catch (Exception e) {
            log.error("[execute][message({}) 发送失败，MQTT 服务器: {}]",
                    message.getId(), config.getUrl(), e);
            throw e;
        }
    }

    @Override
    protected MqttClient initProducer(IotDataSinkMqttConfig config) throws Exception {
        // 1. 创建 MqttClient，使用内存持久化
        // 拼接时间戳后缀，避免多个规则指向同一 Broker 时 clientId 冲突
        String clientId = config.getClientId() + "_" + System.currentTimeMillis();
        MqttClient mqttClient = new MqttClient(config.getUrl(), clientId, new MemoryPersistence());

        // 2. 连接到 MQTT Broker
        mqttClient.connect(buildConnectOptions(config));
        log.info("[initProducer][MQTT 客户端创建并连接成功，服务器: {}，clientId: {}]",
                config.getUrl(), clientId);
        return mqttClient;
    }

    @Override
    protected void closeProducer(MqttClient producer) throws Exception {
        if (producer.isConnected()) {
            producer.disconnect();
        }
        producer.close();
    }

    /**
     * 构建 MQTT 连接选项
     *
     * @param config MQTT 配置
     * @return 连接选项
     */
    private MqttConnectOptions buildConnectOptions(IotDataSinkMqttConfig config) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(10); // 连接超时 10 秒
        options.setKeepAliveInterval(20); // 心跳间隔 20 秒
        // 注意：不开启 automaticReconnect，由 execute() 中的 isConnected() 手动控制重连，避免竞争
        // 设置认证信息（如果有）
        if (config.getUsername() != null) {
            options.setUserName(config.getUsername());
        }
        if (config.getPassword() != null) {
            options.setPassword(config.getPassword().toCharArray());
        }
        return options;
    }

}
