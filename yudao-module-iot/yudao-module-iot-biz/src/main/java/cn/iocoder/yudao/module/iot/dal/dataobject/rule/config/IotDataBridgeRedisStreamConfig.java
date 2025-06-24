package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import lombok.Data;

/**
 * IoT Redis Stream 配置 {@link IotDataBridgeAbstractConfig} 实现类
 *
 * @author HUIHUI
 */
@Data
public class IotDataBridgeRedisStreamConfig extends IotDataBridgeAbstractConfig {

    /**
     * Redis 服务器地址
     */
    private String host;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 密码
     */
    private String password;
    /**
     * 数据库索引
     */
    private Integer database;

    /**
     * 主题
     */
    private String topic;
}