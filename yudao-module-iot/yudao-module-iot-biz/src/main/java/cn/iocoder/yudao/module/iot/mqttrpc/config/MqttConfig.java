package cn.iocoder.yudao.module.iot.mqttrpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {
    /**
     * MQTT 代理地址    
     */
    private String broker;

    /**
     * MQTT 用户名
     */
    private String username;

    /**
     * MQTT 密码
     */
    private String password;

    /**
     * MQTT 客户端 ID
     */
    private String clientId;

    /**
     * MQTT 请求主题
     */
    private String requestTopic;

    /**
     * MQTT 响应主题前缀
     */
    private String responseTopicPrefix;
}
