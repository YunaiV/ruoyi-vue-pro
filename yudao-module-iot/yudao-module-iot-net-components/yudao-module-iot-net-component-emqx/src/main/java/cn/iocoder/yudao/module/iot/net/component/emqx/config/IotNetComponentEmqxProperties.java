package cn.iocoder.yudao.module.iot.net.component.emqx.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * IoT EMQX 网络组件配置属性
 *
 * @author haohao
 */
@ConfigurationProperties(prefix = "yudao.iot.component.emqx")
@Data
@Validated
public class IotNetComponentEmqxProperties {

    /**
     * 是否启用 EMQX 组件
     */
    private Boolean enabled;

    /**
     * MQTT 服务主机
     */
    @NotBlank(message = "MQTT 服务器主机不能为空")
    private String mqttHost;

    /**
     * MQTT 服务端口
     */
    @NotNull(message = "MQTT 服务器端口不能为空")
    private Integer mqttPort;

    /**
     * MQTT 服务用户名
     */
    @NotBlank(message = "MQTT 服务器用户名不能为空")
    private String mqttUsername;

    /**
     * MQTT 服务密码
     */
    @NotBlank(message = "MQTT 服务器密码不能为空")
    private String mqttPassword;

    /**
     * 是否启用 SSL
     */
    @NotNull(message = "MQTT SSL 配置不能为空")
    private Boolean mqttSsl;

    /**
     * 订阅的主题列表
     */
    @NotEmpty(message = "MQTT 订阅主题不能为空")
    private String[] mqttTopics;

    /**
     * 认证端口
     */
    @NotNull(message = "认证端口不能为空")
    private Integer authPort;

    /**
     * 重连延迟时间(毫秒)
     * <p>
     * 默认值：5000 毫秒
     */
    private Integer reconnectDelayMs = 5000;

    /**
     * 连接超时时间(毫秒)
     * <p>
     * 默认值：10000 毫秒
     */
    private Integer connectionTimeoutMs = 10000;
}