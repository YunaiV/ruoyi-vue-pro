package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import lombok.Data;

/**
 * IoT TCP 配置 {@link IotAbstractDataSinkConfig} 实现类
 *
 * @author HUIHUI
 */
@Data
public class IotDataSinkTcpConfig extends IotAbstractDataSinkConfig {

    /**
     * 默认连接超时时间（毫秒）
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    /**
     * 默认读取超时时间（毫秒）
     */
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;
    /**
     * 默认是否启用 SSL
     */
    public static final boolean DEFAULT_SSL = false;
    /**
     * 默认数据格式
     */
    public static final String DEFAULT_DATA_FORMAT = "JSON";
    /**
     * 默认心跳间隔时间（毫秒）
     */
    public static final long DEFAULT_HEARTBEAT_INTERVAL_MS = 30000L;
    /**
     * 默认重连间隔时间（毫秒）
     */
    public static final long DEFAULT_RECONNECT_INTERVAL_MS = 5000L;
    /**
     * 默认最大重连次数
     */
    public static final int DEFAULT_MAX_RECONNECT_ATTEMPTS = 3;

    /**
     * TCP 服务器地址
     */
    private String host;

    /**
     * TCP 服务器端口
     */
    private Integer port;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;

    /**
     * 是否启用 SSL
     */
    private Boolean ssl = DEFAULT_SSL;

    /**
     * SSL 证书路径（当 ssl=true 时需要）
     */
    private String sslCertPath;

    /**
     * 数据格式：JSON 或 BINARY
     */
    private String dataFormat = DEFAULT_DATA_FORMAT;

    /**
     * 心跳间隔时间（毫秒），0 表示不启用心跳
     */
    private Long heartbeatIntervalMs = DEFAULT_HEARTBEAT_INTERVAL_MS;

    /**
     * 重连间隔时间（毫秒）
     */
    private Long reconnectIntervalMs = DEFAULT_RECONNECT_INTERVAL_MS;

    /**
     * 最大重连次数
     */
    private Integer maxReconnectAttempts = DEFAULT_MAX_RECONNECT_ATTEMPTS;

}