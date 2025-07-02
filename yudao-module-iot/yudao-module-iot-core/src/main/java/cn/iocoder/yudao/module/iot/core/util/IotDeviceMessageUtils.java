package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

import java.util.Map;

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

    /**
     * 是否是上行消息：由设备发送
     *
     * @param message 消息
     * @return 是否
     */
    @SuppressWarnings("SimplifiableConditionalExpression")
    public static boolean isUpstreamMessage(IotDeviceMessage message) {
        IotDeviceMessageMethodEnum methodEnum = IotDeviceMessageMethodEnum.of(message.getMethod());
        Assert.notNull(methodEnum, "无法识别的消息方法：" + message.getMethod());
        // 注意：回复消息时，需要取反
        return !isReplyMessage(message) ? methodEnum.getUpstream() : !methodEnum.getUpstream();
    }

    /**
     * 是否是回复消息，通过 {@link IotDeviceMessage#getCode()} 非空进行识别
     *
     * @param message 消息
     * @return 是否
     */
    public static boolean isReplyMessage(IotDeviceMessage message) {
        return message.getCode() != null;
    }

    /**
     * 提取消息中的标识符
     *
     * @param message 消息
     * @return 标识符
     */
    @SuppressWarnings("unchecked")
    public static String getIdentifier(IotDeviceMessage message) {
        if (message.getParams() == null) {
            return null;
        }
        if (StrUtil.equalsAny(message.getMethod(), IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod())) {
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            return MapUtil.getStr(params, "identifier");
        }  else if (StrUtil.equalsAny(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            return MapUtil.getStr(params, "state");
        }
        return null;
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