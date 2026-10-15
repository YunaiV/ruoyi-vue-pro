package cn.iocoder.yudao.module.pay.dal.redis.transfer;

import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static cn.iocoder.yudao.module.pay.dal.redis.RedisKeyConstants.PAY_TRANSFER_LOCK;

/**
 * 支付转账的锁 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
public class PayTransferLockRedisDAO {

    @Resource
    private RedissonClient redissonClient;

    public <V> V lock(Long appId, String merchantTransferId, Long timeoutMillis, Supplier<V> supplier) {
        String lockKey = formatKey(appId, merchantTransferId);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(timeoutMillis, TimeUnit.MILLISECONDS);
            // 执行逻辑
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    private static String formatKey(Long appId, String merchantTransferId) {
        return String.format(PAY_TRANSFER_LOCK, appId, merchantTransferId);
    }

}
