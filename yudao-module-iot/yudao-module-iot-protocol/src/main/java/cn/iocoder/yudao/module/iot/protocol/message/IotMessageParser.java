package cn.iocoder.yudao.module.iot.protocol.message;

/**
 * IoT 消息解析器接口
 * <p>
 * 用于解析不同协议的消息内容
 *
 * @author haohao
 */
public interface IotMessageParser {

    /**
     * 解析消息
     *
     * @param topic   主题
     * @param payload 消息负载
     * @return 解析后的标准消息，如果解析失败返回 null
     */
    IotMqttMessage parse(String topic, byte[] payload);

    /**
     * 格式化响应消息
     *
     * @param response 标准响应
     * @return 格式化后的响应字节数组
     */
    byte[] formatResponse(IotStandardResponse response);

    /**
     * 检查是否能够处理指定主题的消息
     *
     * @param topic 主题
     * @return 如果能处理返回 true，否则返回 false
     */
    boolean canHandle(String topic);
} 