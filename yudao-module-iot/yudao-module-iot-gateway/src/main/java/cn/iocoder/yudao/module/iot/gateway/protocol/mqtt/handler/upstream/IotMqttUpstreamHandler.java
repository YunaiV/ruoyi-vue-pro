package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 网关 MQTT 上行消息处理器：处理业务消息（属性上报、事件上报等）
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
    public void handleBusinessRequest(MqttEndpoint endpoint, String topic, byte[] payload) {
        String clientId = endpoint.clientIdentifier();
        IotDeviceMessage message = null;
        String productKey = null;
        String deviceName = null;

        try {
            // 1.1 基础检查
            if (ArrayUtil.isEmpty(payload)) {
                return;
            }
            // 1.2 解析主题，获取 productKey 和 deviceName
            String[] topicParts = topic.split("/");
            productKey = ArrayUtil.get(topicParts, 2);
            deviceName = ArrayUtil.get(topicParts, 3);
            Assert.notBlank(productKey, "产品 Key 不能为空");
            Assert.notBlank(deviceName, "设备名称不能为空");
            // 1.3 校验设备信息，防止伪造设备消息
            IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(endpoint);
            Assert.notNull(connectionInfo, "无法获取连接信息");
            Assert.equals(productKey, connectionInfo.getProductKey(), "产品 Key 不匹配");
            Assert.equals(deviceName, connectionInfo.getDeviceName(), "设备名称不匹配");

            // 2. 反序列化消息
            message = deviceMessageService.decodeDeviceMessage(payload, productKey, deviceName);
            if (message == null) {
                log.warn("[handleBusinessRequest][消息解码失败，客户端 ID: {}，主题: {}]", clientId, topic);
                sendErrorResponse(endpoint, productKey, deviceName, null, null,
                        BAD_REQUEST.getCode(), "消息解码失败");
                return;
            }

            // 3. 处理业务消息
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
            log.debug("[handleBusinessRequest][消息处理成功，客户端 ID: {}，主题: {}]", clientId, topic);
        } catch (ServiceException e) {
            log.warn("[handleBusinessRequest][业务异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(endpoint, productKey, deviceName, requestId, method, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[handleBusinessRequest][参数校验失败，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage());
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(endpoint, productKey, deviceName, requestId, method,
                    BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[handleBusinessRequest][消息处理异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage(), e);
            String requestId = message != null ? message.getRequestId() : null;
            String method = message != null ? message.getMethod() : null;
            sendErrorResponse(endpoint, productKey, deviceName, requestId, method,
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
        }
    }

}
