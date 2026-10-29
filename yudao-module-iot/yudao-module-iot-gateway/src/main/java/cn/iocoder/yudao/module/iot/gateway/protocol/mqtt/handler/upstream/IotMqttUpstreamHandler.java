package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import cn.iocoder.yudao.module.iot.gateway.util.IotWorkerTaskQueue;
import io.vertx.core.Future;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;

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
     * <p>
     * 在 event loop 上校验主题与身份；反序列化、透传解析、发送消息会阻塞，交给本连接的 worker 队列按顺序执行。
     * 处理失败、校验不通过只记录日志（与原来一致，照常确认）；只有队列已满或已关闭时返回失败的 Future
     *
     * @param endpoint MQTT 连接端点
     * @param queue    本连接的 worker 队列
     * @param topic    主题
     * @param payload  消息内容
     * @return 处理完成的结果，在 event loop 上完成：调用方在它完成后再确认（PUBACK / PUBREC）
     */
    public Future<Void> handleBusinessRequest(MqttEndpoint endpoint, IotWorkerTaskQueue queue,
                                              String topic, byte[] payload) {
        String clientId = endpoint.clientIdentifier();
        String productKey;
        String deviceName;
        try {
            // 1.1 基础检查
            if (ArrayUtil.isEmpty(payload)) {
                return skip(queue);
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
            // 1.4 校验 topic 是否允许发布
            if (!IotMqttTopicUtils.isTopicPublishAllowed(topic, productKey, deviceName)) {
                log.warn("[handleBusinessRequest][topic 不允许发布，客户端 ID: {}，主题: {}]", clientId, topic);
                return skip(queue);
            }
        } catch (Exception e) {
            log.error("[handleBusinessRequest][消息校验失败，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage());
            return skip(queue);
        }

        // 2. 交给本连接的 worker 队列：透传上报的截止时间从收到（入队之前）开始计算
        boolean raw = IotMqttTopicUtils.isRawPostTopic(topic);
        long receivedNanos = System.nanoTime();
        return queue.submit(() -> {
            try {
                if (raw) {
                    handleRawRequest(productKey, deviceName, payload, receivedNanos);
                } else {
                    handleStandardRequest(clientId, topic, productKey, deviceName, payload);
                }
            } catch (Exception e) {
                log.error("[handleBusinessRequest][消息处理异常，客户端 ID: {}，主题: {}，错误: {}]",
                        clientId, topic, e.getMessage(), e);
            }
            return null;
        });
    }

    /**
     * 不处理的消息：也排在本连接之前的消息之后完成，保证确认（PUBACK / PUBREC）仍按收到的顺序
     */
    private static Future<Void> skip(IotWorkerTaskQueue queue) {
        return queue.submit(() -> null);
    }

    /**
     * 处理标准消息（worker 线程）：反序列化后发送
     */
    private void handleStandardRequest(String clientId, String topic, String productKey, String deviceName,
                                       byte[] payload) {
        // 1.1 反序列化消息
        IotDeviceMessage message = deviceMessageService.deserializeDeviceMessage(payload, productKey, deviceName);
        if (message == null) {
            log.warn("[handleStandardRequest][消息解码失败，客户端 ID: {}，主题: {}]", clientId, topic);
            return;
        }
        // 1.2 标准化回复消息的 method（MQTT 协议中，设备回复消息的 method 会携带 _reply 后缀）
        IotMqttTopicUtils.normalizeReplyMethod(message);

        // 2. 处理业务消息
        deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
        log.debug("[handleStandardRequest][消息处理成功，客户端 ID: {}，主题: {}]", clientId, topic);
    }

    /**
     * 处理透传上报（worker 线程）：原始字节按产品的数据解析脚本 decode 后发送；身份已在 event loop 上与连接比对
     *
     * @param receivedNanos 收到的时刻（System.nanoTime）：入队之前取得，截止时间从此刻开始计算
     */
    private void handleRawRequest(String productKey, String deviceName, byte[] payload, long receivedNanos) {
        try {
            deviceMessageService.sendRawMessage(payload, productKey, deviceName, serverId, IotProtocolTypeEnum.MQTT,
                    receivedNanos);
        } catch (ServiceException e) {
            log.warn("[handleRawRequest][透传上报处理失败，productKey: {}，deviceName: {}，数据长度: {} 字节，错误: {}]",
                    productKey, deviceName, payload.length, e.getMessage());
        }
    }

}
