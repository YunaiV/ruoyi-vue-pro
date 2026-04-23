package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayInventoryDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayInventoryMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * InventoryAgent — 库存管理（Phase 3 + Phase 4 升级版）。
 *
 * <p>职责：
 * <ul>
 *   <li><b>初始化</b>：新商品上架时 stock=10，lockedStock=0，写入 deepay_inventory，同步 deepay_product.stock。</li>
 *   <li><b>支付扣减</b>（{@link #onPaid}）：Redisson 分布式锁 + 原子 SQL，永不负库存。</li>
 *   <li><b>退款回滚</b>（{@link #refund}）：库存 +1，sold_count -1，幂等。</li>
 *   <li><b>ROI 驱动补货</b>（{@link #restockIfProfitable}）：仅在 ROI &gt; 0.4 且库存不足时补货。</li>
 *   <li><b>低库存信号</b>：stock &lt; LOW_STOCK 时输出告警。</li>
 * </ul>
 * </p>
 */
@Component
public class InventoryAgent implements Agent {

    private static final Logger log        = LoggerFactory.getLogger(InventoryAgent.class);
    private static final int    INIT_STOCK = 10;
    private static final int    LOW_STOCK  = 3;
    private static final int    RESTOCK_DELTA = 10;

    /** Phase 4 ROI 补货阈值：ROI 高于此值才值得补货 */
    private static final BigDecimal ROI_RESTOCK_THRESHOLD = new BigDecimal("0.4");

    private static final String LOCK_PREFIX = "deepay:inventory:lock:";
    private static final long   LOCK_WAIT_SECONDS    = 3L;
    private static final long   LOCK_EXPIRE_SECONDS  = 10L;

    @Resource private DeepayInventoryMapper deepayInventoryMapper;
    @Resource private DeepayProductMapper   deepayProductMapper;
    @Resource private RedissonClient        redissonClient;

    /** 新品上架：初始化库存（Orchestrator 调用）。 */
    @Override
    public Context run(Context ctx) {
        ctx.stock       = INIT_STOCK;
        ctx.lockedStock = 0;

        DeepayInventoryDO inv = new DeepayInventoryDO();
        inv.setChainCode(ctx.chainCode);
        inv.setStock(ctx.stock);
        inv.setLockedStock(ctx.lockedStock);
        inv.setCreatedAt(LocalDateTime.now());
        deepayInventoryMapper.insert(inv);

        if (ctx.productId != null) {
            deepayProductMapper.addStock(Long.parseLong(ctx.productId), ctx.stock);
        }

        log.info("InventoryAgent: 库存初始化，chainCode={} stock={}", ctx.chainCode, ctx.stock);
        return ctx;
    }

    /**
     * 支付成功后原子扣减库存并同步 soldCount。
     *
     * <p>使用 Redisson 分布式锁防止多实例并发导致库存不一致；
     * 锁内使用原子 SQL（WHERE stock &gt; 0）彻底杜绝负库存。</p>
     *
     * @param chainCode 链码
     * @return 扣减后剩余库存（来自 deepay_inventory 最新值）
     * @throws RuntimeException 商品/库存不存在、锁获取超时、库存不足时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public int onPaid(String chainCode) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + chainCode);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("获取库存锁超时，请稍后重试 chainCode=" + chainCode);
            }
            return doDecrementStock(chainCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("库存扣减被中断 chainCode=" + chainCode, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 退款回滚库存（库存 +1，sold_count -1）。
     *
     * <p>幂等保证：sold_count WHERE sold_count &gt; 0，防止重复退款。</p>
     *
     * @param chainCode 链码
     */
    @Transactional(rollbackFor = Exception.class)
    public void refund(String chainCode) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + chainCode);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("获取库存锁超时，退款回滚失败 chainCode=" + chainCode);
            }

            // 库存回滚
            deepayInventoryMapper.update(null, new LambdaUpdateWrapper<DeepayInventoryDO>()
                    .eq(DeepayInventoryDO::getChainCode, chainCode)
                    .setSql("stock = stock + 1"));

            // sold_count 回滚（幂等：sold_count > 0 才减）
            DeepayProductDO product = deepayProductMapper.selectByChainCode(chainCode);
            if (product != null && product.getSoldCount() != null && product.getSoldCount() > 0) {
                deepayProductMapper.update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                        .eq(DeepayProductDO::getChainCode, chainCode)
                        .gt(DeepayProductDO::getSoldCount, 0)
                        .setSql("sold_count = sold_count - 1, stock = stock + 1"));
            }

            log.info("InventoryAgent.refund: 退款库存已回滚 chainCode={}", chainCode);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("退款库存回滚被中断 chainCode=" + chainCode, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Phase 4 — ROI 驱动补货。
     *
     * <p>仅当 {@code roi > ROI_RESTOCK_THRESHOLD && stock < LOW_STOCK} 时才补货，
     * 避免爆卖但亏钱的场景越补越亏。</p>
     *
     * @param chainCode  链码
     * @param currentRoi 当前最新 ROI（来自 deepay_metrics）
     */
    public void restockIfProfitable(String chainCode, BigDecimal currentRoi) {
        if (currentRoi == null || currentRoi.compareTo(ROI_RESTOCK_THRESHOLD) <= 0) {
            log.info("InventoryAgent.restock: ROI={} 低于阈值{}，跳过补货 chainCode={}",
                    currentRoi, ROI_RESTOCK_THRESHOLD, chainCode);
            return;
        }

        DeepayInventoryDO inv = deepayInventoryMapper.selectByChainCode(chainCode);
        if (inv == null || inv.getStock() >= LOW_STOCK) {
            return;
        }

        deepayInventoryMapper.update(null, new LambdaUpdateWrapper<DeepayInventoryDO>()
                .eq(DeepayInventoryDO::getChainCode, chainCode)
                .setSql("stock = stock + " + RESTOCK_DELTA));

        DeepayProductDO product = deepayProductMapper.selectByChainCode(chainCode);
        if (product != null) {
            deepayProductMapper.addStock(product.getId(), RESTOCK_DELTA);
        }

        log.info("InventoryAgent.restock: ROI={} 超过阈值，补货 +{} chainCode={}",
                currentRoi, RESTOCK_DELTA, chainCode);
    }

    // ---------------------------------------------------------------- private

    /** 锁内执行原子库存扣减（不可重入公共逻辑）。 */
    private int doDecrementStock(String chainCode) {
        DeepayProductDO product = deepayProductMapper.selectByChainCode(chainCode);
        if (product == null) {
            throw new RuntimeException("product not found: " + chainCode);
        }

        // ① 原子扣减 deepay_inventory（WHERE stock > 0，永不负库存）
        int invRows = deepayInventoryMapper.decrementStockAtomic(chainCode);
        if (invRows == 0) {
            throw new RuntimeException("库存不足，无法完成支付 chainCode=" + chainCode);
        }

        // ② 原子扣减 deepay_product（WHERE stock > 0，同步 sold_count++）
        int prodRows = deepayProductMapper.incrementSoldCount(product.getId());
        if (prodRows == 0) {
            throw new RuntimeException("商品库存不足，无法完成支付 chainCode=" + chainCode);
        }

        // ③ 查询最新剩余库存
        DeepayInventoryDO inv = deepayInventoryMapper.selectByChainCode(chainCode);
        int remaining = inv != null ? inv.getStock() : 0;

        if (remaining < LOW_STOCK) {
            log.warn("InventoryAgent: 库存告警 chainCode={} stock={}，建议补货", chainCode, remaining);
        }

        log.info("InventoryAgent.onPaid: chainCode={} 剩余库存={}", chainCode, remaining);
        return remaining;
    }

}

