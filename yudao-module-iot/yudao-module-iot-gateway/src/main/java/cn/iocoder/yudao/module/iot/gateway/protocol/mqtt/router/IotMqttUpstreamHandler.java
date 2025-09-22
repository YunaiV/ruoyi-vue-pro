package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * MQTT 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttUpstreamHandler {

    private final IotDeviceMessageService deviceMessageService;

    private final IotMqttConnectionManager connectionManager;

    private final IotDeviceCommonApi deviceApi;

    private final String serverId;

    public IotMqttUpstreamHandler(IotMqttUpstreamProtocol protocol,
                                  IotDeviceMessageService deviceMessageService,
                                  IotMqttConnectionManager connectionManager) {
        this.deviceMessageService = deviceMessageService;
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.connectionManager = connectionManager;
        this.serverId = protocol.getServerId();
    }

    /**
     * 处理 MQTT 连接
     *
     * @param endpoint MQTT 连接端点
     */
    public void handle(MqttEndpoint endpoint) {
        String clientId = endpoint.clientIdentifier();
        String username = endpoint.auth() != null ? endpoint.auth().getUsername() : null;
        String password = endpoint.auth() != null ? endpoint.auth().getPassword() : null;

        log.debug("[handle][设备连接请求，客户端 ID: {}，用户名: {}，地址: {}]",
                clientId, username, connectionManager.getEndpointAddress(endpoint));

        // 1. 先进行认证
        if (!authenticateDevice(clientId, username, password, endpoint)) {
            log.warn("[handle][设备认证失败，拒绝连接，客户端 ID: {}，用户名: {}]", clientId, username);
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
            return;
        }

        log.info("[handle][设备认证成功，建立连接，客户端 ID: {}，用户名: {}]", clientId, username);

        // 2. 设置心跳处理器（监听客户端的 PINGREQ 消息）
        endpoint.pingHandler(v -> {
            log.debug("[handle][收到客户端心跳，客户端 ID: {}]", clientId);
            // Vert.x 会自动发送 PINGRESP 响应，无需手动处理
        });

        // 3. 设置异常和关闭处理器
        endpoint.exceptionHandler(ex -> {
            log.warn("[handle][连接异常，客户端 ID: {}，地址: {}]", clientId, connectionManager.getEndpointAddress(endpoint));
            cleanupConnection(endpoint);
        });
        endpoint.closeHandler(v -> {
            cleanupConnection(endpoint);
        });

        // 4. 设置消息处理器
        endpoint.publishHandler(message -> {
            try {
                processMessage(clientId, message.topicName(), message.payload().getBytes());

                // 根据 QoS 级别发送相应的确认消息
                if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                    // QoS 1: 发送 PUBACK 确认
                    endpoint.publishAcknowledge(message.messageId());
                } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                    // QoS 2: 发送 PUBREC 确认
                    endpoint.publishReceived(message.messageId());
                }
                // QoS 0 无需确认

            } catch (Exception e) {
                log.error("[handle][消息解码失败，断开连接，客户端 ID: {}，地址: {}，错误: {}]",
                        clientId, connectionManager.getEndpointAddress(endpoint), e.getMessage());
                cleanupConnection(endpoint);
                endpoint.close();
            }
        });

        // 5. 设置订阅处理器
        endpoint.subscribeHandler(subscribe -> {
            // 提取主题名称列表用于日志显示
            List<String> topicNames = subscribe.topicSubscriptions().stream()
                    .map(MqttTopicSubscription::topicName)
                    .collect(java.util.stream.Collectors.toList());
            log.debug("[handle][设备订阅，客户端 ID: {}，主题: {}]", clientId, topicNames);

            // 提取 QoS 列表
            List<MqttQoS> grantedQoSLevels = subscribe.topicSubscriptions().stream()
                    .map(MqttTopicSubscription::qualityOfService)
                    .collect(java.util.stream.Collectors.toList());
            endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQoSLevels);
        });

        // 6. 设置取消订阅处理器
        endpoint.unsubscribeHandler(unsubscribe -> {
            log.debug("[handle][设备取消订阅，客户端 ID: {}，主题: {}]", clientId, unsubscribe.topics());
            endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        });

        // 7. 设置 QoS 2消息的 PUBREL 处理器
        endpoint.publishReleaseHandler(endpoint::publishComplete);

        // 8. 设置断开连接处理器
        endpoint.disconnectHandler(v -> {
            log.debug("[handle][设备断开连接，客户端 ID: {}]", clientId);
            cleanupConnection(endpoint);
        });

        // 9. 接受连接
        endpoint.accept(false);
    }

    /**
     * 处理消息
     *
     * @param clientId 客户端 ID
     * @param topic    主题
     * @param payload  消息内容
     */
    private void processMessage(String clientId, String topic, byte[] payload) {
        // 1. 基础检查
        if (payload == null || payload.length == 0) {
            return;
        }

        // 2. 解析主题，获取 productKey 和 deviceName
        String[] topicParts = topic.split("/");
        if (topicParts.length < 4 || StrUtil.hasBlank(topicParts[2], topicParts[3])) {
            log.warn("[processMessage][topic({}) 格式不正确，无法解析有效的 productKey 和 deviceName]", topic);
            return;
        }

        String productKey = topicParts[2];
        String deviceName = topicParts[3];

        // 3. 解码消息（使用从 topic 解析的 productKey 和 deviceName）
        try {
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(payload, productKey, deviceName);
            if (message == null) {
                log.warn("[processMessage][消息解码失败，客户端 ID: {}，主题: {}]", clientId, topic);
                return;
            }

            log.info("[processMessage][收到设备消息，设备: {}.{}, 方法: {}]",
                    productKey, deviceName, message.getMethod());

            // 4. 处理业务消息（认证已在连接时完成）
            handleBusinessRequest(message, productKey, deviceName);
        } catch (Exception e) {
            log.error("[processMessage][消息处理异常，客户端 ID: {}，主题: {}，错误: {}]",
                    clientId, topic, e.getMessage(), e);
        }
    }

    /**
     * 在 MQTT 连接时进行设备认证
     *
     * @param clientId 客户端 ID
     * @param username 用户名
     * @param password 密码
     * @param endpoint MQTT 连接端点
     * @return 认证是否成功
     */
    private boolean authenticateDevice(String clientId, String username, String password, MqttEndpoint endpoint) {
        try {
            // 1. 参数校验
            if (StrUtil.hasEmpty(clientId, username, password)) {
                log.warn("[authenticateDevice][认证参数不完整，客户端 ID: {}，用户名: {}]", clientId, username);
                return false;
            }

            // 2. 构建认证参数
            IotDeviceAuthReqDTO authParams = new IotDeviceAuthReqDTO()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);

            // 3. 调用设备认证 API
            CommonResult<Boolean> authResult = deviceApi.authDevice(authParams);
            if (!authResult.isSuccess() || !BooleanUtil.isTrue(authResult.getData())) {
                log.warn("[authenticateDevice][设备认证失败，客户端 ID: {}，用户名: {}，错误: {}]",
                        clientId, username, authResult.getMsg());
                return false;
            }

            // 4. 获取设备信息
            IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            if (deviceInfo == null) {
                log.warn("[authenticateDevice][用户名格式不正确，客户端 ID: {}，用户名: {}]", clientId, username);
                return false;
            }

            IotDeviceGetReqDTO getReqDTO = new IotDeviceGetReqDTO()
                    .setProductKey(deviceInfo.getProductKey())
                    .setDeviceName(deviceInfo.getDeviceName());

            CommonResult<IotDeviceRespDTO> deviceResult = deviceApi.getDevice(getReqDTO);
            if (!deviceResult.isSuccess() || deviceResult.getData() == null) {
                log.warn("[authenticateDevice][获取设备信息失败，客户端 ID: {}，用户名: {}，错误: {}]",
                        clientId, username, deviceResult.getMsg());
                return false;
            }

            // 5. 注册连接
            IotDeviceRespDTO device = deviceResult.getData();
            registerConnection(endpoint, device, clientId);

            // 6. 发送设备上线消息
            sendOnlineMessage(device);

            return true;
        } catch (Exception e) {
            log.error("[authenticateDevice][设备认证异常，客户端 ID: {}，用户名: {}]", clientId, username, e);
            return false;
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

    /**
     * 注册连接
     */
    private void registerConnection(MqttEndpoint endpoint, IotDeviceRespDTO device,
                                    String clientId) {

        IotMqttConnectionManager.ConnectionInfo connectionInfo = new IotMqttConnectionManager.ConnectionInfo()
                .setDeviceId(device.getId())
                .setProductKey(device.getProductKey())
                .setDeviceName(device.getDeviceName())
                .setClientId(clientId)
                .setAuthenticated(true)
                .setRemoteAddress(connectionManager.getEndpointAddress(endpoint));

        connectionManager.registerConnection(endpoint, device.getId(), connectionInfo);
    }

    /**
     * 发送设备上线消息
     */
    private void sendOnlineMessage(IotDeviceRespDTO device) {
        try {
            IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
            deviceMessageService.sendDeviceMessage(onlineMessage, device.getProductKey(),
                    device.getDeviceName(), serverId);
            log.info("[sendOnlineMessage][设备上线，设备 ID: {}，设备名称: {}]", device.getId(), device.getDeviceName());
        } catch (Exception e) {
            log.error("[sendOnlineMessage][发送设备上线消息失败，设备 ID: {}，错误: {}]", device.getId(), e.getMessage());
        }
    }

    /**
     * 清理连接
     */
    private void cleanupConnection(MqttEndpoint endpoint) {
        try {
            IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfo(endpoint);
            if (connectionInfo != null) {
                // 发送设备离线消息
                IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
                deviceMessageService.sendDeviceMessage(offlineMessage, connectionInfo.getProductKey(),
                        connectionInfo.getDeviceName(), serverId);
                log.info("[cleanupConnection][设备离线，设备 ID: {}，设备名称: {}]",
                        connectionInfo.getDeviceId(), connectionInfo.getDeviceName());
            }

            // 注销连接
            connectionManager.unregisterConnection(endpoint);
        } catch (Exception e) {
            log.error("[cleanupConnection][清理连接失败，客户端 ID: {}，错误: {}]",
                    endpoint.clientIdentifier(), e.getMessage());
        }
    }

}
