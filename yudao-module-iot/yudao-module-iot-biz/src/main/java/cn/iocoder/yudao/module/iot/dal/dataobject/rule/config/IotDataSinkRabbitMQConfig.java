package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import lombok.Data;

/**
 * IoT RabbitMQ 配置 {@link IotAbstractDataSinkConfig} 实现类
 *
 * @author HUIHUI
 */
@Data
public class IotDataSinkRabbitMQConfig extends IotAbstractDataSinkConfig {

    /**
     * RabbitMQ 服务器地址
     */
    private String host;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 虚拟主机
     */
    private String virtualHost;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    /**
     * 交换机名称
     */
    private String exchange;
    /**
     * 路由键
     */
    private String routingKey;
    /**
     * 队列名称
     */
    private String queue;
}