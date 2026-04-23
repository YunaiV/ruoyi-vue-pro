package cn.iocoder.yudao.module.deepay.service.inventory;

import cn.iocoder.yudao.module.deepay.agent.PatternAgent;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayInventoryDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayInventoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Deepay 库存业务服务实现。
 *
 * <p>库存状态规则：
 * <ul>
 *   <li>stock &gt; 2  → NORMAL</li>
 *   <li>0 &lt; stock &le; 2 → LOW</li>
 *   <li>stock == 0 → OUT，并自动触发 {@link PatternAgent} 重新打版/生产</li>
 * </ul>
 * </p>
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    /** 初始库存默认数量 */
    private static final int DEFAULT_INITIAL_STOCK = 10;

    /** 库存预警阈值（stock &le; 此值时状态变为 LOW） */
    private static final int LOW_THRESHOLD = 2;

    @Resource
    private DeepayInventoryMapper inventoryMapper;

    @Resource
    private PatternAgent patternAgent;

    // ------------------------------------------------------------------ init

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initInventory(String chainCode) {
        DeepayInventoryDO record = new DeepayInventoryDO();
        record.setChainCode(chainCode);
        record.setStock(DEFAULT_INITIAL_STOCK);
        record.setLockedStock(0);
        record.setStatus("NORMAL");
        record.setUpdatedAt(LocalDateTime.now());
        inventoryMapper.insert(record);
        log.info("InventoryService: 库存初始化完成，chainCode={}, stock={}", chainCode, DEFAULT_INITIAL_STOCK);
    }

    // --------------------------------------------------------------- lockStock

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(String chainCode) {
        DeepayInventoryDO inv = getExistingInventory(chainCode);
        if (inv.getStock() <= 0) {
            log.warn("InventoryService: 库存不足，无法下单，chainCode={}", chainCode);
            return false;
        }

        inv.setStock(inv.getStock() - 1);
        inv.setLockedStock(inv.getLockedStock() + 1);
        inv.setStatus(resolveStatus(inv.getStock()));
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryMapper.updateById(inv);

        log.info("InventoryService: 下单锁定库存，chainCode={}, stock={}, lockedStock={}",
                chainCode, inv.getStock(), inv.getLockedStock());

        // 库存降至 0 时自动触发生产（此时当前线程是使库存归零的那个请求）
        // NOTE: 高并发场景建议对 chainCode 行加悲观锁（SELECT FOR UPDATE），MVP 阶段通过 @Transactional 保证基本一致性
        if (inv.getStock() == 0) {
            triggerProduction(chainCode);
        }

        return true;
    }

    // ----------------------------------------------------------- confirmPayment

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(String chainCode) {
        DeepayInventoryDO inv = getExistingInventory(chainCode);
        if (inv.getLockedStock() <= 0) {
            log.warn("InventoryService: 支付确认时锁定库存已为 0，可能存在数据不一致，chainCode={}", chainCode);
        }
        int newLocked = Math.max(0, inv.getLockedStock() - 1);
        inv.setLockedStock(newLocked);
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryMapper.updateById(inv);
        log.info("InventoryService: 支付成功，释放锁定库存，chainCode={}, lockedStock={}", chainCode, newLocked);
    }

    // ---------------------------------------------------------------- addStock

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(String chainCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("补货数量必须大于 0");
        }
        DeepayInventoryDO inv = getExistingInventory(chainCode);
        inv.setStock(inv.getStock() + quantity);
        inv.setStatus(resolveStatus(inv.getStock()));
        inv.setUpdatedAt(LocalDateTime.now());
        inventoryMapper.updateById(inv);
        log.info("InventoryService: 手动补货，chainCode={}, 补货={}, 当前stock={}", chainCode, quantity, inv.getStock());
    }

    // ------------------------------------------------------------------ query

    @Override
    public List<DeepayInventoryDO> listAll() {
        return inventoryMapper.selectAll();
    }

    @Override
    public DeepayInventoryDO getByChainCode(String chainCode) {
        return inventoryMapper.selectByChainCode(chainCode);
    }

    // ---------------------------------------------------------------- private

    private DeepayInventoryDO getExistingInventory(String chainCode) {
        DeepayInventoryDO inv = inventoryMapper.selectByChainCode(chainCode);
        if (inv == null) {
            throw new IllegalStateException("库存记录不存在，chainCode=" + chainCode);
        }
        return inv;
    }

    /**
     * 根据当前库存数量计算库存状态。
     *
     * @param stock 当前库存
     * @return 状态字符串
     */
    private String resolveStatus(int stock) {
        if (stock == 0) {
            return "OUT";
        } else if (stock <= LOW_THRESHOLD) {
            return "LOW";
        }
        return "NORMAL";
    }

    /**
     * 库存为 0 时触发 PatternAgent 自动生产。
     *
     * @param chainCode 商品链码
     */
    private void triggerProduction(String chainCode) {
        try {
            log.warn("InventoryService: 库存耗尽，自动触发生产，chainCode={}", chainCode);
            cn.iocoder.yudao.module.deepay.agent.Context ctx =
                    new cn.iocoder.yudao.module.deepay.agent.Context();
            ctx.chainCode = chainCode;
            patternAgent.run(ctx);
        } catch (Exception e) {
            // 生产触发失败不影响主流程
            log.error("InventoryService: 触发生产失败，chainCode={}", chainCode, e);
        }
    }

}
