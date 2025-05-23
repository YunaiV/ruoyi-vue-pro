package cn.iocoder.yudao.module.iot.protocol.config;

import cn.iocoder.yudao.module.iot.protocol.convert.IotProtocolConverter;
import cn.iocoder.yudao.module.iot.protocol.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotAlinkMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotHttpMessageParser;
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
    void testIotAlinkMessageParser() {
        // 测试 Alink 协议解析器 Bean 创建
        IotMessageParser parser = configuration.iotAlinkMessageParser();

        assertNotNull(parser);
        assertInstanceOf(IotAlinkMessageParser.class, parser);
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
        IotMessageParser alinkParser = configuration.iotAlinkMessageParser();
        IotMessageParser httpParser = configuration.iotHttpMessageParser();

        // 测试协议转换器 Bean 创建
        IotProtocolConverter converter = configuration.iotProtocolConverter(alinkParser, httpParser);

        assertNotNull(converter);

        // 验证支持的协议
        assertTrue(converter.supportsProtocol(IotProtocolTypeEnum.ALINK.getCode()));
        assertTrue(converter.supportsProtocol(IotProtocolTypeEnum.HTTP.getCode()));

        // 验证支持的协议数量
        String[] supportedProtocols = converter.getSupportedProtocols();
        assertEquals(2, supportedProtocols.length);
    }

    @Test
    void testBeanNameConstants() {
        // 测试 Bean 名称常量定义
        assertEquals("iotAlinkMessageParser", IotProtocolAutoConfiguration.IOT_ALINK_MESSAGE_PARSER_BEAN_NAME);
        assertEquals("iotHttpMessageParser", IotProtocolAutoConfiguration.IOT_HTTP_MESSAGE_PARSER_BEAN_NAME);
    }
}