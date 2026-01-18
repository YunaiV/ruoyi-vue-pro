package cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.manager;

import cn.hutool.core.collection.CollUtil;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * IoT MQTT WebSocket 连接管理器
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotMqttWsConnectionManager {

    /**
     * 存储设备连接
     * Key: 设备标识（deviceKey）
     * Value: WebSocket 连接
     */
    private final Map<String, ServerWebSocket> connections = new ConcurrentHashMap<>();

    /**
     * 存储设备标识与 Socket ID 的映射
     * Key: 设备标识（deviceKey）
     * Value: Socket ID（UUID）
     */
    private final Map<String, String> deviceKeyToSocketId = new ConcurrentHashMap<>();

    /**
     * 存储 Socket ID 与设备标识的映射
     * Key: Socket ID（UUID）
     * Value: 设备标识（deviceKey）
     */
    private final Map<String, String> socketIdToDeviceKey = new ConcurrentHashMap<>();

    /**
     * 存储设备订阅的主题
     * Key: 设备标识（deviceKey）
     * Value: 订阅的主题集合
     */
    private final Map<String, Set<String>> deviceSubscriptions = new ConcurrentHashMap<>();

    /**
     * 添加连接
     *
     * @param deviceKey 设备标识
     * @param socket    WebSocket 连接
     * @param socketId  Socket ID（UUID）
     */
    public void addConnection(String deviceKey, ServerWebSocket socket, String socketId) {
        connections.put(deviceKey, socket);
        deviceKeyToSocketId.put(deviceKey, socketId);
        socketIdToDeviceKey.put(socketId, deviceKey);
        log.info("[addConnection][设备连接已添加，deviceKey: {}，socketId: {}，当前连接数: {}]",
                deviceKey, socketId, connections.size());
    }

    /**
     * 移除连接
     *
     * @param deviceKey 设备标识
     */
    public void removeConnection(String deviceKey) {
        ServerWebSocket socket = connections.remove(deviceKey);
        String socketId = deviceKeyToSocketId.remove(deviceKey);
        if (socketId != null) {
            socketIdToDeviceKey.remove(socketId);
        }
        if (socket != null) {
            log.info("[removeConnection][设备连接已移除，deviceKey: {}，socketId: {}，当前连接数: {}]",
                    deviceKey, socketId, connections.size());
        }
    }

    /**
     * 根据 Socket ID 移除连接
     *
     * @param socketId WebSocket 文本框架 ID
     */
    public void removeConnectionBySocketId(String socketId) {
        String deviceKey = socketIdToDeviceKey.remove(socketId);
        if (deviceKey != null) {
            connections.remove(deviceKey);
            log.info("[removeConnectionBySocketId][设备连接已移除，socketId: {}，deviceKey: {}，当前连接数: {}]",
                    socketId, deviceKey, connections.size());
        }
    }

    /**
     * 获取连接
     *
     * @param deviceKey 设备标识
     * @return WebSocket 连接
     */
    public ServerWebSocket getConnection(String deviceKey) {
        return connections.get(deviceKey);
    }

    /**
     * 根据 Socket ID 获取设备标识
     *
     * @param socketId WebSocket 文本框架 ID
     * @return 设备标识
     */
    public String getDeviceKeyBySocketId(String socketId) {
        return socketIdToDeviceKey.get(socketId);
    }

    /**
     * 检查设备是否在线
     *
     * @param deviceKey 设备标识
     * @return 是否在线
     */
    public boolean isOnline(String deviceKey) {
        return connections.containsKey(deviceKey);
    }

    /**
     * 获取当前连接数
     *
     * @return 连接数
     */
    public int getConnectionCount() {
        return connections.size();
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        connections.forEach((deviceKey, socket) -> {
            try {
                socket.close();
                log.info("[closeAllConnections][关闭设备连接，deviceKey: {}]", deviceKey);
            } catch (Exception e) {
                log.error("[closeAllConnections][关闭设备连接失败，deviceKey: {}]", deviceKey, e);
            }
        });
        connections.clear();
        deviceKeyToSocketId.clear();
        socketIdToDeviceKey.clear();
        deviceSubscriptions.clear();
        log.info("[closeAllConnections][所有连接已关闭]");
    }

    // ==================== 订阅管理方法 ====================

    /**
     * 添加订阅
     *
     * @param deviceKey 设备标识
     * @param topic     订阅主题
     */
    public void addSubscription(String deviceKey, String topic) {
        deviceSubscriptions.computeIfAbsent(deviceKey, k -> new CopyOnWriteArraySet<>()).add(topic);
        log.debug("[addSubscription][设备订阅主题，deviceKey: {}，topic: {}]", deviceKey, topic);
    }

    /**
     * 移除订阅
     *
     * @param deviceKey 设备标识
     * @param topic     订阅主题
     */
    public void removeSubscription(String deviceKey, String topic) {
        Set<String> topics = deviceSubscriptions.get(deviceKey);
        if (topics != null) {
            topics.remove(topic);
            log.debug("[removeSubscription][设备取消订阅，deviceKey: {}，topic: {}]", deviceKey, topic);
        }
    }

    /**
     * 检查设备是否订阅了指定主题
     * 支持 MQTT 通配符匹配（+ 和 #）
     *
     * @param deviceKey 设备标识
     * @param topic     发布主题
     * @return 是否匹配
     */
    public boolean isSubscribed(String deviceKey, String topic) {
        Set<String> subscriptions = deviceSubscriptions.get(deviceKey);
        if (CollUtil.isEmpty(subscriptions)) {
            return false;
        }

        // 检查是否有匹配的订阅
        for (String subscription : subscriptions) {
            if (topicMatches(subscription, topic)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取设备的所有订阅
     *
     * @param deviceKey 设备标识
     * @return 订阅主题集合
     */
    public Set<String> getSubscriptions(String deviceKey) {
        return deviceSubscriptions.get(deviceKey);
    }

    // TODO @haohao：这个方法，是不是也可以考虑抽到 IotMqttTopicUtils 里面去哈；感觉更简洁一点？
    /**
     * MQTT 主题匹配
     * 支持通配符：
     * - +：匹配单层主题
     * - #：匹配多层主题（必须在末尾）
     *
     * @param subscription 订阅主题（可能包含通配符）
     * @param topic        发布主题（不包含通配符）
     * @return 是否匹配
     */
    private boolean topicMatches(String subscription, String topic) {
        // 完全匹配
        if (subscription.equals(topic)) {
            return true;
        }

        // 不包含通配符
        // TODO @haohao：这里要不要枚举下哈；+ #
        if (!subscription.contains("+") && !subscription.contains("#")) {
            return false;
        }

        String[] subscriptionParts = subscription.split("/");
        String[] topicParts = topic.split("/");
        int i = 0;
        for (; i < subscriptionParts.length && i < topicParts.length; i++) {
            String subPart = subscriptionParts[i];
            String topicPart = topicParts[i];

            // # 匹配剩余所有层级，且必须在末尾
            if (subPart.equals("#")) {
                return i == subscriptionParts.length - 1;
            }

            // 不是通配符且不匹配
            if (!subPart.equals("+") && !subPart.equals(topicPart)) {
                return false;
            }
        }

        // 检查是否都匹配完
        return i == subscriptionParts.length && i == topicParts.length;
    }

}
