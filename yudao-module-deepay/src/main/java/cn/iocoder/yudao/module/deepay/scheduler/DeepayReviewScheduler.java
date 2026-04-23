package cn.iocoder.yudao.module.deepay.scheduler;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.InventoryAgent;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import cn.iocoder.yudao.module.deepay.orchestrator.ProductionOrchestrator;
import cn.iocoder.yudao.module.deepay.service.DeepayAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 自动复盘调度器（Phase 4 升级版）— ROI 驱动决策。
 *
 * <p>每小时扫描所有在售商品，核心决策逻辑从"看销量"升级为"看 ROI"：
 * <pre>
 *   ROI &gt; BOOST_ROI_THRESHOLD  → BOOST  （盈利强，ROI 驱动补货）
 *   ROI &lt; STOP_ROI_THRESHOLD   → STOP   （亏钱，下架止损）
 *   其他                         → REDESIGN（换款）
 * </pre>
 * 无 ROI 数据时降级为原始销量逻辑（兼容首批无成本数据商品）。
 * 每次决策结果写入 deepay_metrics（可追溯）。
 * </p>
 */
@Component
public class DeepayReviewScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeepayReviewScheduler.class);

    // Phase 4 ROI 阈值
    private static final BigDecimal BOOST_ROI_THRESHOLD    = new BigDecimal("0.5");
    private static final BigDecimal STOP_ROI_THRESHOLD     = new BigDecimal("0.1");

    // 销量兜底阈值（无 ROI 时使用）
    private static final int BOOST_THRESHOLD     = 8;
    private static final int STOP_THRESHOLD      = 2;

    @Resource private DeepayProductMapper    deepayProductMapper;
    @Resource private DeepayMetricsMapper    deepayMetricsMapper;
    @Resource private DeepayStyleChainMapper deepayStyleChainMapper;
    @Resource private ProductionOrchestrator productionOrchestrator;
    @Resource private InventoryAgent         inventoryAgent;
    @Resource private DeepayAuditService     auditService;

    /** 每小时整点执行一次复盘。 */
    @Scheduled(cron = "0 0 * * * *")
    public void review() {
        log.info("=== 自动复盘开始（ROI 驱动）===");

        List<DeepayProductDO> sellingProducts = deepayProductMapper.selectBySelling();
        if (sellingProducts.isEmpty()) {
            log.info("自动复盘：当前无在售商品，跳过");
            return;
        }

        for (DeepayProductDO product : sellingProducts) {
            try {
                reviewOne(product);
            } catch (Exception e) {
                log.error("自动复盘：处理异常 chainCode={}", product.getChainCode(), e);
            }
        }

        log.info("=== 自动复盘完成，共处理 {} 件商品 ===", sellingProducts.size());
    }

    // ----------------------------------------------------------------

    private void reviewOne(DeepayProductDO product) {
        String chainCode = product.getChainCode();

        // Phase 4：优先读 ROI 决策；无 ROI 时降级为销量
        BigDecimal roi = null;
        try {
            roi = deepayMetricsMapper.selectLatestRoiByChainCode(chainCode);
        } catch (Exception e) {
            log.warn("复盘：查询 ROI 失败 chainCode={}", chainCode, e);
        }

        String action;
        String reason;

        if (roi != null) {
            // Phase 4 ROI 决策
            if (roi.compareTo(BOOST_ROI_THRESHOLD) > 0) {
                action = "BOOST";
                reason = "ROI=" + roi + " > " + BOOST_ROI_THRESHOLD + "，盈利强，ROI 驱动补货";
                doBoost(product, roi);
            } else if (roi.compareTo(STOP_ROI_THRESHOLD) <= 0) {
                action = "STOP";
                reason = "ROI=" + roi + " ≤ " + STOP_ROI_THRESHOLD + "，亏钱，执行下架止损";
                doStop(product);
            } else {
                action = "REDESIGN";
                reason = "ROI=" + roi + "，盈利不足，触发改款重新生产";
                doRedesign(product);
            }
        } else {
            // 降级：无 ROI 数据时用销量兜底
            int soldCount = product.getSoldCount() != null ? product.getSoldCount() : 0;
            if (soldCount >= BOOST_THRESHOLD) {
                action = "BOOST";
                reason = "soldCount=" + soldCount + "（无 ROI 数据，销量兜底）";
                doBoost(product, null);
            } else if (soldCount <= STOP_THRESHOLD) {
                action = "STOP";
                reason = "soldCount=" + soldCount + "（无 ROI 数据，销量兜底下架）";
                doStop(product);
            } else {
                action = "REDESIGN";
                reason = "soldCount=" + soldCount + "（无 ROI 数据，销量兜底改款）";
                doRedesign(product);
            }
        }

        // 写入复盘快照（可追溯）
        DeepayMetricsDO snapshot = new DeepayMetricsDO();
        snapshot.setChainCode(chainCode);
        snapshot.setSoldCount(product.getSoldCount() != null ? product.getSoldCount() : 0);
        snapshot.setPrice(product.getPrice());
        snapshot.setCostPrice(product.getCostPrice());
        snapshot.setRoi(roi);
        snapshot.setCategory("REVIEW:" + action + " | " + reason);
        snapshot.setCreatedAt(LocalDateTime.now());
        deepayMetricsMapper.insert(snapshot);

        auditService.log(chainCode, action, "status=SELLING roi=" + roi, reason);
        log.info("[复盘] chainCode={} action={} reason={}", chainCode, action, reason);
    }

    /** BOOST — Phase 4 ROI 驱动补货，仅在盈利充分时补库存 */
    private void doBoost(DeepayProductDO product, BigDecimal roi) {
        inventoryAgent.restockIfProfitable(product.getChainCode(), roi);
        // 状态机守卫：SELLING → SELLING（保持，return value ignored）
        deepayProductMapper.updateStatusGuarded(product.getId(), "SELLING", "SELLING");
    }

    /** STOP — 状态机守卫：只允许从 SELLING 下架 */
    private void doStop(DeepayProductDO product) {
        int rows = deepayProductMapper.updateStatusGuarded(product.getId(), "STOPPED", "SELLING");
        if (rows == 0) {
            log.warn("[STOP] 状态机拒绝：商品已不在 SELLING 状态 chainCode={}", product.getChainCode());
            return;
        }
        log.info("[STOP] 商品已下架 chainCode={}", product.getChainCode());
    }

    /** REDESIGN — 状态机守卫：SELLING → REDESIGNING，触发全新生产流水线 */
    private void doRedesign(DeepayProductDO product) {
        int rows = deepayProductMapper.updateStatusGuarded(
                product.getId(), "REDESIGNING", "SELLING");
        if (rows == 0) {
            log.warn("[REDESIGN] 状态机拒绝：商品已不在 SELLING 状态 chainCode={}", product.getChainCode());
            return;
        }

        String keyword = null;
        if (product.getChainCode() != null) {
            DeepayStyleChainDO chain = deepayStyleChainMapper.selectByChainCode(product.getChainCode());
            if (chain != null && chain.getKeyword() != null) {
                keyword = chain.getKeyword();
            }
        }
        if (keyword == null) {
            keyword = product.getTitle() != null ? product.getTitle() : "新款";
        }

        log.info("[REDESIGN] 触发重新生产 chainCode={} keyword={}", product.getChainCode(), keyword);
        Context ctx = new Context();
        ctx.keyword = keyword;
        productionOrchestrator.run(ctx);
        log.info("[REDESIGN] 新品已完成 新chainCode={}", ctx.chainCode);
    }

}
