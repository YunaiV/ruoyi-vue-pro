package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import lombok.Data;

/**
 * IoT Kafka 配置 {@link IotDataBridgeAbstractConfig} 实现类
 *
 * @author HUIHUI
 */
@Data
public class IotDataBridgeKafkaMQConfig extends IotDataBridgeAbstractConfig {

    /**
     * Kafka 服务器地址
     */
    private String bootstrapServers;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 是否启用 SSL
     */
    private Boolean ssl;

    /**
     * 主题
     */
    private String topic;

}
