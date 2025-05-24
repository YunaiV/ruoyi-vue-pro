package cn.iocoder.yudao.module.iot.protocol.config;

import cn.iocoder.yudao.module.iot.protocol.convert.IotProtocolConverter;
import cn.iocoder.yudao.module.iot.protocol.convert.impl.DefaultIotProtocolConverter;
import cn.iocoder.yudao.module.iot.protocol.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotHttpMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotMqttMessageParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IoT 协议模块自动配置类
 *
 * @author haohao
 */
@Configuration(proxyBeanMethods = false)
public class IotProtocolAutoConfiguration {

    /**
     * Bean 名称常量
     */
    public static final String IOT_MQTT_MESSAGE_PARSER_BEAN_NAME = "iotMqttMessageParser";
    public static final String IOT_HTTP_MESSAGE_PARSER_BEAN_NAME = "iotHttpMessageParser";

    /**
     * 注册 MQTT 协议消息解析器
     *
     * @return MQTT 协议消息解析器
     */
    @Bean
    @ConditionalOnMissingBean(name = IOT_MQTT_MESSAGE_PARSER_BEAN_NAME)
    public IotMessageParser iotMqttMessageParser() {
        return new IotMqttMessageParser();
    }


    /**
     * 注册 HTTP 协议消息解析器
     *
     * @return HTTP 协议消息解析器
     */
    @Bean
    @ConditionalOnMissingBean(name = IOT_HTTP_MESSAGE_PARSER_BEAN_NAME)
    public IotMessageParser iotHttpMessageParser() {
        return new IotHttpMessageParser();
    }

    /**
     * 注册默认协议转换器
     * <p>
     * 如果用户没有自定义协议转换器，则使用默认实现
     * 默认会注册 MQTT 和 HTTP 协议解析器
     *
     * @param iotMqttMessageParser MQTT 协议解析器
     * @param iotHttpMessageParser HTTP 协议解析器
     * @return 默认协议转换器
     */
    @Bean
    @ConditionalOnMissingBean
    public IotProtocolConverter iotProtocolConverter(IotMessageParser iotMqttMessageParser,
                                                     IotMessageParser iotHttpMessageParser) {
        DefaultIotProtocolConverter converter = new DefaultIotProtocolConverter();

        // 注册 MQTT 协议解析器（默认实现）
        converter.registerParser(IotProtocolTypeEnum.MQTT.getCode(), iotMqttMessageParser);

        // 注册 HTTP 协议解析器
        converter.registerParser(IotProtocolTypeEnum.HTTP.getCode(), iotHttpMessageParser);

        return converter;
    }
}