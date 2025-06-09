package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

/**
 * IoT 设备【消息】的工具类
 *
 * @author 芋道源码
 */
public class IotDeviceMessageUtils {

    // ========== Message 相关 ==========

    public static String generateMessageId() {
        return IdUtil.fastSimpleUUID();
    }

    // TODO @芋艿：需要优化下；
    /**
     * 是否是上行消息：由设备发送
     *
     * @param message 消息
     * @return 是否
     */
    public static boolean isUpstreamMessage(IotDeviceMessage message) {
        return StrUtil.isNotEmpty(message.getServerId());
    }

    // ========== Topic 相关 ==========

    public static String buildMessageBusGatewayDeviceMessageTopic(String serverId) {
        return String.format(IotDeviceMessage.MESSAGE_BUS_GATEWAY_DEVICE_MESSAGE_TOPIC, serverId);
    }

    /**
     * 生成服务器编号
     *
     * @param serverPort 服务器端口
     * @return 服务器编号
     */
    public static String generateServerId(Integer serverPort) {
        String serverId = String.format("%s.%d", SystemUtil.getHostInfo().getAddress(), serverPort);
        // 避免一些场景无法使用 . 符号，例如说 RocketMQ Topic
        return serverId.replaceAll("\\.", "_");
    }

}