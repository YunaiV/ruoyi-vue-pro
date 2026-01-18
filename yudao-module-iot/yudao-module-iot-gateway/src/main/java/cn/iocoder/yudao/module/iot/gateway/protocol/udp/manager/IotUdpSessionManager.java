package cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Iterator;
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
     * 设备 ID -> 设备地址（用于下行消息发送）
     */
    private final Map<Long, InetSocketAddress> deviceAddressMap = new ConcurrentHashMap<>();

    /**
     * 设备地址 Key -> 最后活跃时间（用于清理）
     */
    // TODO @AI：是不是尽量使用 LocalDateTime ？统一时间类型
    private final Map<String, Long> lastActiveTimeMap = new ConcurrentHashMap<>();

    /**
     * 设备地址 Key -> 设备 ID（反向映射，用于清理时同步）
     */
    private final Map<String, Long> addressDeviceMap = new ConcurrentHashMap<>();

    /**
     * 更新设备地址（每次收到上行消息时调用）
     *
     * @param deviceId 设备 ID
     * @param address  设备地址
     */
    public void updateDeviceAddress(Long deviceId, InetSocketAddress address) {
        String addressKey = buildAddressKey(address);
        // 更新设备地址映射
        deviceAddressMap.put(deviceId, address);
        lastActiveTimeMap.put(addressKey, System.currentTimeMillis());
        addressDeviceMap.put(addressKey, deviceId);
        log.debug("[updateDeviceAddress][更新设备地址，设备 ID: {}，地址: {}]", deviceId, addressKey);
    }

    // TODO @AI：是不是用不到？用不掉就删除掉！简化
    /**
     * 获取设备地址（下行消息发送时使用）
     *
     * @param deviceId 设备 ID
     * @return 设备地址，如果不存在返回 null
     */
    public InetSocketAddress getDeviceAddress(Long deviceId) {
        return deviceAddressMap.get(deviceId);
    }

    /**
     * 检查设备是否在线（即是否有地址映射）
     *
     * @param deviceId 设备 ID
     * @return 是否在线
     */
    public boolean isDeviceOnline(Long deviceId) {
        return deviceAddressMap.containsKey(deviceId);
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
        InetSocketAddress address = deviceAddressMap.get(deviceId);
        if (address == null) {
            log.warn("[sendToDevice][设备地址不存在，设备 ID: {}]", deviceId);
            return false;
        }

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
    // TODO @AI：目前暂时用不到 address 字段，是不是只返回 list of deviceId 就行？简化
    public java.util.List<DeviceOfflineInfo> cleanExpiredMappings(long timeoutMs) {
        java.util.List<DeviceOfflineInfo> offlineDevices = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = lastActiveTimeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() > timeoutMs) {
                String addressKey = entry.getKey();
                Long deviceId = addressDeviceMap.remove(addressKey);
                // TODO @AI：if continue，减少括号层级；
                if (deviceId != null) {
                    InetSocketAddress address = deviceAddressMap.remove(deviceId);
                    if (address != null) {
                        // 获取设备信息用于发送离线消息
                        offlineDevices.add(new DeviceOfflineInfo(deviceId, addressKey));
                        log.info("[cleanExpiredMappings][清理超时设备，设备 ID: {}，地址: {}，最后活跃时间: {}ms 前]",
                                deviceId, addressKey, now - entry.getValue());
                    }
                }
                iterator.remove();
            }
        }
        return offlineDevices;
    }

    // TODO @AI：是不是用不到？用不掉就删除掉！简化
    /**
     * 移除设备地址映射
     *
     * @param deviceId 设备 ID
     */
    public void removeDeviceAddress(Long deviceId) {
        InetSocketAddress address = deviceAddressMap.remove(deviceId);
        if (address != null) {
            String addressKey = buildAddressKey(address);
            lastActiveTimeMap.remove(addressKey);
            addressDeviceMap.remove(addressKey);
            log.debug("[removeDeviceAddress][移除设备地址，设备 ID: {}，地址: {}]", deviceId, addressKey);
        }
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
     * 设备离线信息
     */
    @Data
    public static class DeviceOfflineInfo {

        /**
         * 设备 ID
         */
        private final Long deviceId;

        /**
         * 设备地址
         */
        private final String address;

        public DeviceOfflineInfo(Long deviceId, String address) {
            this.deviceId = deviceId;
            this.address = address;
        }

    }

}
