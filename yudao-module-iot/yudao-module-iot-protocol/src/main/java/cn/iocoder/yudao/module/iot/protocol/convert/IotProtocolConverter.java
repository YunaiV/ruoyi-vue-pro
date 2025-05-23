package cn.iocoder.yudao.module.iot.protocol.convert;

import cn.iocoder.yudao.module.iot.protocol.message.IotAlinkMessage;
import cn.iocoder.yudao.module.iot.protocol.message.IotStandardResponse;

/**
 * IoT 协议转换器接口
 * <p>
 * 用于在不同协议之间进行转换
 *
 * @author haohao
 */
public interface IotProtocolConverter {

    /**
     * 将字节数组转换为标准消息
     *
     * @param topic    主题
     * @param payload  消息负载
     * @param protocol 协议类型
     * @return 标准消息对象，转换失败返回 null
     */
    IotAlinkMessage convertToStandardMessage(String topic, byte[] payload, String protocol);

    /**
     * 将标准响应转换为字节数组
     *
     * @param response 标准响应
     * @param protocol 协议类型
     * @return 字节数组，转换失败返回空数组
     */
    byte[] convertFromStandardResponse(IotStandardResponse response, String protocol);

    /**
     * 检查是否支持指定协议
     *
     * @param protocol 协议类型
     * @return 如果支持返回 true，否则返回 false
     */
    boolean supportsProtocol(String protocol);

    /**
     * 获取支持的协议类型列表
     *
     * @return 协议类型数组
     */
    String[] getSupportedProtocols();
}