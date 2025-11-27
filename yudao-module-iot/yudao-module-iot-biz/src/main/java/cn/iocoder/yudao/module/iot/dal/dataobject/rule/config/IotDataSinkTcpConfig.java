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
    private Integer connectTimeoutMs = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeoutMs = 10000;

    /**
     * 是否启用 SSL
     */
    private Boolean ssl = false;

    /**
     * SSL 证书路径（当 ssl=true 时需要）
     */
    private String sslCertPath;

    /**
     * 数据格式：JSON 或 BINARY
     */
    private String dataFormat = "JSON";

    /**
     * 心跳间隔时间（毫秒），0 表示不启用心跳
     */
    private Long heartbeatIntervalMs = 30000L;

    /**
     * 重连间隔时间（毫秒）
     */
    private Long reconnectIntervalMs = 5000L;

    /**
     * 最大重连次数
     */
    private Integer maxReconnectAttempts = 3;

}