package cn.iocoder.yudao.module.pay.dal.redis.order;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.pay.dal.redis.RedisKeyConstants.PAY_ORDER_SUBMIT_LOCK;

/**
 * 支付订单的锁 Redis DAO
 *
 * 用于防止并发提交同一支付订单导致创建多个支付拓展单（重复支付风险）
 *
 * @author 芋道源码
 */
@Repository
public class PayOrderLockRedisDAO {

    @Resource
    private RedissonClient redissonClient;

    public <V> V lock(Long orderId, Long timeoutMillis, Callable<V> callable) throws Exception {
        String lockKey = formatKey(orderId);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(timeoutMillis, TimeUnit.MILLISECONDS);
            return callable.call();
        } finally {
            lock.unlock();
        }
    }

    private static String formatKey(Long orderId) {
        return String.format(PAY_ORDER_SUBMIT_LOCK, orderId);
    }

}
