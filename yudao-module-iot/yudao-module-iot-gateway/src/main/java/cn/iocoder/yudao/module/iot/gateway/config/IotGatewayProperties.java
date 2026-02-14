package cn.iocoder.yudao.module.iot.gateway.config;

import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.IotModbusTcpClientConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.IotModbusTcpServerConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.IotWebSocketConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
     * 协议实例列表
     */
    private List<ProtocolProperties> protocols;

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

    /**
     * 协议实例配置
     */
    @Data
    public static class ProtocolProperties {

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
         * <p>
         * 不同协议含义不同：
         * 1. TCP/UDP/HTTP/WebSocket/MQTT/CoAP：对应网关自身监听的服务端口
         * 2. EMQX：对应网关提供给 EMQX 回调的 HTTP Hook 端口（/mqtt/auth、/mqtt/acl、/mqtt/event）
         */
        @NotNull(message = "服务端口不能为空")
        private Integer port;
        /**
         * 序列化类型（可选）
         *
         * @see cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum
         *
         * 为什么是可选的呢？
         * 1. {@link IotProtocolTypeEnum#HTTP}、{@link IotProtocolTypeEnum#COAP} 协议，目前强制是 JSON 格式
         * 2. {@link IotProtocolTypeEnum#EMQX} 协议，目前支持根据产品（设备）配置的序列化类型来解析
         */
        private String serialize;

        // ========== SSL 配置 ==========

        /**
         * SSL 配置（可选，配置文件中不配置则为 null）
         */
        @Valid
        private SslConfig ssl;

        // ========== 各协议配置 ==========

        /**
         * HTTP 协议配置
         */
        @Valid
        private IotHttpConfig http;
        /**
         * WebSocket 协议配置
         */
        @Valid
        private IotWebSocketConfig websocket;

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
         * MQTT 协议配置
         */
        @Valid
        private IotMqttConfig mqtt;
        /**
         * EMQX 协议配置
         */
        @Valid
        private IotEmqxConfig emqx;

        /**
         * Modbus TCP Client 协议配置
         */
        @Valid
        private IotModbusTcpClientConfig modbusTcpClient;

        /**
         * Modbus TCP Server 协议配置
         */
        @Valid
        private IotModbusTcpServerConfig modbusTcpServer;

    }

    /**
     * SSL 配置
     */
    @Data
    public static class SslConfig {

        /**
         * 是否启用 SSL
         */
        @NotNull(message = "是否启用 SSL 不能为空")
        private Boolean ssl = false;

        /**
         * SSL 证书路径
         */
        @NotEmpty(message = "SSL 证书路径不能为空")
        private String sslCertPath;

        /**
         * SSL 私钥路径
         */
        @NotEmpty(message = "SSL 私钥路径不能为空")
        private String sslKeyPath;

        /**
         * 密钥库（KeyStore）路径
         * <p>
         * 包含客户端自己的证书和私钥，用于向服务端证明身份（双向认证）
         */
        private String keyStorePath;
        /**
         * 密钥库密码
         */
        private String keyStorePassword;

        /**
         * 信任库（TrustStore）路径
         * <p>
         * 包含服务端信任的 CA 证书，用于验证服务端的身份
         */
        private String trustStorePath;
        /**
         * 信任库密码
         */
        private String trustStorePassword;

    }

}
