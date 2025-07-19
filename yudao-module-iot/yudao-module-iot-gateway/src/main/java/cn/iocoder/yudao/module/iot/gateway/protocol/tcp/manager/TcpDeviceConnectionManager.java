package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager;

import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.client.TcpDeviceClient;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TCP 设备连接管理器
 * <p>
 * 参考 EMQX 设计理念：
 * 1. 高性能连接管理
 * 2. 连接池和资源管理
 * 3. 流量控制 TODO @haohao：这个要不先去掉
 * 4. 监控统计 TODO @haohao：这个要不先去掉
 * 5. 自动清理和容错
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class TcpDeviceConnectionManager {

    // ==================== 连接存储 ====================

    /**
     * 设备客户端映射
     * Key: 设备地址, Value: 设备客户端
     */
    private final ConcurrentMap<String, TcpDeviceClient> clientMap = new ConcurrentHashMap<>();

    /**
     * 设备ID到设备地址的映射
     * Key: 设备ID, Value: 设备地址
     */
    private final ConcurrentMap<Long, String> deviceIdToAddrMap = new ConcurrentHashMap<>();

    /**
     * 套接字到客户端的映射，用于快速查找
     * Key: NetSocket, Value: 设备地址
     */
    private final ConcurrentMap<NetSocket, String> socketToAddrMap = new ConcurrentHashMap<>();

    // ==================== 读写锁 ====================

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    // ==================== 定时任务 ====================

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3, r -> {
        Thread t = new Thread(r, "tcp-connection-manager");
        t.setDaemon(true);
        return t;
    });

    // ==================== 统计信息 ====================

    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong totalDisconnections = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalFailedMessages = new AtomicLong(0);
    private final AtomicLong totalBytes = new AtomicLong(0);

    // ==================== 配置参数 ====================

    private static final int MAX_CONNECTIONS = 10000;
    private static final int HEARTBEAT_CHECK_INTERVAL = 30; // 秒
    private static final int CONNECTION_CLEANUP_INTERVAL = 60; // 秒
    private static final int STATS_LOG_INTERVAL = 300; // 秒

    /**
     * 构造函数，启动定时任务
     */
    public TcpDeviceConnectionManager() {
        startScheduledTasks();
    }

    /**
     * 启动定时任务
     */
    private void startScheduledTasks() {
        // 心跳检查任务
        scheduler.scheduleAtFixedRate(this::checkHeartbeat,
                HEARTBEAT_CHECK_INTERVAL, HEARTBEAT_CHECK_INTERVAL, TimeUnit.SECONDS);

        // 连接清理任务
        scheduler.scheduleAtFixedRate(this::cleanupConnections,
                CONNECTION_CLEANUP_INTERVAL, CONNECTION_CLEANUP_INTERVAL, TimeUnit.SECONDS);

        // 统计日志任务
        scheduler.scheduleAtFixedRate(this::logStatistics,
                STATS_LOG_INTERVAL, STATS_LOG_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 添加设备客户端
     */
    public boolean addClient(String deviceAddr, TcpDeviceClient client) {
        // TODO @haohao：这个要不去掉；目前看着没做 result 的处理；
        if (clientMap.size() >= MAX_CONNECTIONS) {
            log.warn("[addClient][连接数已达上限({})，拒绝新连接: {}]", MAX_CONNECTIONS, deviceAddr);
            return false;
        }

        writeLock.lock();
        try {
            log.info("[addClient][添加设备客户端: {}]", deviceAddr);

            // 关闭之前的连接（如果存在）
            TcpDeviceClient existingClient = clientMap.get(deviceAddr);
            if (existingClient != null) {
                log.warn("[addClient][设备({})已存在连接，关闭旧连接]", deviceAddr);
                removeClientInternal(deviceAddr, existingClient);
            }

            // 添加新连接
            clientMap.put(deviceAddr, client);

            // 添加套接字映射
            if (client.getSocket() != null) {
                socketToAddrMap.put(client.getSocket(), deviceAddr);
            }

            // 如果客户端已设置设备 ID，更新映射
            if (client.getDeviceId() != null) {
                deviceIdToAddrMap.put(client.getDeviceId(), deviceAddr);
            }

            totalConnections.incrementAndGet();
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 移除设备客户端
     */
    public void removeClient(String deviceAddr) {
        writeLock.lock();
        try {
            TcpDeviceClient client = clientMap.get(deviceAddr);
            if (client != null) {
                removeClientInternal(deviceAddr, client);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 内部移除客户端方法（无锁）
     */
    private void removeClientInternal(String deviceAddr, TcpDeviceClient client) {
        log.info("[removeClient][移除设备客户端: {}]", deviceAddr);

        // 从映射中移除
        clientMap.remove(deviceAddr);

        // 移除套接字映射
        if (client.getSocket() != null) {
            socketToAddrMap.remove(client.getSocket());
        }

        // 移除设备ID映射
        if (client.getDeviceId() != null) {
            deviceIdToAddrMap.remove(client.getDeviceId());
        }

        // 关闭连接
        client.shutdown();

        totalDisconnections.incrementAndGet();
    }

    /**
     * 通过设备地址获取客户端
     */
    public TcpDeviceClient getClient(String deviceAddr) {
        readLock.lock();
        try {
            return clientMap.get(deviceAddr);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 通过设备 ID 获取客户端
     */
    public TcpDeviceClient getClientByDeviceId(Long deviceId) {
        readLock.lock();
        try {
            String deviceAddr = deviceIdToAddrMap.get(deviceId);
            return deviceAddr != null ? clientMap.get(deviceAddr) : null;
        } finally {
            readLock.unlock();
        }
    }

    // TODO @haohao：getClientBySocket、isDeviceOnline、sendMessage、sendMessageByDeviceId、broadcastMessage 用不到的方法，要不先暂时不提供？保持简洁、更容易理解哈。

    /**
     * 通过网络连接获取客户端
     */
    public TcpDeviceClient getClientBySocket(NetSocket socket) {
        readLock.lock();
        try {
            String deviceAddr = socketToAddrMap.get(socket);
            return deviceAddr != null ? clientMap.get(deviceAddr) : null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 检查设备是否在线
     */
    public boolean isDeviceOnline(Long deviceId) {
        TcpDeviceClient client = getClientByDeviceId(deviceId);
        return client != null && client.isOnline();
    }

    /**
     * 设置设备 ID 映射
     */
    public void setDeviceIdMapping(String deviceAddr, Long deviceId) {
        writeLock.lock();
        try {
            TcpDeviceClient client = clientMap.get(deviceAddr);
            if (client != null) {
                client.setDeviceId(deviceId);
                deviceIdToAddrMap.put(deviceId, deviceAddr);
                log.debug("[setDeviceIdMapping][设置设备ID映射: {} -> {}]", deviceAddr, deviceId);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 发送消息给设备
     */
    public boolean sendMessage(String deviceAddr, Buffer buffer) {
        TcpDeviceClient client = getClient(deviceAddr);
        if (client != null && client.isOnline()) {
            try {
                client.sendMessage(buffer);
                totalMessages.incrementAndGet();
                totalBytes.addAndGet(buffer.length());
                return true;
            } catch (Exception e) {
                totalFailedMessages.incrementAndGet();
                log.error("[sendMessage][发送消息失败] 设备地址: {}", deviceAddr, e);
                return false;
            }
        }
        log.warn("[sendMessage][设备({})不在线，无法发送消息]", deviceAddr);
        return false;
    }

    /**
     * 通过设备ID发送消息
     */
    public boolean sendMessageByDeviceId(Long deviceId, Buffer buffer) {
        TcpDeviceClient client = getClientByDeviceId(deviceId);
        if (client != null && client.isOnline()) {
            try {
                client.sendMessage(buffer);
                totalMessages.incrementAndGet();
                totalBytes.addAndGet(buffer.length());
                return true;
            } catch (Exception e) {
                totalFailedMessages.incrementAndGet();
                log.error("[sendMessageByDeviceId][发送消息失败] 设备ID: {}", deviceId, e);
                return false;
            }
        }
        log.warn("[sendMessageByDeviceId][设备ID({})不在线，无法发送消息]", deviceId);
        return false;
    }

    /**
     * 广播消息给所有在线设备
     */
    public int broadcastMessage(Buffer buffer) {
        int successCount = 0;
        readLock.lock();
        try {
            for (TcpDeviceClient client : clientMap.values()) {
                if (client.isOnline()) {
                    try {
                        client.sendMessage(buffer);
                        successCount++;
                    } catch (Exception e) {
                        log.error("[broadcastMessage][广播消息失败] 设备: {}", client.getDeviceAddr(), e);
                    }
                }
            }
        } finally {
            readLock.unlock();
        }

        totalMessages.addAndGet(successCount);
        totalBytes.addAndGet((long) successCount * buffer.length());
        return successCount;
    }

    /**
     * 获取在线设备数量
     */
    public int getOnlineCount() {
        readLock.lock();
        try {
            return (int) clientMap.values().stream()
                    .filter(TcpDeviceClient::isOnline)
                    .count();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 获取总连接数
     */
    public int getTotalCount() {
        return clientMap.size();
    }

    /**
     * 获取认证设备数量
     */
    public int getAuthenticatedCount() {
        readLock.lock();
        try {
            return (int) clientMap.values().stream()
                    .filter(TcpDeviceClient::isAuthenticated)
                    .count();
        } finally {
            readLock.unlock();
        }
    }

    // TODO @haohao：心跳超时，需要 close 么？
    /**
     * 心跳检查任务
     */
    private void checkHeartbeat() {
        try {
            int offlineCount = 0;

            readLock.lock();
            try {
                for (TcpDeviceClient client : clientMap.values()) {
                    if (!client.isOnline()) {
                        offlineCount++;
                    }
                }
            } finally {
                readLock.unlock();
            }

            if (offlineCount > 0) {
                log.info("[checkHeartbeat][发现 {} 个离线设备，将在清理任务中处理]", offlineCount);
            }
        } catch (Exception e) {
            log.error("[checkHeartbeat][心跳检查任务异常]", e);
        }
    }

    /**
     * 连接清理任务
     */
    private void cleanupConnections() {
        try {
            int beforeSize = clientMap.size();

            writeLock.lock();
            try {
                clientMap.entrySet().removeIf(entry -> {
                    TcpDeviceClient client = entry.getValue();
                    if (!client.isOnline()) {
                        log.debug("[cleanupConnections][清理离线连接: {}]", entry.getKey());

                        // 清理相关映射
                        if (client.getSocket() != null) {
                            socketToAddrMap.remove(client.getSocket());
                        }
                        if (client.getDeviceId() != null) {
                            deviceIdToAddrMap.remove(client.getDeviceId());
                        }

                        client.shutdown();
                        totalDisconnections.incrementAndGet();
                        return true;
                    }
                    return false;
                });
            } finally {
                writeLock.unlock();
            }

            int afterSize = clientMap.size();
            if (beforeSize != afterSize) {
                log.info("[cleanupConnections][清理完成] 连接数: {} -> {}, 清理数: {}",
                        beforeSize, afterSize, beforeSize - afterSize);
            }
        } catch (Exception e) {
            log.error("[cleanupConnections][连接清理任务异常]", e);
        }
    }

    /**
     * 统计日志任务
     */
    private void logStatistics() {
        try {
            long totalConn = totalConnections.get();
            long totalDisconnections = this.totalDisconnections.get();
            long totalMsg = totalMessages.get();
            long totalFailedMsg = totalFailedMessages.get();
            long totalBytesValue = totalBytes.get();

            log.info("[logStatistics][连接统计] 总连接: {}, 总断开: {}, 当前在线: {}, 认证设备: {}, " +
                            "总消息: {}, 失败消息: {}, 总字节: {}",
                    totalConn, totalDisconnections, getOnlineCount(), getAuthenticatedCount(),
                    totalMsg, totalFailedMsg, totalBytesValue);
        } catch (Exception e) {
            log.error("[logStatistics][统计日志任务异常]", e);
        }
    }

    /**
     * 关闭连接管理器
     */
    public void shutdown() {
        log.info("[shutdown][关闭TCP连接管理器]");

        // 关闭定时任务
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 关闭所有连接
        writeLock.lock();
        try {
            clientMap.values().forEach(TcpDeviceClient::shutdown);
            clientMap.clear();
            deviceIdToAddrMap.clear();
            socketToAddrMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 获取连接状态信息
     */
    public String getConnectionStatus() {
        return String.format("总连接数: %d, 在线设备: %d, 认证设备: %d, 成功率: %.2f%%",
                getTotalCount(), getOnlineCount(), getAuthenticatedCount(),
                totalMessages.get() > 0
                        ? (double) (totalMessages.get() - totalFailedMessages.get()) / totalMessages.get() * 100
                        : 0.0);
    }

    /**
     * 获取详细统计信息
     */
    public String getDetailedStatistics() {
        return String.format(
                "TCP连接管理器统计:\n" +
                        "- 当前连接数: %d\n" +
                        "- 在线设备数: %d\n" +
                        "- 认证设备数: %d\n" +
                        "- 历史总连接: %d\n" +
                        "- 历史总断开: %d\n" +
                        "- 总消息数: %d\n" +
                        "- 失败消息数: %d\n" +
                        "- 总字节数: %d\n" +
                        "- 消息成功率: %.2f%%",
                getTotalCount(), getOnlineCount(), getAuthenticatedCount(),
                totalConnections.get(), totalDisconnections.get(),
                totalMessages.get(), totalFailedMessages.get(), totalBytes.get(),
                totalMessages.get() > 0
                        ? (double) (totalMessages.get() - totalFailedMessages.get()) / totalMessages.get() * 100
                        : 0.0);
    }

}