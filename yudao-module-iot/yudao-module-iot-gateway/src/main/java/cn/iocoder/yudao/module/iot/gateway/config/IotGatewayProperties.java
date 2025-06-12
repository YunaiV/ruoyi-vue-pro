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

    }

    @Data
    public static class EmqxProperties {

        /**
         * 是否开启
         */
        @NotNull(message = "是否开启不能为空")
        private Boolean enabled;

        /**
         * HTTP 认证端口（默认：8090）
         */
        private Integer httpAuthPort = 8090;

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
         * MQTT 是否开启 SSL（默认：false）
         */
        @NotNull(message = "MQTT 是否开启 SSL 不能为空")
        private Boolean mqttSsl = false;

        /**
         * MQTT 客户端 ID（如果为空，系统将自动生成）
         */
        private String mqttClientId;

        /**
         * MQTT 主题列表
         */
        @NotEmpty(message = "MQTT 主题不能为空")
        private List<String> mqttTopics;

        /**
         * 获取 MQTT 客户端 ID，如果未配置则自动生成
         *
         * @return MQTT 客户端 ID
         */
        public String getMqttClientId() {
            if (cn.hutool.core.util.StrUtil.isBlank(mqttClientId)) {
                mqttClientId = "iot-gateway-mqtt-" + System.currentTimeMillis();
            }
            return mqttClientId;
        }

    }

}
