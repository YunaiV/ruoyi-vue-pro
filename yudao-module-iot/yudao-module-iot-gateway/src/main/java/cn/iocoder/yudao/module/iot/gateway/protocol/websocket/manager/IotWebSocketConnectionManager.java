package cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager;

import io.vertx.core.http.ServerWebSocket;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网关 WebSocket 连接管理器
 * <p>
 * 统一管理 WebSocket 连接的认证状态、设备会话和消息发送功能：
 * 1. 管理 WebSocket 连接的认证状态
 * 2. 管理设备会话和在线状态
 * 3. 管理消息发送到设备
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotWebSocketConnectionManager {

    /**
     * 连接信息映射：ServerWebSocket -> 连接信息
     */
    private final Map<ServerWebSocket, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();

    /**
     * 设备 ID -> ServerWebSocket 的映射
     */
    private final Map<Long, ServerWebSocket> deviceSocketMap = new ConcurrentHashMap<>();

    /**
     * 注册设备连接（包含认证信息）
     *
     * @param socket         WebSocket 连接
     * @param deviceId       设备 ID
     * @param connectionInfo 连接信息
     */
    public void registerConnection(ServerWebSocket socket, Long deviceId, ConnectionInfo connectionInfo) {
        // 如果设备已有其他连接，先清理旧连接
        ServerWebSocket oldSocket = deviceSocketMap.get(deviceId);
        if (oldSocket != null && oldSocket != socket) {
            log.info("[registerConnection][设备已有其他连接，断开旧连接，设备 ID: {}，旧连接: {}]",
                    deviceId, oldSocket.remoteAddress());
            oldSocket.close();
            // 清理旧连接的映射
            connectionMap.remove(oldSocket);
        }

        // 注册新连接
        connectionMap.put(socket, connectionInfo);
        deviceSocketMap.put(deviceId, socket);
        log.info("[registerConnection][注册设备连接，设备 ID: {}，连接: {}，product key: {}，device name: {}]",
                deviceId, socket.remoteAddress(), connectionInfo.getProductKey(), connectionInfo.getDeviceName());
    }

    /**
     * 注销设备连接
     *
     * @param socket WebSocket 连接
     */
    public void unregisterConnection(ServerWebSocket socket) {
        ConnectionInfo connectionInfo = connectionMap.remove(socket);
        if (connectionInfo == null) {
            return;
        }
        Long deviceId = connectionInfo.getDeviceId();
        deviceSocketMap.remove(deviceId);
        log.info("[unregisterConnection][注销设备连接，设备 ID: {}，连接: {}]",
                deviceId, socket.remoteAddress());
    }

    /**
     * 获取连接信息
     */
    public ConnectionInfo getConnectionInfo(ServerWebSocket socket) {
        return connectionMap.get(socket);
    }

    /**
     * 根据设备 ID 获取连接信息
     */
    public ConnectionInfo getConnectionInfoByDeviceId(Long deviceId) {
        ServerWebSocket socket = deviceSocketMap.get(deviceId);
        return socket != null ? connectionMap.get(socket) : null;
    }

    /**
     * 发送消息到设备（文本消息）
     *
     * @param deviceId 设备 ID
     * @param message  JSON 消息
     * @return 是否发送成功
     */
    public boolean sendToDevice(Long deviceId, String message) {
        ServerWebSocket socket = deviceSocketMap.get(deviceId);
        if (socket == null) {
            log.warn("[sendToDevice][设备未连接，设备 ID: {}]", deviceId);
            return false;
        }

        try {
            socket.writeTextMessage(message);
            log.debug("[sendToDevice][发送消息成功，设备 ID: {}，数据长度: {} 字节]", deviceId, message.length());
            return true;
        } catch (Exception e) {
            log.error("[sendToDevice][发送消息失败，设备 ID: {}]", deviceId, e);
            // 发送失败时清理连接
            unregisterConnection(socket);
            return false;
        }
    }

    /**
     * 连接信息（包含认证信息）
     */
    @Data
    @Accessors(chain = true)
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
         * 消息编解码类型（认证后确定）
         */
        private String codecType;

    }

}
