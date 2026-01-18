package cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.router;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.manager.IotMqttWsConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * IoT MQTT WebSocket 下行消息处理器
 * <p>
 * 处理从消息总线发送到设备的消息，包括：
 * - 属性设置
 * - 服务调用
 * - 事件通知
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttWsDownstreamHandler {

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceService deviceService;

    private final IotMqttWsConnectionManager connectionManager;

    /**
     * 消息 ID 生成器（用于发布消息）
     */
    private final AtomicInteger messageIdGenerator = new AtomicInteger(1);

    public IotMqttWsDownstreamHandler(IotDeviceMessageService deviceMessageService,
                                      IotDeviceService deviceService,
                                      IotMqttWsConnectionManager connectionManager) {
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
        this.connectionManager = connectionManager;
    }

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     * @return 是否处理成功
     */
    public boolean handleDownstreamMessage(IotDeviceMessage message) {
        try {
            // 1. 基础校验
            if (message == null || message.getDeviceId() == null) {
                log.warn("[handleDownstreamMessage][消息或设备 ID 为空，忽略处理]");
                return false;
            }

            // 2. 获取设备信息
            IotDeviceRespDTO deviceInfo = deviceService.getDeviceFromCache(message.getDeviceId());
            if (deviceInfo == null) {
                log.warn("[handleDownstreamMessage][设备不存在，设备 ID：{}]", message.getDeviceId());
                return false;
            }

            // 3. 构建设备标识
            String deviceKey = deviceInfo.getProductKey() + ":" + deviceInfo.getDeviceName();

            // 4. 检查设备是否在线
            if (!connectionManager.isOnline(deviceKey)) {
                log.warn("[handleDownstreamMessage][设备离线，无法发送消息，deviceKey: {}]", deviceKey);
                return false;
            }

            // 5. 构建主题
            String topic = buildDownstreamTopic(message, deviceInfo);
            if (StrUtil.isBlank(topic)) {
                log.warn("[handleDownstreamMessage][主题构建失败，设备 ID：{}，方法：{}]",
                        message.getDeviceId(), message.getMethod());
                return false;
            }

            // 6. 检查设备是否订阅了该主题
            if (!connectionManager.isSubscribed(deviceKey, topic)) {
                log.warn("[handleDownstreamMessage][设备未订阅该主题，deviceKey: {}，topic: {}]", deviceKey, topic);
                return false;
            }

            // 8. 编码消息
            byte[] payload = deviceMessageService.encodeDeviceMessage(message,
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            if (payload == null || payload.length == 0) {
                log.warn("[handleDownstreamMessage][消息编码失败，设备 ID：{}]", message.getDeviceId());
                return false;
            }

            // 9. 发送消息到设备
            return sendMessageToDevice(deviceKey, topic, payload, 1);
        } catch (Exception e) {
            if (message != null) {
                log.error("[handleDownstreamMessage][处理下行消息异常，设备 ID：{}，错误：{}]",
                        message.getDeviceId(), e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 构建下行消息主题
     *
     * @param message    设备消息
     * @param deviceInfo 设备信息
     * @return 主题
     */
    private String buildDownstreamTopic(IotDeviceMessage message, IotDeviceRespDTO deviceInfo) {
        String method = message.getMethod();
        if (StrUtil.isBlank(method)) {
            return null;
        }

        // 使用工具类构建主题，支持回复消息处理
        boolean isReply = IotDeviceMessageUtils.isReplyMessage(message);
        return IotMqttTopicUtils.buildTopicByMethod(method, deviceInfo.getProductKey(),
                deviceInfo.getDeviceName(), isReply);
    }

    /**
     * 发送消息到设备
     *
     * @param deviceKey 设备标识（productKey:deviceName）
     * @param topic     主题
     * @param payload   消息内容
     * @param qos       QoS 级别
     * @return 是否发送成功
     */
    private boolean sendMessageToDevice(String deviceKey, String topic, byte[] payload, int qos) {
        // 获取设备连接
        ServerWebSocket socket = connectionManager.getConnection(deviceKey);
        if (socket == null) {
            log.warn("[sendMessageToDevice][设备未连接，deviceKey: {}]", deviceKey);
            return false;
        }

        try {
            int messageId = qos > 0 ? generateMessageId() : 0;

            // 手动编码 MQTT PUBLISH 消息
            io.netty.buffer.ByteBuf byteBuf = io.netty.buffer.Unpooled.buffer();

            // 固定头：消息类型(PUBLISH=3) + DUP(0) + QoS + RETAIN
            int fixedHeaderByte1 = 0x30 | (qos << 1); // PUBLISH类型
            byteBuf.writeByte(fixedHeaderByte1);

            // 计算剩余长度
            int topicLength = topic.getBytes().length;
            int remainingLength = 2 + topicLength + (qos > 0 ? 2 : 0) + payload.length;

            // 写入剩余长度（简化版本，假设小于 128 字节）
            if (remainingLength < 128) {
                byteBuf.writeByte(remainingLength);
            } else {
                // 处理大于 127 的情况
                int x = remainingLength;
                do {
                    int encodedByte = x % 128;
                    x = x / 128;
                    if (x > 0) {
                        encodedByte = encodedByte | 128;
                    }
                    byteBuf.writeByte(encodedByte);
                } while (x > 0);
            }

            // 可变头：主题名称
            byteBuf.writeShort(topicLength);
            byteBuf.writeBytes(topic.getBytes());

            // 可变头：消息 ID（仅 QoS > 0 时）
            if (qos > 0) {
                byteBuf.writeShort(messageId);
            }

            // 有效载荷
            byteBuf.writeBytes(payload);

            // 发送
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            byteBuf.release();
            socket.writeBinaryMessage(Buffer.buffer(bytes));

            log.info("[sendMessageToDevice][消息已发送到设备，deviceKey: {}，topic: {}，qos: {}，messageId: {}]",
                    deviceKey, topic, qos, messageId);
            return true;
        } catch (Exception e) {
            log.error("[sendMessageToDevice][发送消息到设备失败，deviceKey: {}，topic: {}]", deviceKey, topic, e);
            return false;
        }
    }

    /**
     * 生成消息 ID
     *
     * @return 消息 ID
     */
    private int generateMessageId() {
        int id = messageIdGenerator.getAndIncrement();
        // MQTT 消息 ID 范围是 1-65535
        // TODO @haohao：并发可能有问题；
        if (id > 65535) {
            messageIdGenerator.set(1);
            return 1;
        }
        return id;
    }

}
