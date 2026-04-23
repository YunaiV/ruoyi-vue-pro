package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayInventoryDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductionPlanDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayInventoryMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductionPlanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * ProductionPlanner — Phase 5 生产调度中枢。
 *
 * <p>核心公式：
 * <pre>
 *   需要生产 = max(0, 预测销量 - 当前库存 - 在途库存)
 * </pre>
 * 举例：预测 200，库存 50，在途 30 → 生产 120。</p>
 *
 * <p>只有当需要生产量 &gt; 0 时才创建生产计划，
 * 避免库存充足时重复生产。</p>
 */
@Component
public class ProductionPlanner implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ProductionPlanner.class);

    @Resource
    private DeepayInventoryMapper inventoryMapper;

    @Resource
    private DeepayProductionPlanMapper productionPlanMapper;

    @Override
    public Context run(Context ctx) {
        if (ctx.predictedDemand == null || ctx.predictedDemand <= 0) {
            log.debug("[ProductionPlanner] 无预测需求，跳过生产计划 chainCode={}", ctx.chainCode);
            return ctx;
        }

        // 查当前库存
        int currentStock = 0;
        DeepayInventoryDO inv = inventoryMapper.selectByChainCode(ctx.chainCode);
        if (inv != null && inv.getStock() != null) {
            currentStock = inv.getStock();
        }

        // 在途库存：查最新 IN_PRODUCTION 计划的量（简化：取最新一条）
        int inTransit = 0;
        DeepayProductionPlanDO latestPlan =
                productionPlanMapper.selectLatestByChainCode(ctx.chainCode);
        if (latestPlan != null && "IN_PRODUCTION".equals(latestPlan.getStatus())
                && latestPlan.getPlannedQuantity() != null) {
            inTransit = latestPlan.getPlannedQuantity();
        }

        // 核心公式
        int needed = ctx.predictedDemand - currentStock - inTransit;
        if (needed <= 0) {
            log.info("[ProductionPlanner] 库存充足，无需生产 chainCode={} predicted={} stock={} inTransit={}",
                    ctx.chainCode, ctx.predictedDemand, currentStock, inTransit);
            ctx.suggestedProductionQty = 0;
            return ctx;
        }

        ctx.suggestedProductionQty = needed;

        // 创建生产计划
        DeepayProductionPlanDO plan = new DeepayProductionPlanDO();
        plan.setChainCode(ctx.chainCode);
        plan.setPlannedQuantity(needed);
        plan.setForecastDemand(ctx.predictedDemand);
        plan.setCurrentStock(currentStock);
        plan.setInTransitStock(inTransit);
        plan.setStatus("PENDING");
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        productionPlanMapper.insert(plan);

        log.info("[ProductionPlanner] 生产计划已创建 chainCode={} planned={} " +
                        "(predicted={} - stock={} - inTransit={})",
                ctx.chainCode, needed, ctx.predictedDemand, currentStock, inTransit);
        return ctx;
    }

}
