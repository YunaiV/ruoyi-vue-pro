package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import lombok.Data;

/**
 * RocketMQ 配置
 *
 * @author HUIHUI
 */
@Data
public class IotDataBridgeRocketMQConfig extends IotDataBridgeConfig {

    /**
     * RocketMQ 名称服务器地址
     */
    private String nameServer;
    /**
     * 访问密钥
     */
    private String accessKey;
    /**
     * 秘密钥匙
     */
    private String secretKey;

    /**
     * 生产者组
     */
    private String group;
    /**
     * 主题
     */
    private String topic;
    /**
     * 标签
     */
    private String tags;

}