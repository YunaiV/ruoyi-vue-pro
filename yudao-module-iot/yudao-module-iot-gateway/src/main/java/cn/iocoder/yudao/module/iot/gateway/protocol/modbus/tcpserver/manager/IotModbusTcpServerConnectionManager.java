package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.manager;

import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT Modbus TCP Server 连接管理器
 * <p>
 * 管理设备 TCP 连接：socket ↔ 设备双向映射
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpServerConnectionManager {

    /**
     * socket → 连接信息
     */
    private final Map<NetSocket, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();

    /**
     * deviceId → socket
     */
    private final Map<Long, NetSocket> deviceSocketMap = new ConcurrentHashMap<>();

    /**
     * 连接信息
     */
    @Data
    @Accessors(chain = true)
    public static class ConnectionInfo {

        /**
         * 设备编号
         */
        private Long deviceId;
        /**
         * 产品标识
         */
        private String productKey;
        /**
         * 设备名称
         */
        private String deviceName;
        /**
         * 从站地址
         */
        private Integer slaveId;

        /**
         * 帧格式（首帧自动检测得到）
         */
        private IotModbusFrameFormatEnum frameFormat;

    }

    /**
     * 注册已认证的连接
     */
    public void registerConnection(NetSocket socket, ConnectionInfo info) {
        // 先检查该设备是否有旧连接，若有且不是同一个 socket，关闭旧 socket
        NetSocket oldSocket = deviceSocketMap.get(info.getDeviceId());
        if (oldSocket != null && oldSocket != socket) {
            log.info("[registerConnection][设备 {} 存在旧连接, 关闭旧 socket, oldRemote={}, newRemote={}]",
                    info.getDeviceId(), oldSocket.remoteAddress(), socket.remoteAddress());
            connectionMap.remove(oldSocket);
            try {
                oldSocket.close();
            } catch (Exception e) {
                log.warn("[registerConnection][关闭旧 socket 失败, deviceId={}, oldRemote={}]",
                        info.getDeviceId(), oldSocket.remoteAddress(), e);
            }
        }

        // 注册新连接
        connectionMap.put(socket, info);
        deviceSocketMap.put(info.getDeviceId(), socket);
        log.info("[registerConnection][设备 {} 连接已注册, remoteAddress={}]",
                info.getDeviceId(), socket.remoteAddress());
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
     * 获取所有已连接设备的 ID 集合
     */
    public Set<Long> getConnectedDeviceIds() {
        return deviceSocketMap.keySet();
    }

    /**
     * 移除连接
     */
    public ConnectionInfo removeConnection(NetSocket socket) {
        ConnectionInfo info = connectionMap.remove(socket);
        if (info != null && info.getDeviceId() != null) {
            // 使用两参数 remove：只有当 deviceSocketMap 中对应的 socket 就是当前 socket 时才删除，
            // 避免新 socket 已注册后旧 socket 关闭时误删新映射
            boolean removed = deviceSocketMap.remove(info.getDeviceId(), socket);
            if (removed) {
                log.info("[removeConnection][设备 {} 连接已移除]", info.getDeviceId());
            } else {
                log.info("[removeConnection][设备 {} 旧连接关闭, 新连接仍在线, 跳过清理]", info.getDeviceId());
            }
        }
        return info;
    }

    /**
     * 发送数据到设备
     *
     * @return 发送结果 Future
     */
    public Future<Void> sendToDevice(Long deviceId, byte[] data) {
        NetSocket socket = deviceSocketMap.get(deviceId);
        if (socket == null) {
            log.warn("[sendToDevice][设备 {} 没有连接]", deviceId);
            return Future.failedFuture("设备 " + deviceId + " 没有连接");
        }
        return sendToSocket(socket, data);
    }

    /**
     * 发送数据到指定 socket
     *
     * @return 发送结果 Future
     */
    public Future<Void> sendToSocket(NetSocket socket, byte[] data) {
        return socket.write(Buffer.buffer(data));
    }

    /**
     * 关闭所有连接
     */
    public void closeAll() {
        // 1. 先复制再清空，避免 closeHandler 回调时并发修改
        List<NetSocket> sockets = new ArrayList<>(connectionMap.keySet());
        connectionMap.clear();
        deviceSocketMap.clear();
        // 2. 关闭所有 socket（closeHandler 中 removeConnection 发现 map 为空会安全跳过）
        for (NetSocket socket : sockets) {
            try {
                socket.close();
            } catch (Exception e) {
                log.error("[closeAll][关闭连接失败]", e);
            }
        }
    }

}
