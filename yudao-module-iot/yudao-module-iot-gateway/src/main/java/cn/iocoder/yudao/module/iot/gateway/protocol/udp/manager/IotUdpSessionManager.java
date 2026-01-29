package cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
 * 采用无状态设计，SessionManager 主要用于：
 * 1. 管理设备地址映射（用于下行消息发送）
 * 2. 定期清理不活跃的设备地址映射
 * <p>
 * 注意：UDP 是无连接协议，上行消息通过 token 验证身份，不依赖会话状态
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotUdpSessionManager {

    /**
     * 设备 ID -> 会话信息（包含地址和 codecType）
     */
    private final Map<Long, SessionInfo> deviceSessionMap = new ConcurrentHashMap<>();

    /**
     * 设备地址 Key -> 最后活跃时间（用于清理）
     */
    private final Map<String, LocalDateTime> lastActiveTimeMap = new ConcurrentHashMap<>();

    /**
     * 设备地址 Key -> 设备 ID（反向映射，用于清理时同步）
     */
    private final Map<String, Long> addressDeviceMap = new ConcurrentHashMap<>();

    /**
     * 更新设备会话（每次收到上行消息时调用）
     *
     * @param deviceId  设备 ID
     * @param address   设备地址
     * @param codecType 消息编解码类型
     */
    public void updateDeviceSession(Long deviceId, InetSocketAddress address, String codecType) {
        String addressKey = buildAddressKey(address);
        // 更新设备会话映射
        deviceSessionMap.put(deviceId, new SessionInfo().setAddress(address).setCodecType(codecType));
        lastActiveTimeMap.put(addressKey, LocalDateTime.now());
        addressDeviceMap.put(addressKey, deviceId);
        log.debug("[updateDeviceSession][更新设备会话，设备 ID: {}，地址: {}，codecType: {}]", deviceId, addressKey, codecType);
    }

    /**
     * 更新设备地址（兼容旧接口，默认不更新 codecType）
     *
     * @param deviceId 设备 ID
     * @param address  设备地址
     */
    public void updateDeviceAddress(Long deviceId, InetSocketAddress address) {
        SessionInfo sessionInfo = deviceSessionMap.get(deviceId);
        String codecType = sessionInfo != null ? sessionInfo.getCodecType() : null;
        updateDeviceSession(deviceId, address, codecType);
    }

    /**
     * 获取设备会话信息
     *
     * @param deviceId 设备 ID
     * @return 会话信息
     */
    public SessionInfo getSessionInfo(Long deviceId) {
        return deviceSessionMap.get(deviceId);
    }

    /**
     * 检查设备是否在线（即是否有地址映射）
     *
     * @param deviceId 设备 ID
     * @return 是否在线
     */
    public boolean isDeviceOnline(Long deviceId) {
        return deviceSessionMap.containsKey(deviceId);
    }

    /**
     * 检查设备是否离线
     *
     * @param deviceId 设备 ID
     * @return 是否离线
     */
    public boolean isDeviceOffline(Long deviceId) {
        return !isDeviceOnline(deviceId);
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
                } else {
                    log.error("[sendToDevice][发送消息失败，设备 ID: {}，地址: {}]",
                            deviceId, buildAddressKey(address), result.cause());
                }
            });
            return true;
        } catch (Exception e) {
            log.error("[sendToDevice][发送消息异常，设备 ID: {}]", deviceId, e);
            return false;
        }
    }

    /**
     * 定期清理不活跃的设备地址映射
     *
     * @param timeoutMs 超时时间（毫秒）
     * @return 清理的设备 ID 列表（用于发送离线消息）
     */
    public List<Long> cleanExpiredMappings(long timeoutMs) {
        List<Long> offlineDeviceIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.minusNanos(timeoutMs * 1_000_000);
        Iterator<Map.Entry<String, LocalDateTime>> iterator = lastActiveTimeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            // 未过期，跳过
            Map.Entry<String, LocalDateTime> entry = iterator.next();
            if (entry.getValue().isAfter(expireTime)) {
                continue;
            }
            // 过期处理：记录离线设备 ID
            String addressKey = entry.getKey();
            Long deviceId = addressDeviceMap.remove(addressKey);
            if (deviceId == null) {
                iterator.remove();
                continue;
            }
            SessionInfo sessionInfo = deviceSessionMap.remove(deviceId);
            if (sessionInfo == null) {
                iterator.remove();
                continue;
            }
            offlineDeviceIds.add(deviceId);
            log.debug("[cleanExpiredMappings][清理超时设备，设备 ID: {}，地址: {}，最后活跃时间: {}]",
                    deviceId, addressKey, entry.getValue());
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
     * 会话信息
     */
    @Data
    public static class SessionInfo {

        /**
         * 设备地址
         */
        private InetSocketAddress address;

        /**
         * 消息编解码类型
         */
        private String codecType;

    }

}
