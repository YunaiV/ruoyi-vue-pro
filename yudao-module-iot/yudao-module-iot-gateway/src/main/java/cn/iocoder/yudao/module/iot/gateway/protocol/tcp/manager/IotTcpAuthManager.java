package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager;

import io.vertx.core.net.NetSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网关 TCP 认证信息管理器
 * <p>
 * 维护 TCP 连接的认证状态，支持认证信息的存储、查询和清理
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotTcpAuthManager {

    /**
     * 连接认证状态映射：NetSocket -> 认证信息
     */
    private final Map<NetSocket, AuthInfo> authStatusMap = new ConcurrentHashMap<>();

    // TODO @haohao：得考虑，一个设备连接多次？
    /**
     * 设备 ID -> NetSocket 的映射（用于快速查找）
     */
    private final Map<Long, NetSocket> deviceSocketMap = new ConcurrentHashMap<>();

    /**
     * 注册认证信息
     *
     * @param socket   TCP 连接
     * @param authInfo 认证信息
     */
    public void registerAuth(NetSocket socket, AuthInfo authInfo) {
        // 如果设备已有其他连接，先清理旧连接
        // TODO @haohao：是不是允许同时连接？就像 mqtt 应该也允许重复连接哈？
        NetSocket oldSocket = deviceSocketMap.get(authInfo.getDeviceId());
        if (oldSocket != null && oldSocket != socket) {
            log.info("[registerAuth][设备已有其他连接，清理旧连接] 设备 ID: {}, 旧连接: {}",
                    authInfo.getDeviceId(), oldSocket.remoteAddress());
            authStatusMap.remove(oldSocket);
        }

        // 注册新认证信息
        authStatusMap.put(socket, authInfo);
        deviceSocketMap.put(authInfo.getDeviceId(), socket);

        log.info("[registerAuth][注册认证信息] 设备 ID: {}, 连接: {}, productKey: {}, deviceName: {}",
                authInfo.getDeviceId(), socket.remoteAddress(), authInfo.getProductKey(), authInfo.getDeviceName());
    }

    /**
     * 注销认证信息
     *
     * @param socket TCP 连接
     */
    public void unregisterAuth(NetSocket socket) {
        AuthInfo authInfo = authStatusMap.remove(socket);
        if (authInfo != null) {
            deviceSocketMap.remove(authInfo.getDeviceId());
            log.info("[unregisterAuth][注销认证信息] 设备 ID: {}, 连接: {}",
                    authInfo.getDeviceId(), socket.remoteAddress());
        }
    }

    // TODO @haohao：建议暂时没用的方法，可以删除掉；整体聚焦！
    /**
     * 注销设备认证信息
     *
     * @param deviceId 设备 ID
     */
    public void unregisterAuth(Long deviceId) {
        NetSocket socket = deviceSocketMap.remove(deviceId);
        if (socket != null) {
            AuthInfo authInfo = authStatusMap.remove(socket);
            if (authInfo != null) {
                log.info("[unregisterAuth][注销设备认证信息] 设备 ID: {}, 连接: {}",
                        deviceId, socket.remoteAddress());
            }
        }
    }

    /**
     * 获取认证信息
     *
     * @param socket TCP 连接
     * @return 认证信息，如果未认证则返回 null
     */
    public AuthInfo getAuthInfo(NetSocket socket) {
        return authStatusMap.get(socket);
    }

    /**
     * 获取设备的认证信息
     *
     * @param deviceId 设备 ID
     * @return 认证信息，如果设备未认证则返回 null
     */
    public AuthInfo getAuthInfo(Long deviceId) {
        NetSocket socket = deviceSocketMap.get(deviceId);
        return socket != null ? authStatusMap.get(socket) : null;
    }

    /**
     * 检查连接是否已认证
     *
     * @param socket TCP 连接
     * @return 是否已认证
     */
    public boolean isAuthenticated(NetSocket socket) {
        return authStatusMap.containsKey(socket);
    }

    /**
     * 检查设备是否已认证
     *
     * @param deviceId 设备 ID
     * @return 是否已认证
     */
    public boolean isAuthenticated(Long deviceId) {
        return deviceSocketMap.containsKey(deviceId);
    }

    /**
     * 获取设备的 TCP 连接
     *
     * @param deviceId 设备 ID
     * @return TCP 连接，如果设备未认证则返回 null
     */
    public NetSocket getDeviceSocket(Long deviceId) {
        return deviceSocketMap.get(deviceId);
    }

    /**
     * 获取当前已认证设备数量
     *
     * @return 已认证设备数量
     */
    public int getAuthenticatedDeviceCount() {
        return deviceSocketMap.size();
    }

    /**
     * 获取所有已认证设备 ID
     *
     * @return 已认证设备 ID 集合
     */
    public java.util.Set<Long> getAuthenticatedDeviceIds() {
        return deviceSocketMap.keySet();
    }

    /**
     * 清理所有认证信息
     */
    public void clearAll() {
        int count = authStatusMap.size();
        authStatusMap.clear();
        deviceSocketMap.clear();
        // TODO @haohao：第一个括号是方法，第二个括号是明细日志；其它日志，也可以检查下哈。
        log.info("[clearAll][清理所有认证信息] 清理数量: {}", count);
    }

    /**
     * 认证信息
     */
    @Data
    public static class AuthInfo {

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

        // TODO @haohao：令牌不要存储，万一有安全问题哈；
        /**
         * 认证令牌
         */
        private String token;

        /**
         * 客户端 ID
         */
        private String clientId;

    }

}