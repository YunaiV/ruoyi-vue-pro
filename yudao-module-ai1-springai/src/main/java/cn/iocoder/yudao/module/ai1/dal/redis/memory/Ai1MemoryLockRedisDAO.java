package cn.iocoder.yudao.module.ai1.dal.redis.memory;

import javax.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.function.Supplier;

import static cn.iocoder.yudao.module.ai1.dal.redis.Ai1RedisKeyConstants.MEMORY_ARCHIVE_LOCK;
import static cn.iocoder.yudao.module.ai1.dal.redis.Ai1RedisKeyConstants.MEMORY_SNAPSHOT_LOCK;

/**
 * AI1 长期记忆的锁 Redis DAO
 *
 * 不设置租期，由 Redisson 看门狗续期
 *
 * @author 芋道源码
 */
@Repository
public class Ai1MemoryLockRedisDAO {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 按租户尝试获取归档锁并执行；其他节点归档中时不等待、不执行
     *
     * @param tenantId 租户编号
     * @param supplier 归档逻辑
     * @return 执行结果；未获取到锁时返回 null
     */
    public <V> V tryLockArchive(Long tenantId, Supplier<V> supplier) {
        RLock lock = redissonClient.getLock(formatArchiveKey(tenantId));
        if (!lock.tryLock()) {
            return null;
        }
        try {
            return supplier.get();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取用户在 Agent 下的快照锁并执行：消息落库事务与归档读取新消息互斥
     *
     * @param agentId  Agent 编号
     * @param userId   用户编号
     * @param supplier 加锁执行的逻辑
     * @return 执行结果
     */
    public <V> V lockSnapshot(Long agentId, Long userId, Supplier<V> supplier) {
        RLock lock = redissonClient.getLock(formatSnapshotKey(agentId, userId));
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    private static String formatArchiveKey(Long tenantId) {
        return String.format(MEMORY_ARCHIVE_LOCK, tenantId);
    }

    private static String formatSnapshotKey(Long agentId, Long userId) {
        return String.format(MEMORY_SNAPSHOT_LOCK, agentId, userId);
    }

}
