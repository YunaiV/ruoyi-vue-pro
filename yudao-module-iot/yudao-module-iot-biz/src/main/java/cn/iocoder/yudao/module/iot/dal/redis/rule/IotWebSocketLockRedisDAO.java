package cn.iocoder.yudao.module.iot.dal.redis.rule;

import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants.WEBSOCKET_CONNECT_LOCK;

/**
 * IoT WebSocket 连接锁 Redis DAO
 * <p>
 * 用于保证 WebSocket 重连操作的线程安全，避免多线程同时重连导致的资源竞争
 *
 * @author HUIHUI
 */
@Repository
public class IotWebSocketLockRedisDAO {

    /**
     * 锁等待超时时间（毫秒）
     */
    private static final long LOCK_WAIT_TIME_MS = 5000;

    /**
     * 锁持有超时时间（毫秒）
     */
    private static final long LOCK_LEASE_TIME_MS = 10000;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 在分布式锁保护下执行操作
     *
     * @param serverUrl WebSocket 服务器地址
     * @param runnable  需要执行的操作
     * @throws Exception 如果获取锁超时或执行操作时发生异常
     */
    public void lock(String serverUrl, Runnable runnable) throws Exception {
        String lockKey = formatKey(serverUrl);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取分布式锁
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME_MS, LOCK_LEASE_TIME_MS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new RuntimeException("获取 WebSocket 连接锁超时，服务器: " + serverUrl);
            }

            // 执行操作
            runnable.run();
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private static String formatKey(String serverUrl) {
        return String.format(WEBSOCKET_CONNECT_LOCK, serverUrl);
    }

}
