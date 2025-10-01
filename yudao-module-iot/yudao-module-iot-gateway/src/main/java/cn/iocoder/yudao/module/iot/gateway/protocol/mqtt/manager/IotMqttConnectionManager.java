package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager;

import cn.hutool.core.util.StrUtil;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网关 MQTT 连接管理器
 * <p>
 * 统一管理 MQTT 连接的认证状态、设备会话和消息发送功能：
 * 1. 管理 MQTT 连接的认证状态
 * 2. 管理设备会话和在线状态
 * 3. 管理消息发送到设备
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotMqttConnectionManager {

    /**
     * 未知地址常量（当获取端点地址失败时使用）
     */
    private static final String UNKNOWN_ADDRESS = "unknown";

    /**
     * 连接信息映射：MqttEndpoint -> 连接信息
     */
    private final Map<MqttEndpoint, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();

    /**
     * 设备 ID -> MqttEndpoint 的映射
     */
    private final Map<Long, MqttEndpoint> deviceEndpointMap = new ConcurrentHashMap<>();

    /**
     * 安全获取 endpoint 地址
     * <p>
     * 优先从缓存获取地址，缓存为空时再尝试实时获取
     *
     * @param endpoint MQTT 连接端点
     * @return 地址字符串，获取失败时返回 "unknown"
     */
    public String getEndpointAddress(MqttEndpoint endpoint) {
        String realTimeAddress = UNKNOWN_ADDRESS;
        if (endpoint == null) {
            return realTimeAddress;
        }

        // 1. 优先从缓存获取（避免连接关闭时的异常）
        ConnectionInfo connectionInfo = connectionMap.get(endpoint);
        if (connectionInfo != null && StrUtil.isNotBlank(connectionInfo.getRemoteAddress())) {
            return connectionInfo.getRemoteAddress();
        }

        // 2. 缓存为空时尝试实时获取
        try {
            realTimeAddress = endpoint.remoteAddress().toString();
        } catch (Exception ignored) {
            // 连接已关闭，忽略异常
        }

        return realTimeAddress;
    }

    /**
     * 注册设备连接（包含认证信息）
     *
     * @param endpoint       MQTT 连接端点
     * @param deviceId       设备 ID
     * @param connectionInfo 连接信息
     */
    public void registerConnection(MqttEndpoint endpoint, Long deviceId, ConnectionInfo connectionInfo) {
        // 如果设备已有其他连接，先清理旧连接
        MqttEndpoint oldEndpoint = deviceEndpointMap.get(deviceId);
        if (oldEndpoint != null && oldEndpoint != endpoint) {
            log.info("[registerConnection][设备已有其他连接，断开旧连接，设备 ID: {}，旧连接: {}]",
                    deviceId, getEndpointAddress(oldEndpoint));
            oldEndpoint.close();
            // 清理旧连接的映射
            connectionMap.remove(oldEndpoint);
        }

        connectionMap.put(endpoint, connectionInfo);
        deviceEndpointMap.put(deviceId, endpoint);

        log.info("[registerConnection][注册设备连接，设备 ID: {}，连接: {}，product key: {}，device name: {}]",
                deviceId, getEndpointAddress(endpoint), connectionInfo.getProductKey(), connectionInfo.getDeviceName());
    }

    /**
     * 注销设备连接
     *
     * @param endpoint MQTT 连接端点
     */
    public void unregisterConnection(MqttEndpoint endpoint) {
        ConnectionInfo connectionInfo = connectionMap.remove(endpoint);
        if (connectionInfo != null) {
            Long deviceId = connectionInfo.getDeviceId();
            deviceEndpointMap.remove(deviceId);

            log.info("[unregisterConnection][注销设备连接，设备 ID: {}，连接: {}]", deviceId,
                    getEndpointAddress(endpoint));
        }
    }

    /**
     * 获取连接信息
     */
    public ConnectionInfo getConnectionInfo(MqttEndpoint endpoint) {
        return connectionMap.get(endpoint);
    }

    /**
     * 根据设备 ID 获取连接信息
     *
     * @param deviceId 设备 ID
     * @return 连接信息
     */
    public IotMqttConnectionManager.ConnectionInfo getConnectionInfoByDeviceId(Long deviceId) {
        // 通过设备 ID 获取连接端点
        MqttEndpoint endpoint = getDeviceEndpoint(deviceId);
        if (endpoint == null) {
            return null;
        }

        // 获取连接信息
        return getConnectionInfo(endpoint);
    }

    /**
     * 检查设备是否在线
     */
    public boolean isDeviceOnline(Long deviceId) {
        return deviceEndpointMap.containsKey(deviceId);
    }

    /**
     * 检查设备是否离线
     */
    public boolean isDeviceOffline(Long deviceId) {
        return !isDeviceOnline(deviceId);
    }

    /**
     * 发送消息到设备
     *
     * @param deviceId 设备 ID
     * @param topic    主题
     * @param payload  消息内容
     * @param qos      服务质量
     * @param retain   是否保留消息
     * @return 是否发送成功
     */
    public boolean sendToDevice(Long deviceId, String topic, byte[] payload, int qos, boolean retain) {
        MqttEndpoint endpoint = deviceEndpointMap.get(deviceId);
        if (endpoint == null) {
            log.warn("[sendToDevice][设备离线，无法发送消息，设备 ID: {}，主题: {}]", deviceId, topic);
            return false;
        }

        try {
            endpoint.publish(topic, io.vertx.core.buffer.Buffer.buffer(payload), MqttQoS.valueOf(qos), false, retain);
            log.debug("[sendToDevice][发送消息成功，设备 ID: {}，主题: {}，QoS: {}]", deviceId, topic, qos);
            return true;
        } catch (Exception e) {
            log.error("[sendToDevice][发送消息失败，设备 ID: {}，主题: {}，错误: {}]", deviceId, topic, e.getMessage());
            return false;
        }
    }

    /**
     * 获取设备连接端点
     */
    public MqttEndpoint getDeviceEndpoint(Long deviceId) {
        return deviceEndpointMap.get(deviceId);
    }

    /**
     * 连接信息
     */
    @Data
    public static class ConnectionInfo {

        /**
         * 设备 ID
         */
        private Long deviceId;

        /**
         * 产品 Key
         */
        private String productKey;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 客户端 ID
         */
        private String clientId;

        /**
         * 是否已认证
         */
        private boolean authenticated;

        /**
         * 连接地址
         */
        private String remoteAddress;

    }

}
