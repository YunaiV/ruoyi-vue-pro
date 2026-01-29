package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网关 TCP 连接管理器
 * <p>
 * 统一管理 TCP 连接的认证状态、设备会话和消息发送功能：
 * 1. 管理 TCP 连接的认证状态
 * 2. 管理设备会话和在线状态
 * 3. 管理消息发送到设备
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotTcpConnectionManager {

    /**
     * 连接信息映射：NetSocket -> 连接信息
     */
    private final Map<NetSocket, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();

    /**
     * 设备 ID -> NetSocket 的映射
     */
    private final Map<Long, NetSocket> deviceSocketMap = new ConcurrentHashMap<>();

    /**
     * 注册设备连接（包含认证信息）
     *
     * @param socket         TCP 连接
     * @param deviceId       设备 ID
     * @param connectionInfo 连接信息
     */
    public void registerConnection(NetSocket socket, Long deviceId, ConnectionInfo connectionInfo) {
        // 如果设备已有其他连接，先清理旧连接
        NetSocket oldSocket = deviceSocketMap.get(deviceId);
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
     * @param socket TCP 连接
     */
    public void unregisterConnection(NetSocket socket) {
        ConnectionInfo connectionInfo = connectionMap.remove(socket);
        if (connectionInfo == null) {
            return;
        }
        Long deviceId = connectionInfo.getDeviceId();
        deviceSocketMap.remove(deviceId);
        log.info("[unregisterConnection][注销设备连接，设备 ID: {}，连接: {}]", deviceId, socket.remoteAddress());
    }

    /**
     * 检查连接是否已认证
     */
    public boolean isAuthenticated(NetSocket socket) {
        ConnectionInfo info = connectionMap.get(socket);
        return info != null;
    }

    /**
     * 检查连接是否未认证
     */
    public boolean isNotAuthenticated(NetSocket socket) {
        return !isAuthenticated(socket);
    }

    /**
     * 获取连接信息
     */
    public ConnectionInfo getConnectionInfo(NetSocket socket) {
        return connectionMap.get(socket);
    }

    /**
     * 根据设备 ID 获取连接信息
     */
    public ConnectionInfo getConnectionInfoByDeviceId(Long deviceId) {
        NetSocket socket = deviceSocketMap.get(deviceId);
        return socket != null ? connectionMap.get(socket) : null;
    }

    /**
     * 发送消息到设备
     */
    public boolean sendToDevice(Long deviceId, byte[] data) {
        NetSocket socket = deviceSocketMap.get(deviceId);
        if (socket == null) {
            log.warn("[sendToDevice][设备未连接，设备 ID: {}]", deviceId);
            return false;
        }

        try {
            socket.write(Buffer.buffer(data));
            log.debug("[sendToDevice][发送消息成功，设备 ID: {}，数据长度: {} 字节]", deviceId, data.length);
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