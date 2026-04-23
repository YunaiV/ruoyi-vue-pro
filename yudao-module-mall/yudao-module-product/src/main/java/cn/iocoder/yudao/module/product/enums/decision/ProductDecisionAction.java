package cn.iocoder.yudao.module.product.enums.decision;

/**
 * 商品决策动作枚举
 *
 * BOOST   - 爆款：继续放大销售，库存 +10
 * STOP    - 滞销：下架商品
 * REDESIGN- 可优化：重新改版
 */
public enum ProductDecisionAction {

    BOOST,
    STOP,
    REDESIGN

}
