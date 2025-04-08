package cn.iocoder.yudao.module.wms.dal.redis.lock;


import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.function.Supplier;


/**
 * WMS 分布式锁的 Redis DAO
 *
 * @author LeeFJ
 */
@Repository
@Slf4j
public class WmsLockRedisDAO {

    private static final String WMS_STOCK_WAREHOUSE_LOCK = "wms:warehouse-product:lock:%d";


    @Resource
    private RedissonClient redissonClient;

    public void lockByWarehouse(Long warehouseId, Runnable runnable) {
        lockByWarehouse(warehouseId, () -> {
            runnable.run();
        });
    }

    public <T> T lockByWarehouse(Long warehouseId, Supplier<T> runnable) {
        String key = formatKey(WMS_STOCK_WAREHOUSE_LOCK, warehouseId);
        if(SpringUtils.isProd()) {
            // 高负载时，如果锁的时长不够，可能导致库存不准确
            return lock(key, runnable, 16, 16);
        } else {
            // 压测时加大锁的时长
            return lock(key, runnable, 600, 600);
        }
    }


    private <T> T lock(String lockKey, Supplier<T> runnable, int waitSeconds, int leaseSeconds) {

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(waitSeconds, leaseSeconds, java.util.concurrent.TimeUnit.SECONDS);
            // 执行逻辑
            if(isLocked) {
                return runnable.get();
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            log.error("锁已被中断",e);
            return null;
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    private static String formatKey(String format,Long... ids) {
        return String.format(format, ids);
    }

}
