package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * IoT IotDataBridgeConfig 抽象类
 *
 * 用于表示数据目的配置数据的通用类型，根据具体的 "type" 字段动态映射到对应的子类
 * 提供多态支持，适用于不同类型的数据结构序列化和反序列化场景。
 *
 * @author HUIHUI
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = IotDataSinkHttpConfig.class, name = "1"),
        @JsonSubTypes.Type(value = IotDataSinkTcpConfig.class, name = "2"),
        @JsonSubTypes.Type(value = IotDataSinkWebSocketConfig.class, name = "3"),
        @JsonSubTypes.Type(value = IotDataSinkMqttConfig.class, name = "10"),
        @JsonSubTypes.Type(value = IotDataSinkRedisConfig.class, name = "21"),
        @JsonSubTypes.Type(value = IotDataSinkRocketMQConfig.class, name = "30"),
        @JsonSubTypes.Type(value = IotDataSinkRabbitMQConfig.class, name = "31"),
        @JsonSubTypes.Type(value = IotDataSinkKafkaConfig.class, name = "32"),
})
public abstract class IotAbstractDataSinkConfig {

    /**
     * 配置类型
     *
     * 枚举 {@link IotDataSinkTypeEnum#getType()}
     */
    private String type;

}
