package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
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
public class IotTcpConnectionManager {

    /**
     * 最大连接数
     */
    private final int maxConnections;

    /**
     * 连接信息映射：NetSocket -> 连接信息
     */
    private final Map<NetSocket, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();

    /**
     * 设备 ID -> NetSocket 的映射
     */
    private final Map<Long, NetSocket> deviceSocketMap = new ConcurrentHashMap<>();

    public IotTcpConnectionManager(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * 注册设备连接（包含认证信息）
     *
     * @param socket         TCP 连接
     * @param deviceId       设备 ID
     * @param connectionInfo 连接信息
     */
    public synchronized void registerConnection(NetSocket socket, Long deviceId, ConnectionInfo connectionInfo) {
        // 检查连接数是否已达上限（同步方法确保检查和注册的原子性）
        if (connectionMap.size() >= maxConnections) {
            throw new IllegalStateException("连接数已达上限: " + maxConnections);
        }
        // 如果设备已有其他连接，先清理旧连接
        NetSocket oldSocket = deviceSocketMap.get(deviceId);
        if (oldSocket != null && oldSocket != socket) {
            log.info("[registerConnection][设备已有其他连接，断开旧连接，设备 ID: {}，旧连接: {}]",
                    deviceId, oldSocket.remoteAddress());
            // 先清理映射，再关闭连接
            connectionMap.remove(oldSocket);
            oldSocket.close();
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
        // 仅当 deviceSocketMap 中的 socket 是当前 socket 时才移除，避免误删新连接
        deviceSocketMap.remove(deviceId, socket);
        log.info("[unregisterConnection][注销设备连接，设备 ID: {}，连接: {}]", deviceId, socket.remoteAddress());
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
     * 关闭所有连接
     */
    public void closeAll() {
        // 1. 先复制再清空，避免 closeHandler 回调时并发修改
        List<NetSocket> sockets = new ArrayList<>(connectionMap.keySet());
        connectionMap.clear();
        deviceSocketMap.clear();
        // 2. 关闭所有连接（closeHandler 中 unregisterConnection 发现 map 为空会安全跳过）
        for (NetSocket socket : sockets) {
            try {
                socket.close();
            } catch (Exception ignored) {
                // 连接可能已关闭，忽略异常
            }
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

    }

}