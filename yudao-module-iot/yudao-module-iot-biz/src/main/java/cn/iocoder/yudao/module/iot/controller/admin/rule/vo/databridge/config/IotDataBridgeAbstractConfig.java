package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * IoT IotDataBridgeConfig 抽象类
 *
 * 用于表示数据桥梁配置数据的通用类型，根据具体的 "type" 字段动态映射到对应的子类
 * 提供多态支持，适用于不同类型的数据结构序列化和反序列化场景。
 *
 * @author HUIHUI
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = IotDataBridgeHttpConfig.class, name = "1"),
        @JsonSubTypes.Type(value = IotDataBridgeMqttConfig.class, name = "10"),
        @JsonSubTypes.Type(value = IotDataBridgeRedisStreamMQConfig.class, name = "21"),
        @JsonSubTypes.Type(value = IotDataBridgeRocketMQConfig.class, name = "30"),
        @JsonSubTypes.Type(value = IotDataBridgeRabbitMQConfig.class, name = "31"),
        @JsonSubTypes.Type(value = IotDataBridgeKafkaMQConfig.class, name = "32"),
})
public abstract class IotDataBridgeAbstractConfig {

    /**
     * 配置类型
     *
     * 枚举 {@link IotDataBridgeTypeEnum#getType()}
     */
    private String type;

}
