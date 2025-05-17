package cn.iocoder.yudao.module.pay.dal.redis.wallet;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.pay.dal.redis.RedisKeyConstants.PAY_WALLET_LOCK;

/**
 * 支付钱包的锁 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class PayWalletLockRedisDAO {

    @Resource
    private RedissonClient redissonClient;

    public <V> V lock(Long id, Long timeoutMillis, Callable<V> callable) throws Exception {
        String lockKey = formatKey(id);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(timeoutMillis, TimeUnit.MILLISECONDS);
            // 执行逻辑
            return callable.call();
        } catch (Exception e) {
            throw e;
        } finally {
            lock.unlock();
        }
    }

    private static String formatKey(Long id) {
        return String.format(PAY_WALLET_LOCK, id);
    }

}
