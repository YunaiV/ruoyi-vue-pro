package cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 网关 UDP 会话管理器
 * <p>
 * 统一管理 UDP 会话的认证状态、设备会话和消息发送功能：
 * 1. 管理 UDP 会话的认证状态
 * 2. 管理设备会话和在线状态
 * 3. 管理消息发送到设备
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpSessionManager {

    /**
     * 最大会话数
     */
    private final int maxSessions;

    /**
     * 设备 ID -> 会话信息
     */
    private final Map<Long, SessionInfo> deviceSessionMap = new ConcurrentHashMap<>();

    /**
     * 设备地址 Key -> 设备 ID（反向映射，用于清理时同步）
     */
    // TODO @AI：1）这个变量是否必须？2）unregisterSession 这个方法是否必须？
    private final Map<String, Long> addressDeviceMap = new ConcurrentHashMap<>();

    public IotUdpSessionManager(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    /**
     * 注册设备会话（包含认证信息）
     *
     * @param deviceId    设备 ID
     * @param sessionInfo 会话信息
     */
    public void registerSession(Long deviceId, SessionInfo sessionInfo) {
        // 检查会话数是否已达上限
        if (deviceSessionMap.size() >= maxSessions) {
            throw new IllegalStateException("会话数已达上限: " + maxSessions);
        }
        // 如果设备已有其他会话，先清理旧会话
        SessionInfo oldSessionInfo = deviceSessionMap.get(deviceId);
        if (oldSessionInfo != null) {
            String oldAddressKey = buildAddressKey(oldSessionInfo.getAddress());
            addressDeviceMap.remove(oldAddressKey, deviceId);
            log.info("[registerSession][设备已有其他会话，清理旧会话，设备 ID: {}，旧地址: {}]",
                    deviceId, oldAddressKey);
        }

        // 注册新会话
        String addressKey = buildAddressKey(sessionInfo.getAddress());
        deviceSessionMap.put(deviceId, sessionInfo);
        addressDeviceMap.put(addressKey, deviceId);
        log.info("[registerSession][注册设备会话，设备 ID: {}，地址: {}，product key: {}，device name: {}]",
                deviceId, addressKey, sessionInfo.getProductKey(), sessionInfo.getDeviceName());
    }

    /**
     * 注销设备会话
     *
     * @param deviceId 设备 ID
     */
    public void unregisterSession(Long deviceId) {
        SessionInfo sessionInfo = deviceSessionMap.remove(deviceId);
        if (sessionInfo == null) {
            return;
        }
        String addressKey = buildAddressKey(sessionInfo.getAddress());
        // 仅当 addressDeviceMap 中的 deviceId 是当前 deviceId 时才移除，避免误删新会话
        addressDeviceMap.remove(addressKey, deviceId);
        log.info("[unregisterSession][注销设备会话，设备 ID: {}，地址: {}]", deviceId, addressKey);
    }

    /**
     * 更新会话活跃时间（每次收到上行消息时调用）
     *
     * @param deviceId 设备 ID
     */
    public void updateSessionActivity(Long deviceId) {
        SessionInfo sessionInfo = deviceSessionMap.get(deviceId);
        if (sessionInfo != null) {
            sessionInfo.setLastActiveTime(LocalDateTime.now());
        }
    }

    /**
     * 更新设备会话地址（设备地址变更时调用）
     *
     * @param deviceId   设备 ID
     * @param newAddress 新地址
     */
    public void updateSessionAddress(Long deviceId, InetSocketAddress newAddress) {
        SessionInfo sessionInfo = deviceSessionMap.get(deviceId);
        if (sessionInfo == null) {
            return;
        }
        // 清理旧地址映射
        String oldAddressKey = buildAddressKey(sessionInfo.getAddress());
        addressDeviceMap.remove(oldAddressKey, deviceId);

        // 更新新地址
        String newAddressKey = buildAddressKey(newAddress);
        sessionInfo.setAddress(newAddress);
        sessionInfo.setLastActiveTime(LocalDateTime.now());
        addressDeviceMap.put(newAddressKey, deviceId);
        log.debug("[updateSessionAddress][更新设备地址，设备 ID: {}，新地址: {}]", deviceId, newAddressKey);
    }

    /**
     * 获取会话信息
     */
    public SessionInfo getSessionInfo(Long deviceId) {
        return deviceSessionMap.get(deviceId);
    }

    /**
     * 发送消息到设备
     *
     * @param deviceId 设备 ID
     * @param data     数据
     * @param socket   UDP Socket
     * @return 是否发送成功
     */
    public boolean sendToDevice(Long deviceId, byte[] data, DatagramSocket socket) {
        SessionInfo sessionInfo = deviceSessionMap.get(deviceId);
        if (sessionInfo == null || sessionInfo.getAddress() == null) {
            log.warn("[sendToDevice][设备会话不存在，设备 ID: {}]", deviceId);
            return false;
        }
        InetSocketAddress address = sessionInfo.getAddress();
        try {
            socket.send(Buffer.buffer(data), address.getPort(), address.getHostString(), result -> {
                if (result.succeeded()) {
                    log.debug("[sendToDevice][发送消息成功，设备 ID: {}，地址: {}，数据长度: {} 字节]",
                            deviceId, buildAddressKey(address), data.length);
                    return;
                }
                log.error("[sendToDevice][发送消息失败，设备 ID: {}，地址: {}]",
                        deviceId, buildAddressKey(address), result.cause());
            });
            return true;
        } catch (Exception e) {
            log.error("[sendToDevice][发送消息异常，设备 ID: {}]", deviceId, e);
            return false;
        }
    }

    /**
     * 定期清理不活跃的设备会话
     *
     * @param timeoutMs 超时时间（毫秒）
     * @return 清理的设备 ID 列表（用于发送离线消息）
     */
    public List<Long> cleanExpiredSessions(long timeoutMs) {
        List<Long> offlineDeviceIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.minusNanos(timeoutMs * 1_000_000);
        Iterator<Map.Entry<Long, SessionInfo>> iterator = deviceSessionMap.entrySet().iterator();
        // TODO @AI：改成 for each 会不会更好？
        while (iterator.hasNext()) {
            Map.Entry<Long, SessionInfo> entry = iterator.next();
            SessionInfo sessionInfo = entry.getValue();
            // 未过期，跳过
            if (sessionInfo.getLastActiveTime().isAfter(expireTime)) {
                continue;
            }
            // 过期处理：记录离线设备 ID
            Long deviceId = entry.getKey();
            String addressKey = buildAddressKey(sessionInfo.getAddress());
            addressDeviceMap.remove(addressKey, deviceId);
            offlineDeviceIds.add(deviceId);
            log.debug("[cleanExpiredSessions][清理超时设备，设备 ID: {}，地址: {}，最后活跃时间: {}]",
                    deviceId, addressKey, sessionInfo.getLastActiveTime());
            iterator.remove();
        }
        return offlineDeviceIds;
    }

    /**
     * 构建地址 Key
     *
     * @param address 地址
     * @return 地址 Key
     */
    public String buildAddressKey(InetSocketAddress address) {
        return address.getHostString() + ":" + address.getPort();
    }

    /**
     * 会话信息（包含认证信息）
     */
    @Data
    public static class SessionInfo {

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
         * 设备地址
         */
        private InetSocketAddress address;
        /**
         * 最后活跃时间
         */
        private LocalDateTime lastActiveTime;

    }

}
