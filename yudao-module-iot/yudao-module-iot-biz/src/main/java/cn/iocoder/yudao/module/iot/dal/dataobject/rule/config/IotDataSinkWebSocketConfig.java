package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import lombok.Data;

/**
 * IoT WebSocket 配置 {@link IotAbstractDataSinkConfig} 实现类
 * <p>
 * 配置设备消息通过 WebSocket 协议发送到外部 WebSocket 服务器
 * 支持 WebSocket (ws://) 和 WebSocket Secure (wss://) 连接
 *
 * @author HUIHUI
 */
@Data
public class IotDataSinkWebSocketConfig extends IotAbstractDataSinkConfig {

    /**
     * WebSocket 服务器地址
     * 例如：ws://localhost:8080/ws 或 wss://example.com/ws
     */
    private String serverUrl;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeoutMs = 5000;

    /**
     * 发送超时时间（毫秒）
     */
    private Integer sendTimeoutMs = 10000;

    /**
     * 心跳间隔时间（毫秒），0 表示不启用心跳
     */
    private Long heartbeatIntervalMs = 30000L;

    /**
     * 心跳消息内容（JSON 格式）
     */
    private String heartbeatMessage = "{\"type\":\"heartbeat\"}";

    /**
     * 子协议列表（逗号分隔）
     */
    private String subprotocols;

    /**
     * 自定义请求头（JSON 格式）
     */
    private String customHeaders;

    /**
     * 是否启用 SSL 证书验证（仅对 wss:// 生效）
     */
    private Boolean verifySslCert = true;

    /**
     * 数据格式：JSON 或 TEXT
     */
    private String dataFormat = "JSON";

    /**
     * 重连间隔时间（毫秒）
     */
    private Long reconnectIntervalMs = 5000L;

    /**
     * 最大重连次数
     */
    private Integer maxReconnectAttempts = 3;

    /**
     * 是否启用压缩
     */
    private Boolean enableCompression = false;

    /**
     * 消息发送重试次数
     */
    private Integer sendRetryCount = 1;

    /**
     * 消息发送重试间隔（毫秒）
     */
    private Long sendRetryIntervalMs = 1000L;

}