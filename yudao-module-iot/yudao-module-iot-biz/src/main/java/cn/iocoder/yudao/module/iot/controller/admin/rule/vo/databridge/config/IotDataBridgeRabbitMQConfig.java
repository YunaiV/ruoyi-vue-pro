package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import lombok.Data;

/**
 * RabbitMQ 配置
 *
 * @author HUIHUI
 */
@Data
public class IotDataBridgeRabbitMQConfig extends IotDataBridgeConfig {

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