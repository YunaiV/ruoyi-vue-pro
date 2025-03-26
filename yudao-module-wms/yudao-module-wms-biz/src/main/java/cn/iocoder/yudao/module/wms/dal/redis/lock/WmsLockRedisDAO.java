package cn.iocoder.yudao.module.wms.dal.redis.lock;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;


/**
 * WMS 分布式锁的 Redis DAO
 *
 * @author LeeFJ
 */
@Repository
@Slf4j
public class WmsLockRedisDAO {


    private static final String WMS_STOCK_WAREHOUSE_LOCK = "wms:stock-warehouse:lock:%d:%d";
    private static final String WMS_STOCK_BIN_LOCK = "wms:stock-bin:lock:%d:%d";
    private static final String WMS_STOCK_OWNER_LOCK = "wms:stock-owner:lock:%d:%d";
    private static final String WMS_STOCK_FLOW_LOCK = "wms:stock-flow:lock:%d:%d:%d";

    @Resource
    private RedissonClient redissonClient;



    public void lockFlow(Long warehouseId, Integer stockType, Long stockId,Runnable runnable) {
        String key = formatKey(WMS_STOCK_FLOW_LOCK,warehouseId,Long.valueOf(stockType),stockId);
        lock(key,runnable,8,8);
    }

    /**
     * 仓库库存锁
     **/
    public void lockStockLevels(Long warehouseId, Long productId, Runnable runnable) {
        String warehouseLockKey = formatKey(WMS_STOCK_WAREHOUSE_LOCK,warehouseId,productId);
        String binLockKey = formatKey(WMS_STOCK_BIN_LOCK,warehouseId,productId);
        String ownerLockKey = formatKey(WMS_STOCK_OWNER_LOCK,warehouseId,productId);

        // 给仓指定仓库与产品库存加锁
        lock(warehouseLockKey,()->{
            // 给指定仓库的仓位分项的产品加锁
            lock(binLockKey,()->{
                // 给指定仓库的所有者分项的产品加锁
                lock(ownerLockKey, runnable,8,8);
            },8,8);
        },8,8);
    }





    private void lock(String lockKey, Runnable runnable, int waitSeconds, int leaseSeconds) {

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(waitSeconds, leaseSeconds, java.util.concurrent.TimeUnit.SECONDS);
            // 执行逻辑
            if(isLocked) {
                runnable.run();
            }
        } catch (InterruptedException e) {
            log.error("锁已被中断",e);
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
