package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config;

import lombok.Data;

/**
 * IoT MQTT 配置 {@link IotDataBridgeAbstractConfig} 实现类
 *
 * @author HUIHUI
 */
@Data
public class IotDataBridgeMqttConfig extends IotDataBridgeAbstractConfig {

    /**
     * MQTT 服务器地址
     */
    private String url;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 客户端编号
     */
    private String clientId;
    /**
     * 主题
     */
    private String topic;

}