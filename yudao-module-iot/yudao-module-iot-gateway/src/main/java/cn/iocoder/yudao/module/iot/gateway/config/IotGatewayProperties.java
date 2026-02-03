package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketConfig;
import jakarta.validation.Valid;
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
     * 协议配置（旧版，保持兼容）
     */
    private ProtocolProperties protocol;

    /**
     * 协议实例列表
     */
    private List<ProtocolInstanceProperties> protocols;

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
         * EMQX 组件配置
         */
        private EmqxProperties emqx;

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

    // NOTE：暂未统一为 ProtocolProperties，待协议改造完成再调整
    /**
     * 协议实例配置
     */
    @Data
    public static class ProtocolInstanceProperties {

        /**
         * 协议实例 ID，如 "http-alink"、"tcp-binary"
         */
        @NotEmpty(message = "协议实例 ID 不能为空")
        private String id;
        /**
         * 是否启用
         */
        @NotNull(message = "是否启用不能为空")
        private Boolean enabled = true;
        /**
         * 协议类型
         *
         * @see cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum
         */
        @NotEmpty(message = "协议类型不能为空")
        private String protocol;
        /**
         * 服务端口
         */
        @NotNull(message = "服务端口不能为空")
        private Integer port;
        /**
         * 序列化类型（可选）
         *
         * @see cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum
         *
         * 为什么是可选的呢？
         * 1. {@link IotProtocolTypeEnum#HTTP}、${@link IotProtocolTypeEnum#COAP} 协议，目前强制是 JSON 格式
         * 2. {@link IotProtocolTypeEnum#EMQX} 协议，目前支持根据产品（设备）配置的序列化类型来解析
         */
        private String serialize;

        // ========== 各协议配置 ==========

        /**
         * HTTP 协议配置
         */
        @Valid
        private IotHttpConfig http;

        /**
         * TCP 协议配置
         */
        @Valid
        private IotTcpConfig tcp;

        /**
         * UDP 协议配置
         */
        @Valid
        private IotUdpConfig udp;

        /**
         * CoAP 协议配置
         */
        @Valid
        private IotCoapConfig coap;

        /**
         * WebSocket 协议配置
         */
        @Valid
        private IotWebSocketConfig websocket;

        /**
         * MQTT 协议配置
         */
        @Valid
        private IotMqttConfig mqtt;

    }

}
