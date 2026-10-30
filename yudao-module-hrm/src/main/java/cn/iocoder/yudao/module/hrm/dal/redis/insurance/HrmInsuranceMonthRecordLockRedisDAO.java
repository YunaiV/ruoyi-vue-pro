package cn.iocoder.yudao.module.hrm.dal.redis.insurance;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.LOCKED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.dal.redis.RedisKeyConstants.INSURANCE_MONTH_RECORD_MUTATION_LOCK;

/**
 * 月度社保表变更的锁 Redis DAO
 *
 * 首月创建、次月创建、删除共用一把锁，避免没有记录可锁定时并发创建出多个首月社保表。
 * 不设置租期，由 Redisson 看门狗续期；锁在事务提交或回滚后释放，保证下一个获得锁的请求能读到已提交的数据
 *
 * @author 芋道源码
 */
@Repository
@Slf4j
public class HrmInsuranceMonthRecordLockRedisDAO {

    /**
     * 等待获得锁的最长时间，单位：秒
     */
    static final long LOCK_WAIT_SECONDS = 10;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获得锁并执行逻辑，锁在当前事务完成（提交或回滚）后释放
     *
     * 注意：需要在事务中调用；等待超时、或等待时线程被中断，抛出 {@code LOCKED} 业务异常
     *
     * @param supplier 加锁执行的逻辑
     * @return 执行结果
     */
    public <V> V lock(Supplier<V> supplier) {
        // 1. 校验事务：存在实际事务、且事务同步已激活，才能在事务完成后释放锁
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("月度社保表变更锁需要在事务中获得");
        }

        // 2. 获得锁
        RLock lock = redissonClient.getLock(INSURANCE_MONTH_RECORD_MUTATION_LOCK);
        boolean locked;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOCKED);
        }
        if (!locked) {
            throw exception(LOCKED);
        }

        // 3. 事务完成后释放；注册失败时立即释放
        try {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCompletion(int status) {
                    unlock(lock);
                }

            });
        } catch (RuntimeException e) {
            unlock(lock);
            throw e;
        }

        // 4. 执行逻辑；抛出异常时，由事务回滚后释放锁
        return supplier.get();
    }

    /**
     * 获得锁并执行逻辑，见 {@link #lock(Supplier)}
     *
     * @param runnable 加锁执行的逻辑
     */
    public void lock(Runnable runnable) {
        lock(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 释放锁；失败时仅打印日志，不影响事务的结果
     *
     * @param lock 锁
     */
    private static void unlock(RLock lock) {
        try {
            lock.unlock();
        } catch (RuntimeException e) {
            log.error("[unlock][月度社保表变更锁({}) 释放失败]", INSURANCE_MONTH_RECORD_MUTATION_LOCK, e);
        }
    }

}
