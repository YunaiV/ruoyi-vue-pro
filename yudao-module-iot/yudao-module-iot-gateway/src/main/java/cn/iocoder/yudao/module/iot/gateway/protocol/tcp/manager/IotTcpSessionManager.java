package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager;

import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网关 TCP 会话管理器
 * <p>
 * 维护设备 ID 和 TCP 连接的映射关系，支持下行消息发送
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotTcpSessionManager {

    /**
     * 设备 ID -> TCP 连接的映射
     */
    private final Map<Long, NetSocket> deviceSocketMap = new ConcurrentHashMap<>();

    /**
     * TCP 连接 -> 设备 ID 的映射（用于连接断开时清理）
     */
    private final Map<NetSocket, Long> socketDeviceMap = new ConcurrentHashMap<>();

    /**
     * 注册设备会话
     *
     * @param deviceId 设备 ID
     * @param socket   TCP 连接
     */
    public void registerSession(Long deviceId, NetSocket socket) {
        // 如果设备已有连接，先断开旧连接
        NetSocket oldSocket = deviceSocketMap.get(deviceId);
        if (oldSocket != null && oldSocket != socket) {
            log.info("[registerSession][设备已有连接，断开旧连接] 设备 ID: {}, 旧连接: {}", deviceId, oldSocket.remoteAddress());
            oldSocket.close();
            socketDeviceMap.remove(oldSocket);
        }

        // 注册新连接
        deviceSocketMap.put(deviceId, socket);
        socketDeviceMap.put(socket, deviceId);

        log.info("[registerSession][注册设备会话] 设备 ID: {}, 连接: {}", deviceId, socket.remoteAddress());
    }

    /**
     * 注销设备会话
     *
     * @param deviceId 设备 ID
     */
    public void unregisterSession(Long deviceId) {
        NetSocket socket = deviceSocketMap.remove(deviceId);
        if (socket != null) {
            socketDeviceMap.remove(socket);
            log.info("[unregisterSession][注销设备会话] 设备 ID: {}, 连接: {}", deviceId, socket.remoteAddress());
        }
    }

    /**
     * 注销 TCP 连接会话
     *
     * @param socket TCP 连接
     */
    public void unregisterSession(NetSocket socket) {
        Long deviceId = socketDeviceMap.remove(socket);
        if (deviceId != null) {
            deviceSocketMap.remove(deviceId);
            log.info("[unregisterSession][注销连接会话] 设备 ID: {}, 连接: {}", deviceId, socket.remoteAddress());
        }
    }

    /**
     * 获取设备的 TCP 连接
     *
     * @param deviceId 设备 ID
     * @return TCP 连接，如果设备未连接则返回 null
     */
    public NetSocket getDeviceSocket(Long deviceId) {
        return deviceSocketMap.get(deviceId);
    }

    /**
     * 检查设备是否在线
     *
     * @param deviceId 设备 ID
     * @return 是否在线
     */
    public boolean isDeviceOnline(Long deviceId) {
        NetSocket socket = deviceSocketMap.get(deviceId);
        return socket != null;
    }

    /**
     * 发送消息到设备
     *
     * @param deviceId 设备 ID
     * @param data     消息数据
     * @return 是否发送成功
     */
    public boolean sendToDevice(Long deviceId, byte[] data) {
        NetSocket socket = deviceSocketMap.get(deviceId);
        if (socket == null) {
            log.warn("[sendToDevice][设备未连接] 设备 ID: {}", deviceId);
            return false;
        }

        try {
            socket.write(io.vertx.core.buffer.Buffer.buffer(data));
            log.debug("[sendToDevice][发送消息成功] 设备 ID: {}, 数据长度: {} 字节", deviceId, data.length);
            return true;
        } catch (Exception e) {
            log.error("[sendToDevice][发送消息失败] 设备 ID: {}", deviceId, e);
            // 发送失败时清理连接
            unregisterSession(deviceId);
            return false;
        }
    }

    /**
     * 获取当前在线设备数量
     *
     * @return 在线设备数量
     */
    public int getOnlineDeviceCount() {
        return deviceSocketMap.size();
    }

    /**
     * 获取所有在线设备 ID
     *
     * @return 在线设备 ID 集合
     */
    public java.util.Set<Long> getOnlineDeviceIds() {
        return deviceSocketMap.keySet();
    }
}