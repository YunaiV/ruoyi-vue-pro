package cn.iocoder.yudao.module.iot.protocol.convert.impl;

import cn.iocoder.yudao.module.iot.protocol.constants.IotLogConstants;
import cn.iocoder.yudao.module.iot.protocol.convert.IotProtocolConverter;
import cn.iocoder.yudao.module.iot.protocol.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.protocol.message.IotAlinkMessage;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.IotStandardResponse;
import cn.iocoder.yudao.module.iot.protocol.message.impl.IotAlinkMessageParser;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 默认 IoT 协议转换器实现
 * <p>
 * 支持多种协议的转换，可以通过注册不同的消息解析器来扩展支持的协议
 *
 * @author haohao
 */
@Slf4j
public class DefaultIotProtocolConverter implements IotProtocolConverter {

    /**
     * 消息解析器映射
     * Key: 协议类型，Value: 消息解析器
     */
    private final Map<String, IotMessageParser> parsers = new HashMap<>();

    /**
     * 构造函数，初始化默认支持的协议
     */
    public DefaultIotProtocolConverter() {
        // 注册 Alink 协议解析器
        registerParser(IotProtocolTypeEnum.ALINK.getCode(), new IotAlinkMessageParser());
    }

    /**
     * 注册消息解析器
     *
     * @param protocol 协议类型
     * @param parser   消息解析器
     */
    public void registerParser(String protocol, IotMessageParser parser) {
        parsers.put(protocol, parser);
        log.info(IotLogConstants.Converter.REGISTER_PARSER, protocol, parser.getClass().getSimpleName());
    }

    /**
     * 移除消息解析器
     *
     * @param protocol 协议类型
     */
    public void removeParser(String protocol) {
        parsers.remove(protocol);
        log.info(IotLogConstants.Converter.REMOVE_PARSER, protocol);
    }

    @Override
    public IotAlinkMessage convertToStandardMessage(String topic, byte[] payload, String protocol) {
        IotMessageParser parser = parsers.get(protocol);
        if (parser == null) {
            log.warn(IotLogConstants.Converter.UNSUPPORTED_PROTOCOL, protocol);
            return null;
        }

        try {
            return parser.parse(topic, payload);
        } catch (Exception e) {
            log.error(IotLogConstants.Converter.CONVERT_MESSAGE_FAILED, protocol, topic, e);
            return null;
        }
    }

    @Override
    public byte[] convertFromStandardResponse(IotStandardResponse response, String protocol) {
        IotMessageParser parser = parsers.get(protocol);
        if (parser == null) {
            log.warn(IotLogConstants.Converter.UNSUPPORTED_PROTOCOL, protocol);
            return new byte[0];
        }

        try {
            return parser.formatResponse(response);
        } catch (Exception e) {
            log.error(IotLogConstants.Converter.FORMAT_RESPONSE_FAILED, protocol, e);
            return new byte[0];
        }
    }

    @Override
    public boolean supportsProtocol(String protocol) {
        return parsers.containsKey(protocol);
    }

    @Override
    public String[] getSupportedProtocols() {
        Set<String> protocols = parsers.keySet();
        return protocols.toArray(new String[0]);
    }

    /**
     * 根据主题自动选择合适的协议解析器
     *
     * @param topic   主题
     * @param payload 消息负载
     * @return 解析后的标准消息，如果无法解析返回 null
     */
    public IotAlinkMessage autoConvert(String topic, byte[] payload) {
        // 遍历所有解析器，找到能处理该主题的解析器
        for (Map.Entry<String, IotMessageParser> entry : parsers.entrySet()) {
            IotMessageParser parser = entry.getValue();
            if (parser.canHandle(topic)) {
                try {
                    IotAlinkMessage message = parser.parse(topic, payload);
                    if (message != null) {
                        log.debug(IotLogConstants.Converter.AUTO_SELECT_PROTOCOL, entry.getKey(), topic);
                        return message;
                    }
                } catch (Exception e) {
                    log.debug(IotLogConstants.Converter.PROTOCOL_PARSE_FAILED_TRY_NEXT, entry.getKey(), topic);
                }
            }
        }

        log.warn(IotLogConstants.Converter.CANNOT_AUTO_RECOGNIZE_PROTOCOL, topic);
        return null;
    }
}