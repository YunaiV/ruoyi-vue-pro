package cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager;

import cn.hutool.core.util.ObjUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关 UDP 会话管理器
 * <p>
 * 基于 Guava Cache 实现会话的自动过期清理：
 * 1. 管理设备会话信息（设备 ID -> 地址映射）
 * 2. 自动清理超时会话（expireAfterAccess）
 * 3. 限制最大会话数（maximumSize）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpSessionManager {

    /**
     * 设备会话缓存：设备 ID -> 会话信息
     * <p>
     * 使用 Guava Cache 自动管理过期：expireAfterAccess：每次访问（get/put）自动刷新过期时间
     */
    private final Cache<Long, SessionInfo> deviceSessionCache;

    private final int maxSessions;

    public IotUdpSessionManager(int maxSessions, long sessionTimeoutMs) {
        this.maxSessions = maxSessions;
        this.deviceSessionCache = CacheBuilder.newBuilder()
                .maximumSize(maxSessions)
                .expireAfterAccess(sessionTimeoutMs, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 注册设备会话
     *
     * @param deviceId    设备 ID
     * @param sessionInfo 会话信息
     */
    public synchronized void registerSession(Long deviceId, SessionInfo sessionInfo) {
        // 检查是否为新设备，且会话数已达上限（同步方法确保检查和注册的原子性）
        if (deviceSessionCache.getIfPresent(deviceId) == null
                && deviceSessionCache.size() >= maxSessions) {
            throw new IllegalStateException("会话数已达上限: " + maxSessions);
        }
        // 注册会话
        deviceSessionCache.put(deviceId, sessionInfo);
        log.info("[registerSession][注册设备会话，设备 ID: {}，地址: {}，productKey: {}，deviceName: {}]",
                deviceId, buildAddressKey(sessionInfo.getAddress()),
                sessionInfo.getProductKey(), sessionInfo.getDeviceName());
    }

    /**
     * 获取会话信息
     * <p>
     * 注意：调用此方法会自动刷新会话的过期时间
     *
     * @param deviceId 设备 ID
     * @return 会话信息，不存在则返回 null
     */
    public SessionInfo getSession(Long deviceId) {
        return deviceSessionCache.getIfPresent(deviceId);
    }

    /**
     * 更新设备会话地址（设备地址变更时调用）
     * <p>
     * 注意：getIfPresent 已自动刷新过期时间，无需重新 put
     *
     * @param deviceId   设备 ID
     * @param newAddress 新地址
     */
    public void updateSessionAddress(Long deviceId, InetSocketAddress newAddress) {
        // 地址未变化，无需更新
        SessionInfo sessionInfo = deviceSessionCache.getIfPresent(deviceId);
        if (sessionInfo == null) {
            return;
        }
        if (ObjUtil.equals(newAddress, sessionInfo.getAddress())) {
            return;
        }

        // 更新地址
        String oldAddressKey = buildAddressKey(sessionInfo.getAddress());
        sessionInfo.setAddress(newAddress);
        log.debug("[updateSessionAddress][更新设备地址，设备 ID: {}，旧地址: {}，新地址: {}]",
                deviceId, oldAddressKey, buildAddressKey(newAddress));
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
        SessionInfo sessionInfo = deviceSessionCache.getIfPresent(deviceId);
        if (sessionInfo == null || sessionInfo.getAddress() == null) {
            log.warn("[sendToDevice][设备会话不存在，设备 ID: {}]", deviceId);
            return false;
        }
        InetSocketAddress address = sessionInfo.getAddress();
        try {
            // 使用 CompletableFuture 同步等待发送结果
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            socket.send(Buffer.buffer(data), address.getPort(), address.getHostString(), result -> {
                if (result.succeeded()) {
                    log.debug("[sendToDevice][发送消息成功，设备 ID: {}，地址: {}，数据长度: {} 字节]",
                            deviceId, buildAddressKey(address), data.length);
                    future.complete(true);
                } else {
                    log.error("[sendToDevice][发送消息失败，设备 ID: {}，地址: {}]",
                            deviceId, buildAddressKey(address), result.cause());
                    future.complete(false);
                }
            });
            // 同步等待结果，超时 5 秒
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[sendToDevice][发送消息异常，设备 ID: {}]", deviceId, e);
            return false;
        }
    }

    /**
     * 构建地址 Key（用于日志输出）
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

    }

}
