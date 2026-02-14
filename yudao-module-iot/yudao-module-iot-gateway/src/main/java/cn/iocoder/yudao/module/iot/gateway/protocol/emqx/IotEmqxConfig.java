package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * IoT EMQX 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotEmqxConfig {

    // ========== MQTT Client 配置（连接 EMQX Broker） ==========

    /**
     * MQTT 服务器地址
     */
    @NotEmpty(message = "MQTT 服务器地址不能为空")
    private String mqttHost;

    /**
     * MQTT 服务器端口（默认：1883）
     */
    @NotNull(message = "MQTT 服务器端口不能为空")
    private Integer mqttPort = 1883;

    /**
     * MQTT 用户名
     */
    @NotEmpty(message = "MQTT 用户名不能为空")
    private String mqttUsername;

    /**
     * MQTT 密码
     */
    @NotEmpty(message = "MQTT 密码不能为空")
    private String mqttPassword;

    /**
     * MQTT 客户端的 SSL 开关
     */
    @NotNull(message = "MQTT 是否开启 SSL 不能为空")
    private Boolean mqttSsl = false;

    /**
     * MQTT 客户端 ID
     */
    @NotEmpty(message = "MQTT 客户端 ID 不能为空")
    private String mqttClientId;

    /**
     * MQTT 订阅的主题
     */
    @NotEmpty(message = "MQTT 主题不能为空")
    private List<@NotEmpty(message = "MQTT 主题不能为空") String> mqttTopics;

    /**
     * 默认 QoS 级别
     * <p>
     * 0 - 最多一次
     * 1 - 至少一次
     * 2 - 刚好一次
     */
    @NotNull(message = "MQTT QoS 不能为空")
    @Min(value = 0, message = "MQTT QoS 不能小于 0")
    @Max(value = 2, message = "MQTT QoS 不能大于 2")
    private Integer mqttQos = 1;

    /**
     * 连接超时时间（秒）
     */
    @NotNull(message = "连接超时时间不能为空")
    @Min(value = 1, message = "连接超时时间不能小于 1 秒")
    private Integer connectTimeoutSeconds = 10;

    /**
     * 重连延迟时间（毫秒）
     */
    @NotNull(message = "重连延迟时间不能为空")
    @Min(value = 0, message = "重连延迟时间不能小于 0 毫秒")
    private Long reconnectDelayMs = 5000L;

    /**
     * 是否启用 Clean Session (清理会话)
     * true: 每次连接都是新会话，Broker 不保留离线消息和订阅关系。
     * 对于网关这类“永远在线”且会主动重新订阅的应用，建议为 true。
     */
    @NotNull(message = "是否启用 Clean Session 不能为空")
    private Boolean cleanSession = true;

    /**
     * 心跳间隔（秒）
     * 用于保持连接活性，及时发现网络中断。
     */
    @NotNull(message = "心跳间隔不能为空")
    @Min(value = 1, message = "心跳间隔不能小于 1 秒")
    private Integer keepAliveIntervalSeconds = 60;

    /**
     * 最大未确认消息队列大小
     * 限制已发送但未收到 Broker 确认的 QoS 1/2 消息数量，用于流量控制。
     */
    @NotNull(message = "最大未确认消息队列大小不能为空")
    @Min(value = 1, message = "最大未确认消息队列大小不能小于 1")
    private Integer maxInflightQueue = 10000;

    /**
     * 是否信任所有 SSL 证书
     * 警告：此配置会绕过证书验证，仅建议在开发和测试环境中使用！
     * 在生产环境中，应设置为 false，并配置正确的信任库。
     */
    @NotNull(message = "是否信任所有 SSL 证书不能为空")
    private Boolean trustAll = false;

    // ========== MQTT Will / SSL 高级配置 ==========

    /**
     * 遗嘱消息配置 (用于网关异常下线时通知其他系统)
     */
    @Valid
    private Will will = new Will();

    /**
     * 高级 SSL/TLS 配置 (用于生产环境)
     */
    @Valid
    private Ssl sslOptions = new Ssl();

    // ========== HTTP Hook 配置（网关提供给 EMQX 调用） ==========

    /**
     * HTTP Hook 服务配置（用于 /mqtt/auth、/mqtt/event）
     */
    @Valid
    private Http http = new Http();

    /**
     * 遗嘱消息 (Last Will and Testament)
     */
    @Data
    public static class Will {

        /**
         * 是否启用遗嘱消息
         */
        private boolean enabled = false;
        /**
         * 遗嘱消息主题
         */
        private String topic;
        /**
         * 遗嘱消息内容
         */
        private String payload;
        /**
         * 遗嘱消息 QoS 等级
         */
        @Min(value = 0, message = "遗嘱消息 QoS 不能小于 0")
        @Max(value = 2, message = "遗嘱消息 QoS 不能大于 2")
        private Integer qos = 1;
        /**
         * 遗嘱消息是否作为保留消息发布
         */
        private boolean retain = true;

    }

    /**
     * 高级 SSL/TLS 配置
     */
    @Data
    public static class Ssl {

        /**
         * 密钥库（KeyStore）路径，例如：classpath:certs/client.jks
         * 包含客户端自己的证书和私钥，用于向服务端证明身份（双向认证）。
         */
        private String keyStorePath;
        /**
         * 密钥库密码
         */
        private String keyStorePassword;
        /**
         * 信任库（TrustStore）路径，例如：classpath:certs/trust.jks
         * 包含服务端信任的 CA 证书，用于验证服务端的身份，防止中间人攻击。
         */
        private String trustStorePath;
        /**
         * 信任库密码
         */
        private String trustStorePassword;

    }

    /**
     * HTTP Hook 服务 SSL 配置
     */
    @Data
    public static class Http {

        /**
         * 是否启用 SSL
         */
        private Boolean sslEnabled = false;

        /**
         * SSL 证书路径
         */
        private String sslCertPath;

        /**
         * SSL 私钥路径
         */
        private String sslKeyPath;

    }

}
