package cn.iocoder.yudao.module.im.dal.redis.rtc;

import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.IM_RTC_CALL_LOCK;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.RTC_INVITE_BUSY;

/**
 * IM 通话同对 / 同群活跃唯一性的锁 Redis DAO
 * <p>
 * invite 入口包一层；锁内做「SELECT 已有活跃通话 → 命中即加入分支；否则 INSERT 新通话」
 *
 * @author 芋道源码
 */
@Repository
@Slf4j
public class ImRtcCallLockRedisDAO {

    /**
     * 等待获取锁的最长时间；超时抛 RTC_INVITE_BUSY
     */
    private static final long LOCK_WAIT_MS = 5_000L;
    /**
     * 持有锁的最长时间；自动释放兜底；给 LiveKit / DB / 推送偶发慢留余地
     */
    private static final long LOCK_LEASE_MS = 30_000L;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 私聊通话锁；按 userId 排序拼 key 保对称（同对呼叫和反向呼叫共用一把锁）
     */
    public <V> V lockPrivate(Long userIdA, Long userIdB, Callable<V> callable) throws Exception {
        String key = String.format(IM_RTC_CALL_LOCK, ImConversationTypeEnum.PRIVATE.getType(),
                Math.min(userIdA, userIdB)+ "_" + Math.max(userIdA, userIdB));
        return doLock(key, callable);
    }

    /**
     * 群通话锁；同群所有 invite 串行
     */
    public <V> V lockGroup(Long groupId, Callable<V> callable) throws Exception {
        String key = String.format(IM_RTC_CALL_LOCK, ImConversationTypeEnum.GROUP.getType(), groupId);
        return doLock(key, callable);
    }

    /**
     * tryLock(waitTime, leaseTime, unit)：waitTime 内拿不到锁直接抛繁忙；拿到后 leaseTime 自动释放
     * <p>
     * unlock 前用 isHeldByCurrentThread 兜底；业务超过 leaseTime 时锁已自动释放，不再抛 IllegalMonitorStateException
     */
    private <V> V doLock(String lockKey, Callable<V> callable) throws Exception {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = lock.tryLock(LOCK_WAIT_MS, LOCK_LEASE_MS, TimeUnit.MILLISECONDS);
        if (!acquired) {
            log.error("[doLock][lockKey={} 等待 {}ms 仍未获取到锁]", lockKey, LOCK_WAIT_MS);
            throw exception(RTC_INVITE_BUSY);
        }
        try {
            return callable.call();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            } else {
                log.error("[doLock][lockKey={} 业务超过 leaseTime={}ms，锁已被 Redisson 自动释放]", lockKey, LOCK_LEASE_MS);
            }
        }
    }

}
