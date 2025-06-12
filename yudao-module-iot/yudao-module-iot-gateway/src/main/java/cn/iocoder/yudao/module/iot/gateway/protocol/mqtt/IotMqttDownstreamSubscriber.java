package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 订阅者：接收下行给设备的消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttDownstreamSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    private final IotMqttUpstreamProtocol protocol;
    private final IotMessageBus messageBus;

    @Resource
    private IotDeviceService deviceService;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotDeviceMessageUtils.buildMessageBusGatewayDeviceMessageTopic(protocol.getServerId());
    }

    @Override
    public String getGroup() {
        // 保证点对点消费，需要保证独立的 Group，所以使用 Topic 作为 Group
        return getTopic();
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        log.info("[onMessage][接收到下行消息][messageId: {}][method: {}][deviceId: {}]",
                message.getId(), message.getMethod(), message.getDeviceId());
        try {
            // 根据消息方法处理不同的下行消息
            String method = message.getMethod();
            if (method == null) {
                log.warn("[onMessage][消息方法为空][messageId: {}][deviceId: {}]",
                        message.getId(), message.getDeviceId());
                return;
            }

            // 过滤上行消息：下行订阅者只处理下行消息
            if (isUpstreamMessage(method)) {
                log.debug("[onMessage][忽略上行消息][method: {}][messageId: {}]", method, message.getId());
                return;
            }

            // 处理下行消息
            handleDownstreamMessage(message);
        } catch (Exception e) {
            log.error("[onMessage][处理下行消息失败][messageId: {}][method: {}][deviceId: {}]",
                    message.getId(), message.getMethod(), message.getDeviceId(), e);
        }
    }

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     */
    private void handleDownstreamMessage(IotDeviceMessage message) {
        // 1. 获取设备信息（使用缓存）
        IotDeviceRespDTO deviceInfo = deviceService.getDeviceFromCache(message.getDeviceId());
        if (deviceInfo == null) {
            log.warn("[handleDownstreamMessage][设备信息不存在][deviceId: {}]", message.getDeviceId());
            return;
        }

        // 2. 根据方法构建主题
        String topic = buildTopicByMethod(message.getMethod(), deviceInfo.getProductKey(), deviceInfo.getDeviceName());
        if (StrUtil.isBlank(topic)) {
            log.warn("[handleDownstreamMessage][未知的消息方法：{}]", message.getMethod());
            return;
        }

        // 3. 构建载荷
        JSONObject payload = buildDownstreamPayload(message, message.getMethod());

        // 4. 发送消息
        protocol.publishMessage(topic, payload.toString());
        log.info("[handleDownstreamMessage][发送下行消息成功][method: {}][topic: {}]", message.getMethod(), topic);
    }

    /**
     * 根据方法构建主题
     *
     * @param method     消息方法
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 构建的主题，如果方法不支持返回 null
     */
    private String buildTopicByMethod(String method, String productKey, String deviceName) {
        if (StrUtil.isBlank(method)) {
            return null;
        }

        // 属性相关操作
        if (method.startsWith(IotDeviceTopicEnum.PROPERTY_SERVICE_METHOD_PREFIX)) {
            if (IotDeviceTopicEnum.PROPERTY_SET_METHOD.equals(method)) {
                return IotDeviceTopicEnum.buildPropertySetTopic(productKey, deviceName);
            } else if (IotDeviceTopicEnum.PROPERTY_GET_METHOD.equals(method)) {
                return IotDeviceTopicEnum.buildPropertyGetTopic(productKey, deviceName);
            }
            return null;
        }

        // 配置设置操作
        if (method.startsWith(IotDeviceTopicEnum.CONFIG_SERVICE_METHOD_PREFIX)) {
            return IotDeviceTopicEnum.buildConfigSetTopic(productKey, deviceName);
        }

        // OTA 升级操作
        if (method.startsWith(IotDeviceTopicEnum.OTA_SERVICE_METHOD_PREFIX)) {
            return IotDeviceTopicEnum.buildOtaUpgradeTopic(productKey, deviceName);
        }

        // 一般服务调用操作
        if (method.startsWith(IotDeviceTopicEnum.SERVICE_METHOD_PREFIX)) {
            // 排除属性、配置、OTA 相关的服务调用
            if (method.contains("property") || method.contains("config") || method.contains("ota")) {
                return null; // 已在上面处理
            }
            // 从方法中提取服务标识符
            String serviceIdentifier = method.substring(IotDeviceTopicEnum.SERVICE_METHOD_PREFIX.length());
            return IotDeviceTopicEnum.buildServiceTopic(productKey, deviceName, serviceIdentifier);
        }

        // 不支持的方法
        return null;
    }

    /**
     * 构建下行消息载荷
     *
     * @param message 设备消息
     * @param method  方法名
     * @return JSON 载荷
     */
    private JSONObject buildDownstreamPayload(IotDeviceMessage message, String method) {
        JSONObject payload = new JSONObject();
        payload.set("id", message.getId()); // 使用正确的消息ID字段
        payload.set("version", "1.0");
        payload.set("method", method);
        payload.set("params", message.getData());
        return payload;
    }

    /**
     * 判断是否为上行消息
     *
     * @param method 消息方法
     * @return 是否为上行消息
     */
    private boolean isUpstreamMessage(String method) {
        IotDeviceMessageMethodEnum methodEnum = IotDeviceMessageMethodEnum.of(method);
        return methodEnum != null && methodEnum.getUpstream();
    }

}