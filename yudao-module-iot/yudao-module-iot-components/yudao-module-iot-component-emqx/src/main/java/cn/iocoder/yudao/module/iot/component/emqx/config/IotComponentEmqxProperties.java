package cn.iocoder.yudao.module.iot.component.emqx.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IoT EMQX组件配置属性
 */
@ConfigurationProperties(prefix = "yudao.iot.component.emqx")
@Data
public class IotComponentEmqxProperties {

    /**
     * 是否启用EMQX组件
     */
    private Boolean enabled;

    /**
     * 服务主机
     */
    @NotBlank(message = "MQTT服务器主机不能为空")
    private String mqttHost;
    /**
     * 服务端口
     */
    @NotNull(message = "MQTT服务器端口不能为空")
    private Integer mqttPort;
    /**
     * 服务用户名
     */
    @NotBlank(message = "MQTT服务器用户名不能为空")
    private String mqttUsername;
    /**
     * 服务密码
     */
    @NotBlank(message = "MQTT服务器密码不能为空")
    private String mqttPassword;
    /**
     * 是否启用 SSL
     */
    @NotNull(message = "MQTT SSL配置不能为空")
    private Boolean mqttSsl;

    /**
     * 订阅的主题列表
     */
    @NotEmpty(message = "MQTT订阅主题不能为空")
    private String[] mqttTopics;

    /**
     * 认证端口
     */
    @NotNull(message = "认证端口不能为空")
    private Integer authPort;

} 