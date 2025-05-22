package cn.iocoder.yudao.module.iot.protocol.config;

import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotAlinkMessageParser;
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
     * 注册 Alink 协议消息解析器
     *
     * @return Alink 协议消息解析器
     */
    @Bean
    public IotMessageParser iotAlinkMessageParser() {
        return new IotAlinkMessageParser();
    }
} 