package cn.iocoder.yudao.module.product.service.decision;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateStatusReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.decision.ProductDecisionAction;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 商品决策引擎
 *
 * 规则（第一版，写死）：
 * <ul>
 *   <li>BOOST   : salesCount >= 3                                  → 库存 +10，保持上架</li>
 *   <li>STOP    : salesCount == 0 且创建时间超过 7 天               → 商品下架</li>
 *   <li>REDESIGN: salesCount > 0 且 salesCount < 3                 → 触发改款（占位日志）</li>
 * </ul>
 */
@Slf4j
@Service
public class ProductDecisionEngine {

    /**
     * BOOST 动作每次增加的库存量
     */
    private static final int BOOST_STOCK_INCR = 10;

    /**
     * 判定滞销的天数阈值
     */
    private static final int STOP_DAYS_THRESHOLD = 7;

    @Resource
    private ProductSpuService productSpuService;

    /**
     * 对单个商品 SPU 执行决策：判断动作并落地执行
     *
     * @param spu 商品 SPU
     */
    public void decide(ProductSpuDO spu) {
        ProductDecisionAction action = evaluate(spu);
        log.info("[decide] spuId={} name={} salesCount={} action={}", spu.getId(), spu.getName(), spu.getSalesCount(), action);
        execute(spu, action);
    }

    /**
     * 根据规则判断商品应执行的动作
     */
    private ProductDecisionAction evaluate(ProductSpuDO spu) {
        int salesCount = spu.getSalesCount() != null ? spu.getSalesCount() : 0;

        // 1. 爆款：销量 >= 3
        if (salesCount >= 3) {
            return ProductDecisionAction.BOOST;
        }

        // 2. 滞销：7 天内销量为 0
        if (salesCount == 0 && isOlderThanDays(spu, STOP_DAYS_THRESHOLD)) {
            return ProductDecisionAction.STOP;
        }

        // 3. 可优化：销量 > 0 且 < 3
        if (salesCount > 0) {
            return ProductDecisionAction.REDESIGN;
        }

        // 新品（创建 < 7 天，销量 == 0），暂不决策
        return null;
    }

    /**
     * 落地执行决策动作
     */
    private void execute(ProductSpuDO spu, ProductDecisionAction action) {
        if (action == null) {
            return;
        }
        switch (action) {
            case BOOST:
                // 库存 +10，保持上架
                productSpuService.addSpuStock(spu.getId(), BOOST_STOCK_INCR);
                log.info("[execute] BOOST spuId={} stock+{}", spu.getId(), BOOST_STOCK_INCR);
                break;
            case STOP:
                // 商品下架
                ProductSpuUpdateStatusReqVO stopReq = new ProductSpuUpdateStatusReqVO();
                stopReq.setId(spu.getId());
                stopReq.setStatus(ProductSpuStatusEnum.DISABLE.getStatus());
                productSpuService.updateSpuStatus(stopReq);
                log.info("[execute] STOP spuId={} set status=DISABLE", spu.getId());
                break;
            case REDESIGN:
                // 触发改款（DesignAgent 占位，待后续接入）
                log.info("[execute] REDESIGN spuId={} trigger DesignAgent (placeholder)", spu.getId());
                break;
            default:
                break;
        }
    }

    /**
     * 判断商品是否创建超过指定天数
     */
    private boolean isOlderThanDays(ProductSpuDO spu, int days) {
        if (spu.getCreateTime() == null) {
            return false;
        }
        return spu.getCreateTime().isBefore(LocalDateTime.now().minusDays(days));
    }

}
