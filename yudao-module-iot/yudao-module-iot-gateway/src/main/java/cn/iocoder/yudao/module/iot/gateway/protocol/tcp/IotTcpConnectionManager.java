package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * IoT TCP 连接管理器
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotTcpConnectionManager {

    /**
     * 连接集合
     *
     * key：设备唯一标识
     */
    private final ConcurrentMap<String, NetSocket> connectionMap = new ConcurrentHashMap<>();

    /**
     * 添加一个新连接
     *
     * @param deviceId 设备唯一标识
     * @param socket   Netty Channel
     */
    public void addConnection(String deviceId, NetSocket socket) {
        log.info("[addConnection][设备({}) 连接({})]", deviceId, socket.remoteAddress());
        connectionMap.put(deviceId, socket);
    }

    /**
     * 根据设备 ID 获取连接
     *
     * @param deviceId 设备 ID
     * @return 连接
     */
    public NetSocket getConnection(String deviceId) {
        return connectionMap.get(deviceId);
    }

    /**
     * 移除指定连接
     *
     * @param socket Netty Channel
     */
    public void removeConnection(NetSocket socket) {
        connectionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(socket))
                .findFirst()
                .ifPresent(entry -> {
                    log.info("[removeConnection][设备({}) 断开连接({})]", entry.getKey(), socket.remoteAddress());
                    connectionMap.remove(entry.getKey());
                });
    }

}