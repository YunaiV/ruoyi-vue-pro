package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import lombok.Data;

/**
 * Redis Stream MQ 配置
 *
 * @author HUIHUI
 */
@Data
public class IotDataBridgeRedisStreamMQConfig extends IotDataBridgeConfig {

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