package cn.iocoder.yudao.framework.sse.core.session;

import cn.iocoder.yudao.framework.sse.core.codec.SseEventEncoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * SSE 连接管理器
 *
 * 1. 双索引：connectionId → 连接；tenantId:userType:userId → connectionId 集合；变更在同一把锁内完成，查询无锁
 * 2. 同一用户、同一 clientId 重新连接时，替换旧连接
 * 3. 清理：{@link #remove(String)} 幂等，SseEmitter 回调、发送异常、心跳兜底扫描、停机统一调用
 *
 * @author 芋道源码
 */
@Slf4j
public class SseEmitterSessionManager {

    /**
     * 等待发送锁的超时时间，单位：毫秒
     */
    private final long sendTimeoutMillis;

    /**
     * connectionId 与连接的映射
     */
    private final ConcurrentMap<String, SseConnection> connections = new ConcurrentHashMap<>();
    /**
     * 用户与 connectionId 的映射
     *
     * key：tenantId:userType:userId
     * value：connectionId 集合；单用户连接数很少，使用 CopyOnWriteArraySet 支持无锁查询
     */
    private final ConcurrentMap<String, Set<String>> userConnectionIds = new ConcurrentHashMap<>();
    /**
     * 索引锁：保证 {@link #connections}、{@link #userConnectionIds} 两个索引的变更原子一致；查询不加锁
     */
    private final Object indexLock = new Object();

    /**
     * 是否已停机，停机后拒绝新连接
     */
    private volatile boolean shutdown;

    public SseEmitterSessionManager(long sendTimeoutMillis) {
        this.sendTimeoutMillis = sendTimeoutMillis;
    }

    /**
     * 注册连接
     *
     * @param connection 连接
     * @return 是否注册成功；已停机时返回 false
     */
    public boolean register(SseConnection connection) {
        if (shutdown) {
            return false;
        }
        connection.setCloseHandler(reason -> close(connection, reason));
        // 1. 加入索引。在锁内检查是否已关闭：关闭回调总是先 markClosed 再 remove，所以不会残留
        SseConnection replaced;
        synchronized (indexLock) {
            if (connection.isClosed()) {
                return false;
            }
            // 1.1 同一 clientId 的旧连接，会被替换
            replaced = findByClientId(getUserConnections(connection.getUserKey()), connection.getClientId());
            if (replaced != null) {
                removeIndex(replaced);
            }
            // 1.2 加入索引
            connections.put(connection.getConnectionId(), connection);
            userConnectionIds.computeIfAbsent(connection.getUserKey(), key -> new CopyOnWriteArraySet<>())
                    .add(connection.getConnectionId());
        }
        // 2. 在锁外关闭旧连接：会发送 close 事件，涉及网络 IO
        if (replaced != null) {
            close(replaced, SseCloseReasonEnum.REPLACED);
        }
        // 3. 注册期间已停机，回滚注册，避免残留
        if (shutdown) {
            remove(connection);
            return false;
        }
        return true;
    }

    /**
     * 获得指定用户的连接列表，调用方需持有 {@link #indexLock}
     */
    private List<SseConnection> getUserConnections(String userKey) {
        Set<String> ids = userConnectionIds.get(userKey);
        if (CollUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<SseConnection> result = new ArrayList<>(ids.size());
        ids.forEach(id -> {
            SseConnection connection = connections.get(id);
            if (connection != null) {
                result.add(connection);
            }
        });
        return result;
    }

    private static SseConnection findByClientId(List<SseConnection> connections, String clientId) {
        if (StrUtil.isEmpty(clientId)) {
            return null;
        }
        return CollUtil.findOne(connections, connection -> ObjUtil.equal(connection.getClientId(), clientId));
    }

    /**
     * 移除连接，幂等，重复调用安全
     *
     * @param connectionId 连接编号
     */
    public void remove(String connectionId) {
        if (connectionId == null) {
            return;
        }
        synchronized (indexLock) {
            SseConnection connection = connections.get(connectionId);
            if (connection != null) {
                removeIndex(connection);
            }
        }
    }

    /**
     * 移除连接，幂等，重复调用安全
     *
     * @param connection 连接
     */
    public void remove(SseConnection connection) {
        synchronized (indexLock) {
            removeIndex(connection);
        }
    }

    /**
     * 移除索引，调用方需持有 {@link #indexLock}
     *
     * @param connection 连接
     */
    private void removeIndex(SseConnection connection) {
        connections.remove(connection.getConnectionId(), connection);
        Set<String> ids = userConnectionIds.get(connection.getUserKey());
        if (ids == null) {
            return;
        }
        ids.remove(connection.getConnectionId());
        if (ids.isEmpty()) {
            userConnectionIds.remove(connection.getUserKey(), ids);
        }
    }

    /**
     * 服务端主动关闭连接：先发送 close 事件，再 complete，最后移除
     *
     * @param connection 连接
     * @param reason 关闭原因
     */
    public void close(SseConnection connection, SseCloseReasonEnum reason) {
        if (!connection.markClosed()) {
            remove(connection);
            return;
        }
        try {
            // 尽量发送 close 事件，失败不影响后续清理；close 事件与 complete 在发送锁内完成，之后不会再发送其他事件
            connection.closeWithLastEvent(SseEventEncoder.encodeClose(reason), sendTimeoutMillis);
        } catch (Exception ex) {
            log.debug("[close][连接({}) 关闭异常，原因({})]", connection.getConnectionId(), reason, ex);
        } finally {
            remove(connection);
        }
        log.info("[close][连接({}) 用户({}) 已关闭，原因({})]", connection.getConnectionId(), connection.getUserKey(), reason);
    }

    /**
     * 连接已不可用（发送失败、慢客户端），直接以异常结束并移除，不再发送 close 事件
     *
     * @param connection 连接
     * @param ex 异常，可为空
     */
    public void abort(SseConnection connection, Throwable ex) {
        if (connection.markClosed()) {
            try {
                if (ex != null) {
                    connection.getEmitter().completeWithError(ex);
                } else {
                    connection.getEmitter().complete();
                }
            } catch (Exception closeEx) {
                log.debug("[abort][连接({}) 结束异常]", connection.getConnectionId(), closeEx);
            }
        }
        remove(connection);
    }

    /**
     * 停机：拒绝新连接，向全部连接发送 close: SHUTDOWN 后 complete
     */
    public void shutdown() {
        shutdown = true;
        List<SseConnection> list = new ArrayList<>(connections.values());
        list.forEach(connection -> close(connection, SseCloseReasonEnum.SHUTDOWN));
        if (CollUtil.isNotEmpty(list)) {
            log.info("[shutdown][已关闭 {} 个 SSE 连接]", list.size());
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    // ========== 查询 ==========

    public SseConnection getConnection(String connectionId) {
        return connectionId != null ? connections.get(connectionId) : null;
    }

    /**
     * 获得指定用户的连接列表
     *
     * @param tenantId 租户编号；为空时，匹配所有租户
     * @param userType 用户类型
     * @param userId 用户编号
     * @return 连接列表
     */
    public List<SseConnection> getConnectionList(Long tenantId, Integer userType, Long userId) {
        if (tenantId != null) {
            Set<String> ids = userConnectionIds.get(SseConnection.buildUserKey(tenantId, userType, userId));
            if (CollUtil.isEmpty(ids)) {
                return Collections.emptyList();
            }
            List<SseConnection> result = new ArrayList<>(ids.size());
            ids.forEach(id -> {
                SseConnection connection = connections.get(id);
                if (connection != null) {
                    result.add(connection);
                }
            });
            return result;
        }
        return filter(null, userType, userId);
    }

    /**
     * 获得指定用户类型的连接列表
     *
     * @param tenantId 租户编号；为空时，匹配所有租户（系统级广播）
     * @param userType 用户类型
     * @return 连接列表
     */
    public List<SseConnection> getConnectionList(Long tenantId, Integer userType) {
        return filter(tenantId, userType, null);
    }

    public Collection<SseConnection> getConnections() {
        return Collections.unmodifiableCollection(connections.values());
    }

    public int getConnectionCount() {
        return connections.size();
    }

    /**
     * 按租户、用户类型、用户编号过滤连接
     *
     * @param tenantId 租户编号；为空时，不过滤租户
     * @param userType 用户类型
     * @param userId 用户编号；为空时，不过滤用户
     * @return 连接列表
     */
    private List<SseConnection> filter(Long tenantId, Integer userType, Long userId) {
        List<SseConnection> result = new ArrayList<>();
        for (SseConnection connection : connections.values()) {
            if (tenantId != null && ObjUtil.notEqual(tenantId, connection.getTenantId())) {
                continue;
            }
            if (ObjUtil.notEqual(userType, connection.getUserType())) {
                continue;
            }
            if (userId != null && ObjUtil.notEqual(userId, connection.getUserId())) {
                continue;
            }
            result.add(connection);
        }
        return result;
    }

}
