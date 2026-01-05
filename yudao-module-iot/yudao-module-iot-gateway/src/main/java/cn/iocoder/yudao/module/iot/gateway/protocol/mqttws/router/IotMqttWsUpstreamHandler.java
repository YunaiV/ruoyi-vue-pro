package cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.router;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.IotMqttWsUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.manager.IotMqttWsConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.mqtt.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IoT MQTT WebSocket 上行消息处理器
 * <p>
 * 处理来自设备的 MQTT 消息，包括：
 * - CONNECT：设备连接认证
 * - PUBLISH：设备发布消息
 * - SUBSCRIBE：设备订阅主题
 * - UNSUBSCRIBE：设备取消订阅
 * - PINGREQ：心跳请求
 * - DISCONNECT：设备断开连接
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttWsUpstreamHandler {

    private final IotMqttWsUpstreamProtocol upstreamProtocol;

    private final IotDeviceCommonApi deviceApi;

    private final IotDeviceMessageService messageService;

    private final IotMqttWsConnectionManager connectionManager;

    /**
     * 存储 WebSocket 连接到 Socket ID 的映射
     * Key: WebSocket 对象
     * Value: Socket ID（UUID）
     */
    private final ConcurrentHashMap<ServerWebSocket, String> socketIdMap = new ConcurrentHashMap<>();

    /**
     * 存储 Socket ID 对应的设备信息
     * Key: Socket ID（UUID）
     * Value: 设备信息
     */
    private final ConcurrentHashMap<String, IotDeviceRespDTO> socketDeviceMap = new ConcurrentHashMap<>();

    /**
     * 存储设备的消息 ID 生成器（用于 QoS > 0 的消息）
     */
    private final ConcurrentHashMap<String, AtomicInteger> deviceMessageIdMap = new ConcurrentHashMap<>();

    /**
     * MQTT 解码通道（用于解析 WebSocket 中的 MQTT 二进制消息）
     */
    private final ThreadLocal<EmbeddedChannel> decoderChannelThreadLocal = ThreadLocal
            .withInitial(() -> new EmbeddedChannel(new MqttDecoder()));

    /**
     * MQTT 编码通道（用于编码 MQTT 响应消息）
     */
    private final ThreadLocal<EmbeddedChannel> encoderChannelThreadLocal = ThreadLocal
            .withInitial(() -> new EmbeddedChannel(MqttEncoder.INSTANCE));

    public IotMqttWsUpstreamHandler(IotMqttWsUpstreamProtocol upstreamProtocol,
                                    IotDeviceMessageService messageService,
                                    IotMqttWsConnectionManager connectionManager) {
        this.upstreamProtocol = upstreamProtocol;
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.messageService = messageService;
        this.connectionManager = connectionManager;
    }

    /**
     * 处理 WebSocket 连接
     *
     * @param socket WebSocket 连接
     */
    public void handle(ServerWebSocket socket) {
        // 生成唯一的 Socket ID（因为 MQTT 使用二进制协议，textHandlerID() 会返回 null）
        String socketId = IdUtil.simpleUUID();
        socketIdMap.put(socket, socketId);

        log.info("[handle][WebSocket 连接建立，socketId: {}，remoteAddress: {}]",
                socketId, socket.remoteAddress());

        // 设置二进制数据处理器
        socket.binaryMessageHandler(buffer -> {
            try {
                handleMqttMessage(socket, buffer);
            } catch (Exception e) {
                log.error("[handle][处理 MQTT 消息异常，socketId: {}]", socketId, e);
                socket.close();
            }
        });

        // 设置关闭处理器
        socket.closeHandler(v -> {
            socketIdMap.remove(socket);
            IotDeviceRespDTO device = socketDeviceMap.remove(socketId);
            if (device != null) {
                String deviceKey = device.getProductKey() + ":" + device.getDeviceName();
                connectionManager.removeConnection(deviceKey);
                deviceMessageIdMap.remove(deviceKey);
                // 发送设备离线消息
                sendOfflineMessage(device);
                log.info("[handle][WebSocket 连接关闭，deviceKey: {}，socketId: {}]", deviceKey, socketId);
            }
        });

        // 设置异常处理器
        socket.exceptionHandler(e -> {
            log.error("[handle][WebSocket 连接异常，socketId: {}]", socketId, e);
            socketIdMap.remove(socket);
            IotDeviceRespDTO device = socketDeviceMap.remove(socketId);
            if (device != null) {
                String deviceKey = device.getProductKey() + ":" + device.getDeviceName();
                connectionManager.removeConnection(deviceKey);
                deviceMessageIdMap.remove(deviceKey);
            }
            socket.close();
        });
    }

    /**
     * 处理 MQTT 消息
     *
     * @param socket WebSocket 连接
     * @param buffer 消息缓冲区
     */
    private void handleMqttMessage(ServerWebSocket socket, Buffer buffer) {
        String socketId = socketIdMap.get(socket);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer.getBytes());

        try {
            // 使用 EmbeddedChannel 解码 MQTT 消息
            EmbeddedChannel decoderChannel = decoderChannelThreadLocal.get();
            decoderChannel.writeInbound(byteBuf.retain());

            // 读取解码后的消息
            MqttMessage mqttMessage = decoderChannel.readInbound();
            if (mqttMessage == null) {
                log.warn("[handleMqttMessage][MQTT 消息解码失败，socketId: {}]", socketId);
                return;
            }

            MqttMessageType messageType = mqttMessage.fixedHeader().messageType();
            log.debug("[handleMqttMessage][收到 MQTT 消息，类型: {}，socketId: {}]", messageType, socketId);

            // 根据消息类型分发处理
            switch (messageType) {
                case CONNECT:
                    handleConnect(socket, (MqttConnectMessage) mqttMessage);
                    break;
                case PUBLISH:
                    handlePublish(socket, (MqttPublishMessage) mqttMessage);
                    break;
                case PUBACK:
                    handlePubAck(socket, mqttMessage);
                    break;
                case PUBREC:
                    handlePubRec(socket, mqttMessage);
                    break;
                case PUBREL:
                    handlePubRel(socket, mqttMessage);
                    break;
                case PUBCOMP:
                    handlePubComp(socket, mqttMessage);
                    break;
                case SUBSCRIBE:
                    handleSubscribe(socket, (MqttSubscribeMessage) mqttMessage);
                    break;
                case UNSUBSCRIBE:
                    handleUnsubscribe(socket, (MqttUnsubscribeMessage) mqttMessage);
                    break;
                case PINGREQ:
                    handlePingReq(socket);
                    break;
                case DISCONNECT:
                    handleDisconnect(socket);
                    break;
                default:
                    log.warn("[handleMqttMessage][不支持的消息类型: {}，socketId: {}]", messageType, socketId);
            }
        } catch (DecoderException e) {
            log.error("[handleMqttMessage][MQTT 消息解码异常，socketId: {}]", socketId, e);
            socket.close();
        } catch (Exception e) {
            log.error("[handleMqttMessage][处理 MQTT 消息失败，socketId: {}]", socketId, e);
            socket.close();
        } finally {
            byteBuf.release();
        }
    }

    /**
     * 处理 CONNECT 消息（设备认证）
     */
    private void handleConnect(ServerWebSocket socket, MqttConnectMessage message) {
        String socketId = socketIdMap.get(socket);
        try {
            // 1. 解析 CONNECT 消息
            MqttConnectPayload payload = message.payload();
            String clientId = payload.clientIdentifier();
            String username = payload.userName();
            String password = payload.passwordInBytes() != null
                    ? new String(payload.passwordInBytes(), StandardCharsets.UTF_8)
                    : null;

            log.info("[handleConnect][收到 CONNECT 消息，clientId: {}，username: {}，socketId: {}]",
                    clientId, username, socketId);

            // 2. 设备认证
            IotDeviceRespDTO device = authenticateDevice(clientId, username, password);
            if (device == null) {
                log.warn("[handleConnect][设备认证失败，clientId: {}，socketId: {}]", clientId, socketId);
                sendConnAck(socket, MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
                socket.close();
                return;
            }

            // 3. 保存设备信息
            socketDeviceMap.put(socketId, device);
            String deviceKey = device.getProductKey() + ":" + device.getDeviceName();
            connectionManager.addConnection(deviceKey, socket, socketId);
            deviceMessageIdMap.put(deviceKey, new AtomicInteger(1));

            log.info("[handleConnect][设备认证成功，deviceId: {}，deviceKey: {}，socketId: {}]",
                    device.getId(), deviceKey, socketId);

            // 4. 发送 CONNACK
            sendConnAck(socket, MqttConnectReturnCode.CONNECTION_ACCEPTED);

            // 5. 发送设备上线消息
            sendOnlineMessage(device);
        } catch (Exception e) {
            log.error("[handleConnect][处理 CONNECT 消息失败，socketId: {}]", socketId, e);
            sendConnAck(socket, MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE);
            socket.close();
        }
    }

    /**
     * 处理 PUBLISH 消息（设备发布消息）
     */
    private void handlePublish(ServerWebSocket socket, MqttPublishMessage message) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);

        if (device == null) {
            log.warn("[handlePublish][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        try {
            // 1. 解析 PUBLISH 消息
            MqttFixedHeader fixedHeader = message.fixedHeader();
            MqttPublishVariableHeader variableHeader = message.variableHeader();
            ByteBuf payload = message.payload();

            String topic = variableHeader.topicName();
            int messageId = variableHeader.packetId();
            MqttQoS qos = fixedHeader.qosLevel();

            log.debug("[handlePublish][收到 PUBLISH 消息，topic: {}，messageId: {}，QoS: {}，deviceId: {}]",
                    topic, messageId, qos, device.getId());

            // 2. 读取 payload
            byte[] payloadBytes = new byte[payload.readableBytes()];
            payload.readBytes(payloadBytes);

            // 3. 解码并发送消息
            IotDeviceMessage deviceMessage = messageService.decodeDeviceMessage(payloadBytes,
                    device.getProductKey(), device.getDeviceName());
            if (deviceMessage != null) {
                deviceMessage.setServerId(upstreamProtocol.getServerId());
                messageService.sendDeviceMessage(deviceMessage, device.getProductKey(),
                        device.getDeviceName(), upstreamProtocol.getServerId());
                log.info("[handlePublish][设备消息已发送，method: {}，deviceId: {}]",
                        deviceMessage.getMethod(), device.getId());
            }

            // 4. 根据 QoS 级别发送相应的确认消息
            if (qos == MqttQoS.AT_LEAST_ONCE) {
                // QoS 1：发送 PUBACK
                sendPubAck(socket, messageId);
            } else if (qos == MqttQoS.EXACTLY_ONCE) {
                // QoS 2：发送 PUBREC
                sendPubRec(socket, messageId);
            }
            // QoS 0 无需确认
        } catch (Exception e) {
            log.error("[handlePublish][处理 PUBLISH 消息失败，deviceId: {}]", device.getId(), e);
        }
    }

    /**
     * 处理 PUBACK 消息（QoS 1 确认）
     */
    private void handlePubAck(ServerWebSocket socket, MqttMessage message) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);
        if (device == null) {
            log.warn("[handlePubAck][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        int messageId = ((MqttMessageIdVariableHeader) message.variableHeader()).messageId();
        log.debug("[handlePubAck][收到 PUBACK，messageId: {}，deviceId: {}]", messageId, device.getId());
    }

    /**
     * 处理 PUBREC 消息（QoS 2 第一步确认）
     */
    private void handlePubRec(ServerWebSocket socket, MqttMessage message) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);
        if (device == null) {
            log.warn("[handlePubRec][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        int messageId = ((MqttMessageIdVariableHeader) message.variableHeader()).messageId();
        log.debug("[handlePubRec][收到 PUBREC，messageId: {}，deviceId: {}]", messageId, device.getId());
        // 发送 PUBREL
        sendPubRel(socket, messageId);
    }

    /**
     * 处理 PUBREL 消息（QoS 2 第二步）
     */
    private void handlePubRel(ServerWebSocket socket, MqttMessage message) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);

        if (device == null) {
            log.warn("[handlePubRel][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        int messageId = ((MqttMessageIdVariableHeader) message.variableHeader()).messageId();
        log.debug("[handlePubRel][收到 PUBREL，messageId: {}，deviceId: {}]", messageId, device.getId());
        // 发送 PUBCOMP
        sendPubComp(socket, messageId);
    }

    /**
     * 处理 PUBCOMP 消息（QoS 2 完成确认）
     */
    private void handlePubComp(ServerWebSocket socket, MqttMessage message) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);

        if (device == null) {
            log.warn("[handlePubComp][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        int messageId = ((MqttMessageIdVariableHeader) message.variableHeader()).messageId();
        log.debug("[handlePubComp][收到 PUBCOMP，messageId: {}，deviceId: {}]", messageId, device.getId());
    }

    /**
     * 处理 SUBSCRIBE 消息（设备订阅主题）
     */
    private void handleSubscribe(ServerWebSocket socket, MqttSubscribeMessage message) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);
        if (device == null) {
            log.warn("[handleSubscribe][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        try {
            // 1. 解析 SUBSCRIBE 消息
            int messageId = message.variableHeader().messageId();
            MqttSubscribePayload payload = message.payload();
            String deviceKey = device.getProductKey() + ":" + device.getDeviceName();

            log.info("[handleSubscribe][设备订阅请求，deviceKey: {}，messageId: {}，主题数量: {}]",
                    deviceKey, messageId, payload.topicSubscriptions().size());

            // 2. 构建 QoS 列表并记录订阅信息
            int[] grantedQosList = new int[payload.topicSubscriptions().size()];
            for (int i = 0; i < payload.topicSubscriptions().size(); i++) {
                MqttTopicSubscription subscription = payload.topicSubscriptions().get(i);
                String topic = subscription.topicFilter();
                grantedQosList[i] = subscription.qualityOfService().value();

                // 记录订阅信息到连接管理器
                connectionManager.addSubscription(deviceKey, topic);

                log.info("[handleSubscribe][订阅主题: {}，QoS: {}，deviceKey: {}]",
                        topic, subscription.qualityOfService(), deviceKey);
            }

            // 3. 发送 SUBACK
            sendSubAck(socket, messageId, grantedQosList);
        } catch (Exception e) {
            log.error("[handleSubscribe][处理 SUBSCRIBE 消息失败，deviceId: {}]", device.getId(), e);
        }
    }

    /**
     * 处理 UNSUBSCRIBE 消息（设备取消订阅）
     */
    private void handleUnsubscribe(ServerWebSocket socket, MqttUnsubscribeMessage message) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);
        if (device == null) {
            log.warn("[handleUnsubscribe][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        try {
            // 1. 解析 UNSUBSCRIBE 消息
            int messageId = message.variableHeader().messageId();
            MqttUnsubscribePayload payload = message.payload();
            String deviceKey = device.getProductKey() + ":" + device.getDeviceName();

            log.info("[handleUnsubscribe][设备取消订阅，deviceKey: {}，messageId: {}，主题数量: {}]",
                    deviceKey, messageId, payload.topics().size());

            // 2. 移除订阅信息
            for (String topic : payload.topics()) {
                connectionManager.removeSubscription(deviceKey, topic);
                log.info("[handleUnsubscribe][取消订阅主题: {}，deviceKey: {}]", topic, deviceKey);
            }

            // 3. 发送 UNSUBACK
            sendUnsubAck(socket, messageId);
        } catch (Exception e) {
            log.error("[handleUnsubscribe][处理 UNSUBSCRIBE 消息失败，deviceId: {}]", device.getId(), e);
        }
    }

    /**
     * 处理 PINGREQ 消息（心跳请求）
     */
    private void handlePingReq(ServerWebSocket socket) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.get(socketId);
        if (device == null) {
            log.warn("[handlePingReq][设备未认证，socketId: {}]", socketId);
            socket.close();
            return;
        }

        log.debug("[handlePingReq][收到心跳请求，deviceId: {}]", device.getId());
        // 发送 PINGRESP
        sendPingResp(socket);
    }

    /**
     * 处理 DISCONNECT 消息（设备断开连接）
     */
    private void handleDisconnect(ServerWebSocket socket) {
        String socketId = socketIdMap.get(socket);
        IotDeviceRespDTO device = socketDeviceMap.remove(socketId);
        if (device != null) {
            String deviceKey = device.getProductKey() + ":" + device.getDeviceName();
            connectionManager.removeConnection(deviceKey);
            deviceMessageIdMap.remove(deviceKey);
            sendOfflineMessage(device);
            log.info("[handleDisconnect][设备主动断开连接，deviceKey: {}]", deviceKey);
        }

        socket.close();
    }

    // ==================== 设备认证和状态相关方法 ====================

    /**
     * 设备认证
     */
    private IotDeviceRespDTO authenticateDevice(String clientId, String username, String password) {
        try {
            // 1. 参数校验
            if (StrUtil.hasEmpty(clientId, username, password)) {
                log.warn("[authenticateDevice][认证参数不完整，clientId: {}，username: {}]", clientId, username);
                return null;
            }

            // 2. 构建认证参数并调用 API
            IotDeviceAuthReqDTO authParams = new IotDeviceAuthReqDTO()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);

            CommonResult<Boolean> authResult = deviceApi.authDevice(authParams);
            if (!authResult.isSuccess() || !BooleanUtil.isTrue(authResult.getData())) {
                log.warn("[authenticateDevice][设备认证失败，clientId: {}]", clientId);
                return null;
            }

            // 3. 获取设备信息
            IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            if (deviceInfo == null) {
                log.warn("[authenticateDevice][用户名格式不正确，username: {}]", username);
                return null;
            }

            IotDeviceGetReqDTO getReqDTO = new IotDeviceGetReqDTO()
                    .setProductKey(deviceInfo.getProductKey())
                    .setDeviceName(deviceInfo.getDeviceName());

            CommonResult<IotDeviceRespDTO> deviceResult = deviceApi.getDevice(getReqDTO);
            if (!deviceResult.isSuccess() || deviceResult.getData() == null) {
                log.warn("[authenticateDevice][获取设备信息失败，username: {}]", username);
                return null;
            }

            return deviceResult.getData();
        } catch (Exception e) {
            log.error("[authenticateDevice][设备认证异常，clientId: {}]", clientId, e);
            return null;
        }
    }

    /**
     * 发送设备上线消息
     */
    private void sendOnlineMessage(IotDeviceRespDTO device) {
        try {
            IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
            messageService.sendDeviceMessage(onlineMessage, device.getProductKey(),
                    device.getDeviceName(), upstreamProtocol.getServerId());
            log.info("[sendOnlineMessage][设备上线，deviceId: {}]", device.getId());
        } catch (Exception e) {
            log.error("[sendOnlineMessage][发送设备上线消息失败，deviceId: {}]", device.getId(), e);
        }
    }

    /**
     * 发送设备离线消息
     */
    private void sendOfflineMessage(IotDeviceRespDTO device) {
        try {
            IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
            messageService.sendDeviceMessage(offlineMessage, device.getProductKey(),
                    device.getDeviceName(), upstreamProtocol.getServerId());
            log.info("[sendOfflineMessage][设备离线，deviceId: {}]", device.getId());
        } catch (Exception e) {
            log.error("[sendOfflineMessage][发送设备离线消息失败，deviceId: {}]", device.getId(), e);
        }
    }

    // ==================== 发送响应消息的辅助方法 ====================

    /**
     * 发送 CONNACK 消息
     */
    private void sendConnAck(ServerWebSocket socket, MqttConnectReturnCode returnCode) {
        try {
            // 构建 CONNACK 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttConnAckVariableHeader variableHeader = new MqttConnAckVariableHeader(returnCode, false);
            MqttConnAckMessage connAckMessage = new MqttConnAckMessage(fixedHeader, variableHeader);

            // 编码并发送
            sendMqttMessage(socket, connAckMessage);
            log.debug("[sendConnAck][发送 CONNACK 消息，returnCode: {}]", returnCode);
        } catch (Exception e) {
            log.error("[sendConnAck][发送 CONNACK 消息失败]", e);
        }
    }

    /**
     * 发送 PUBACK 消息（QoS 1 确认）
     */
    private void sendPubAck(ServerWebSocket socket, int messageId) {
        try {
            // 构建 PUBACK 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
            MqttMessage pubAckMessage = new MqttMessage(fixedHeader, variableHeader);

            // 编码并发送
            sendMqttMessage(socket, pubAckMessage);
            log.debug("[sendPubAck][发送 PUBACK 消息，messageId: {}]", messageId);
        } catch (Exception e) {
            log.error("[sendPubAck][发送 PUBACK 消息失败，messageId: {}]", messageId, e);
        }
    }

    /**
     * 发送 PUBREC 消息（QoS 2 第一步确认）
     */
    private void sendPubRec(ServerWebSocket socket, int messageId) {
        try {
            // 构建 PUBREC 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
            MqttMessage pubRecMessage = new MqttMessage(fixedHeader, variableHeader);

            // 编码并发送
            sendMqttMessage(socket, pubRecMessage);
            log.debug("[sendPubRec][发送 PUBREC 消息，messageId: {}]", messageId);
        } catch (Exception e) {
            log.error("[sendPubRec][发送 PUBREC 消息失败，messageId: {}]", messageId, e);
        }
    }

    /**
     * 发送 PUBREL 消息（QoS 2 第二步）
     */
    private void sendPubRel(ServerWebSocket socket, int messageId) {
        try {
            // 构建 PUBREL 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
            MqttMessage pubRelMessage = new MqttMessage(fixedHeader, variableHeader);

            // 编码并发送
            sendMqttMessage(socket, pubRelMessage);
            log.debug("[sendPubRel][发送 PUBREL 消息，messageId: {}]", messageId);
        } catch (Exception e) {
            log.error("[sendPubRel][发送 PUBREL 消息失败，messageId: {}]", messageId, e);
        }
    }

    /**
     * 发送 PUBCOMP 消息（QoS 2 完成确认）
     */
    private void sendPubComp(ServerWebSocket socket, int messageId) {
        try {
            // 构建 PUBCOMP 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
            MqttMessage pubCompMessage = new MqttMessage(fixedHeader, variableHeader);

            // 编码并发送
            sendMqttMessage(socket, pubCompMessage);
            log.debug("[sendPubComp][发送 PUBCOMP 消息，messageId: {}]", messageId);
        } catch (Exception e) {
            log.error("[sendPubComp][发送 PUBCOMP 消息失败，messageId: {}]", messageId, e);
        }
    }

    /**
     * 发送 SUBACK 消息
     */
    private void sendSubAck(ServerWebSocket socket, int messageId, int[] grantedQosList) {
        try {
            // 构建 SUBACK 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
            MqttSubAckPayload payload = new MqttSubAckPayload(grantedQosList);
            MqttSubAckMessage subAckMessage = new MqttSubAckMessage(fixedHeader, variableHeader, payload);

            // 编码并发送
            sendMqttMessage(socket, subAckMessage);
            log.debug("[sendSubAck][发送 SUBACK 消息，messageId: {}，主题数量: {}]", messageId, grantedQosList.length);
        } catch (Exception e) {
            log.error("[sendSubAck][发送 SUBACK 消息失败，messageId: {}]", messageId, e);
        }
    }

    /**
     * 发送 UNSUBACK 消息
     */
    private void sendUnsubAck(ServerWebSocket socket, int messageId) {
        try {
            // 构建 UNSUBACK 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
            MqttUnsubAckMessage unsubAckMessage = new MqttUnsubAckMessage(fixedHeader, variableHeader);

            // 编码并发送
            sendMqttMessage(socket, unsubAckMessage);
            log.debug("[sendUnsubAck][发送 UNSUBACK 消息，messageId: {}]", messageId);
        } catch (Exception e) {
            log.error("[sendUnsubAck][发送 UNSUBACK 消息失败，messageId: {}]", messageId, e);
        }
    }

    /**
     * 发送 PINGRESP 消息
     */
    private void sendPingResp(ServerWebSocket socket) {
        try {
            // 构建 PINGRESP 消息
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessage pingRespMessage = new MqttMessage(fixedHeader);

            // 编码并发送
            sendMqttMessage(socket, pingRespMessage);
            log.debug("[sendPingResp][发送 PINGRESP 消息]");
        } catch (Exception e) {
            log.error("[sendPingResp][发送 PINGRESP 消息失败]", e);
        }
    }

    /**
     * 发送 MQTT 消息到 WebSocket
     */
    private void sendMqttMessage(ServerWebSocket socket, MqttMessage mqttMessage) {
        ByteBuf byteBuf = null;
        try {
            // 使用 EmbeddedChannel 编码 MQTT 消息
            EmbeddedChannel encoderChannel = encoderChannelThreadLocal.get();
            encoderChannel.writeOutbound(mqttMessage);

            // 读取编码后的 ByteBuf
            byteBuf = encoderChannel.readOutbound();
            if (byteBuf != null) {
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);
                socket.writeBinaryMessage(Buffer.buffer(bytes));
            }
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }

}
