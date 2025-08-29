package cn.iocoder.yudao.module.iot.gateway.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "yudao.iot.gateway")
@Validated
@Data
public class IotGatewayProperties {

    /**
     * 设备 RPC 服务配置
     */
    private RpcProperties rpc;
    /**
     * Token 配置
     */
    private TokenProperties token;

    /**
     * 协议配置
     */
    private ProtocolProperties protocol;

    @Data
    public static class RpcProperties {

        /**
         * 主程序 API 地址
         */
        @NotEmpty(message = "主程序 API 地址不能为空")
        private String url;
        /**
         * 连接超时时间
         */
        @NotNull(message = "连接超时时间不能为空")
        private Duration connectTimeout;
        /**
         * 读取超时时间
         */
        @NotNull(message = "读取超时时间不能为空")
        private Duration readTimeout;

    }

    @Data
    public static class TokenProperties {

        /**
         * 密钥
         */
        @NotEmpty(message = "密钥不能为空")
        private String secret;
        /**
         * 令牌有效期
         */
        @NotNull(message = "令牌有效期不能为空")
        private Duration expiration;

    }

    @Data
    public static class ProtocolProperties {

        /**
         * HTTP 组件配置
         */
        private HttpProperties http;

        /**
         * EMQX 组件配置
         */
        private EmqxProperties emqx;

        /**
         * TCP 组件配置
         */
        private TcpProperties tcp;

        /**
         * MQTT 组件配置
         */
        private MqttProperties mqtt;

    }

    @Data
    public static class HttpProperties {

        /**
         * 是否开启
         */
        @NotNull(message = "是否开启不能为空")
        private Boolean enabled;
        /**
         * 服务端口
         */
        private Integer serverPort;

        /**
         * 是否开启 SSL
         */
        @NotNull(message = "是否开启 SSL 不能为空")
        private Boolean sslEnabled = false;

        /**
         * SSL 证书路径
         */
        private String sslKeyPath;
        /**
         * SSL 证书路径
         */
        private String sslCertPath;

    }

    @Data
    public static class EmqxProperties {

        /**
         * 是否开启
         */
        @NotNull(message = "是否开启不能为空")
        private Boolean enabled;

        /**
         * HTTP 服务端口（默认：8090）
         */
        private Integer httpPort = 8090;

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
         * MQTT 客户端 ID（如果为空，系统将自动生成）
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
        private Integer mqttQos = 1;

        /**
         * 连接超时时间（秒）
         */
        private Integer connectTimeoutSeconds = 10;

        /**
         * 重连延迟时间（毫秒）
         */
        private Long reconnectDelayMs = 5000L;

        /**
         * 是否启用 Clean Session (清理会话)
         * true: 每次连接都是新会话，Broker 不保留离线消息和订阅关系。
         * 对于网关这类“永远在线”且会主动重新订阅的应用，建议为 true。
         */
        private Boolean cleanSession = true;

        /**
         * 心跳间隔（秒）
         * 用于保持连接活性，及时发现网络中断。
         */
        private Integer keepAliveIntervalSeconds = 60;

        /**
         * 最大未确认消息队列大小
         * 限制已发送但未收到 Broker 确认的 QoS 1/2 消息数量，用于流量控制。
         */
        private Integer maxInflightQueue = 10000;

        /**
         * 是否信任所有 SSL 证书
         * 警告：此配置会绕过证书验证，仅建议在开发和测试环境中使用！
         * 在生产环境中，应设置为 false，并配置正确的信任库。
         */
        private Boolean trustAll = false;

        /**
         * 遗嘱消息配置 (用于网关异常下线时通知其他系统)
         */
        private final Will will = new Will();

        /**
         * 高级 SSL/TLS 配置 (用于生产环境)
         */
        private final Ssl sslOptions = new Ssl();

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

    }

    @Data
    public static class TcpProperties {

        /**
         * 是否开启
         */
        @NotNull(message = "是否开启不能为空")
        private Boolean enabled;

        /**
         * 服务器端口
         */
        private Integer port = 8091;

        /**
         * 心跳超时时间（毫秒）
         */
        private Long keepAliveTimeoutMs = 30000L;

        /**
         * 最大连接数
         */
        private Integer maxConnections = 1000;

        /**
         * 是否启用SSL
         */
        private Boolean sslEnabled = false;

        /**
         * SSL证书路径
         */
        private String sslCertPath;

        /**
         * SSL私钥路径
         */
        private String sslKeyPath;

    }

    @Data
    public static class MqttProperties {

        /**
         * 是否开启
         */
        @NotNull(message = "是否开启不能为空")
        private Boolean enabled;

        /**
         * 服务器端口
         */
        private Integer port = 1883;

        /**
         * 最大消息大小（字节）
         */
        private Integer maxMessageSize = 8192;

        /**
         * 连接超时时间（秒）
         */
        private Integer connectTimeoutSeconds = 60;
        /**
         * 保持连接超时时间（秒）
         */
        private Integer keepAliveTimeoutSeconds = 300;

        /**
         * 是否启用 SSL
         */
        private Boolean sslEnabled = false;
        /**
         * SSL 配置
         */
        private SslOptions sslOptions = new SslOptions();

        /**
         * SSL 配置选项
         */
        @Data
        public static class SslOptions {

            /**
             * 密钥证书选项
             */
            private io.vertx.core.net.KeyCertOptions keyCertOptions;
            /**
             * 信任选项
             */
            private io.vertx.core.net.TrustOptions trustOptions;
            /**
             * SSL 证书路径
             */
            private String certPath;
            /**
             * SSL 私钥路径
             */
            private String keyPath;
            /**
             * 信任存储路径
             */
            private String trustStorePath;
            /**
             * 信任存储密码
             */
            private String trustStorePassword;

        }

    }

}
