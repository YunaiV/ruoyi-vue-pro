package cn.iocoder.yudao.module.iot.protocol.config;

import cn.iocoder.yudao.module.iot.protocol.convert.IotProtocolConverter;
import cn.iocoder.yudao.module.iot.protocol.convert.impl.DefaultIotProtocolConverter;
import cn.iocoder.yudao.module.iot.protocol.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotAlinkMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotHttpMessageParser;
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
    public static final String IOT_ALINK_MESSAGE_PARSER_BEAN_NAME = "iotAlinkMessageParser";
    public static final String IOT_HTTP_MESSAGE_PARSER_BEAN_NAME = "iotHttpMessageParser";

    /**
     * 注册 Alink 协议消息解析器
     *
     * @return Alink 协议消息解析器
     */
    @Bean
    @ConditionalOnMissingBean(name = IOT_ALINK_MESSAGE_PARSER_BEAN_NAME)
    public IotMessageParser iotAlinkMessageParser() {
        return new IotAlinkMessageParser();
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
     * 默认会注册 Alink 和 HTTP 协议解析器
     *
     * @param iotAlinkMessageParser Alink 协议解析器
     * @param iotHttpMessageParser  HTTP 协议解析器
     * @return 默认协议转换器
     */
    @Bean
    @ConditionalOnMissingBean
    public IotProtocolConverter iotProtocolConverter(IotMessageParser iotAlinkMessageParser,
                                                     IotMessageParser iotHttpMessageParser) {
        DefaultIotProtocolConverter converter = new DefaultIotProtocolConverter();

        // 注册 HTTP 协议解析器
        converter.registerParser(IotProtocolTypeEnum.HTTP.getCode(), iotHttpMessageParser);

        // 注意：Alink 协议解析器已经在 DefaultIotProtocolConverter 构造函数中注册
        // 如果需要使用自定义的 Alink 解析器实例，可以重新注册
        // converter.registerParser(IotProtocolTypeEnum.ALINK.getCode(),
        // iotAlinkMessageParser);

        return converter;
    }
}