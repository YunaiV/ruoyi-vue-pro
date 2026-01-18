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
     * 默认连接超时时间（毫秒）
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    /**
     * 默认发送超时时间（毫秒）
     */
    public static final int DEFAULT_SEND_TIMEOUT_MS = 10000;
    /**
     * 默认心跳间隔时间（毫秒）
     */
    public static final long DEFAULT_HEARTBEAT_INTERVAL_MS = 30000L;
    /**
     * 默认心跳消息内容
     */
    public static final String DEFAULT_HEARTBEAT_MESSAGE = "{\"type\":\"heartbeat\"}";
    /**
     * 默认是否启用 SSL 证书验证
     */
    public static final boolean DEFAULT_VERIFY_SSL_CERT = true;
    /**
     * 默认数据格式
     */
    public static final String DEFAULT_DATA_FORMAT = "JSON";
    /**
     * 默认重连间隔时间（毫秒）
     */
    public static final long DEFAULT_RECONNECT_INTERVAL_MS = 5000L;
    /**
     * 默认最大重连次数
     */
    public static final int DEFAULT_MAX_RECONNECT_ATTEMPTS = 3;
    /**
     * 默认是否启用压缩
     */
    public static final boolean DEFAULT_ENABLE_COMPRESSION = false;
    /**
     * 默认消息发送重试次数
     */
    public static final int DEFAULT_SEND_RETRY_COUNT = 1;
    /**
     * 默认消息发送重试间隔（毫秒）
     */
    public static final long DEFAULT_SEND_RETRY_INTERVAL_MS = 1000L;

    /**
     * WebSocket 服务器地址
     * 例如：ws://localhost:8080/ws 或 wss://example.com/ws
     */
    private String serverUrl;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;

    /**
     * 发送超时时间（毫秒）
     */
    private Integer sendTimeoutMs = DEFAULT_SEND_TIMEOUT_MS;

    /**
     * 心跳间隔时间（毫秒），0 表示不启用心跳
     */
    private Long heartbeatIntervalMs = DEFAULT_HEARTBEAT_INTERVAL_MS;

    /**
     * 心跳消息内容（JSON 格式）
     */
    private String heartbeatMessage = DEFAULT_HEARTBEAT_MESSAGE;

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
    private Boolean verifySslCert = DEFAULT_VERIFY_SSL_CERT;

    /**
     * 数据格式：JSON 或 TEXT
     */
    private String dataFormat = DEFAULT_DATA_FORMAT;

    /**
     * 重连间隔时间（毫秒）
     */
    private Long reconnectIntervalMs = DEFAULT_RECONNECT_INTERVAL_MS;

    /**
     * 最大重连次数
     */
    private Integer maxReconnectAttempts = DEFAULT_MAX_RECONNECT_ATTEMPTS;

    /**
     * 是否启用压缩
     */
    private Boolean enableCompression = DEFAULT_ENABLE_COMPRESSION;

    /**
     * 消息发送重试次数
     */
    private Integer sendRetryCount = DEFAULT_SEND_RETRY_COUNT;

    /**
     * 消息发送重试间隔（毫秒）
     */
    private Long sendRetryIntervalMs = DEFAULT_SEND_RETRY_INTERVAL_MS;

}