package cn.iocoder.yudao.module.pay.dal.redis.order;

import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static cn.iocoder.yudao.module.pay.dal.redis.RedisKeyConstants.PAY_ORDER_LOCK;

/**
 * 支付订单提交的锁 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
public class PayOrderLockRedisDAO {

    @Resource
    private RedissonClient redissonClient;

    public <V> V lock(Long orderId, Long timeoutMillis, Supplier<V> supplier) {
        String lockKey = formatKey(orderId);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(timeoutMillis, TimeUnit.MILLISECONDS);
            // 执行逻辑
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    private static String formatKey(Long orderId) {
        return String.format(PAY_ORDER_LOCK, orderId);
    }

}
