package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.manager;

import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT Modbus TCP Slave 连接管理器
 * <p>
 * 管理设备 TCP 连接：socket ↔ 设备双向映射
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusTcpSlaveConnectionManager {

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
            deviceSocketMap.remove(info.getDeviceId());
            log.info("[removeConnection][设备 {} 连接已移除]", info.getDeviceId());
        }
        return info;
    }

    /**
     * 发送数据到设备
     */
    public void sendToDevice(Long deviceId, byte[] data) {
        NetSocket socket = deviceSocketMap.get(deviceId);
        if (socket == null) {
            log.warn("[sendToDevice][设备 {} 没有连接]", deviceId);
            return;
        }
        sendToSocket(socket, data);
    }

    /**
     * 发送数据到指定 socket
     */
    public void sendToSocket(NetSocket socket, byte[] data) {
        socket.write(Buffer.buffer(data));
    }

    /**
     * 关闭所有连接
     */
    public void closeAll() {
        for (NetSocket socket : connectionMap.keySet()) {
            try {
                socket.close();
            } catch (Exception e) {
                log.error("[closeAll][关闭连接失败]", e);
            }
        }
        connectionMap.clear();
        deviceSocketMap.clear();
    }

}
