package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * 抽象类 IotDataBridgeConfig
 *
 * 用于表示数据桥梁配置数据的通用类型，根据具体的 "type" 字段动态映射到对应的子类。
 * 提供多态支持，适用于不同类型的数据结构序列化和反序列化场景。
 *
 * @author HUIHUI
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = IotDataBridgeHttpConfig.class, name = "HTTP"),
        @JsonSubTypes.Type(value = IotDataBridgeKafkaMQConfig.class, name = "KAFKA"),
        @JsonSubTypes.Type(value = IotDataBridgeMqttConfig.class, name = "MQTT"),
        @JsonSubTypes.Type(value = IotDataBridgeRabbitMQConfig.class, name = "RABBITMQ"),
        @JsonSubTypes.Type(value = IotDataBridgeRedisStreamMQConfig.class, name = "REDIS_STREAM"),
        @JsonSubTypes.Type(value = IotDataBridgeRocketMQConfig.class, name = "ROCKETMQ"),
})
public abstract class IotDataBridgeAbstractConfig {

    /**
     * 配置类型
     */
    private String type;

}
