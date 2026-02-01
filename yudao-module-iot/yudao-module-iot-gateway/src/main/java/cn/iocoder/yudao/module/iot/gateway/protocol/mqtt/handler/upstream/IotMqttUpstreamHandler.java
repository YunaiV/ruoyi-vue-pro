package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 上行消息处理器
 * <p>
 * 处理业务消息（属性上报、事件上报等）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttUpstreamHandler extends IotMqttAbstractHandler {

    private final String serverId;

    public IotMqttUpstreamHandler(IotMqttConnectionManager connectionManager,
                                  IotDeviceMessageService deviceMessageService,
                                  String serverId) {
        super(connectionManager, deviceMessageService);
        this.serverId = serverId;
    }

    /**
     * 处理业务消息
     *
     * @param endpoint MQTT 连接端点
     * @param topic    主题
     * @param payload  消息内容
     */
    public void handleMessage(MqttEndpoint endpoint, String topic, byte[] payload) {
        String clientId = endpoint.clientIdentifier();

        // 1. 基础检查
        if (payload == null || payload.length == 0) {
            return;
        }

        // 2. 解析主题，获取 productKey 和 deviceName
        String[] topicParts = topic.split("/");
        if (topicParts.length < 4 || StrUtil.hasBlank(topicParts[2], topicParts[3])) {
            log.warn("[handleMessage][topic({}) 格式不正确，无法解析有效的 productKey 和 deviceName]", topic);
            return;
        }

        // 3. 解码消息（使用从 topic 解析的 productKey 和 deviceName）
        String productKey = topicParts[2];
        String deviceName = topicParts[3];
        try {
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(payload, productKey, deviceName);
            if (message == null) {
                log.warn("[handleMessage][消息解码失败，客户端 ID: {}，主题: {}]", clientId, topic);
                return;
            }

            // 4. 处理业务消息（认证已在连接时完成）
            log.info("[handleMessage][收到设备消息，设备: {}.{}, 方法: {}]",
                    productKey, deviceName, message.getMethod());
            handleBusinessRequest(message, productKey, deviceName);
        } catch (Exception e) {
            // TODO @AI：各种情况下的翻译；
            log.error("[handleMessage][消息处理异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage(), e);
        }
    }

    /**
     * 处理业务请求
     */
    private void handleBusinessRequest(IotDeviceMessage message, String productKey, String deviceName) {
        // 发送消息到消息总线
        message.setServerId(serverId);
        deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
    }

}
