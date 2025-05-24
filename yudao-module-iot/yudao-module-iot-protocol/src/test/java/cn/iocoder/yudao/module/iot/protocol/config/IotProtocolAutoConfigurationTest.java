package cn.iocoder.yudao.module.iot.protocol.config;

import cn.iocoder.yudao.module.iot.protocol.convert.IotProtocolConverter;
import cn.iocoder.yudao.module.iot.protocol.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotHttpMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotMqttMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotProtocolAutoConfiguration} 单元测试
 *
 * @author haohao
 */
class IotProtocolAutoConfigurationTest {

    private IotProtocolAutoConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new IotProtocolAutoConfiguration();
    }

    @Test
    void testIotMqttMessageParser() {
        // 测试 MQTT 协议解析器 Bean 创建
        IotMessageParser parser = configuration.iotMqttMessageParser();

        assertNotNull(parser);
        assertInstanceOf(IotMqttMessageParser.class, parser);
    }

    @Test
    void testIotHttpMessageParser() {
        // 测试 HTTP 协议解析器 Bean 创建
        IotMessageParser parser = configuration.iotHttpMessageParser();

        assertNotNull(parser);
        assertInstanceOf(IotHttpMessageParser.class, parser);
    }

    @Test
    void testIotProtocolConverter() {
        // 创建解析器实例
        IotMessageParser mqttParser = configuration.iotMqttMessageParser();
        IotMessageParser httpParser = configuration.iotHttpMessageParser();

        // 测试协议转换器 Bean 创建
        IotProtocolConverter converter = configuration.iotProtocolConverter(mqttParser, httpParser);

        assertNotNull(converter);

        // 验证支持的协议
        assertTrue(converter.supportsProtocol(IotProtocolTypeEnum.MQTT.getCode()));
        assertTrue(converter.supportsProtocol(IotProtocolTypeEnum.HTTP.getCode()));

        // 验证支持的协议数量
        String[] supportedProtocols = converter.getSupportedProtocols();
        assertEquals(2, supportedProtocols.length);
    }

    @Test
    void testBeanNameConstants() {
        // 测试 Bean 名称常量定义
        assertEquals("iotMqttMessageParser", IotProtocolAutoConfiguration.IOT_MQTT_MESSAGE_PARSER_BEAN_NAME);
        assertEquals("iotHttpMessageParser", IotProtocolAutoConfiguration.IOT_HTTP_MESSAGE_PARSER_BEAN_NAME);
    }
}