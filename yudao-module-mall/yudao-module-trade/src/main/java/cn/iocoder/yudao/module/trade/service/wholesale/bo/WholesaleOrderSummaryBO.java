package cn.iocoder.yudao.module.trade.service.wholesale.bo;

import lombok.Data;

import java.util.List;

/**
 * 批发订单摘要 BO
 *
 * @author deepay
 */
@Data
public class WholesaleOrderSummaryBO {

    /** 原始总价（分） */
    private Integer totalPrice;
    /** 折扣金额（分） */
    private Integer discountAmount;
    /** 最终价格（分） */
    private Integer finalPrice;
    /** 会员基础折扣（%） */
    private Integer wholesaleDiscountPercent;
    /** 批量额外折扣（%）：采购量 >5000 → 10，>1000 → 5 */
    private Integer bulkBonusPercent;
    /** 订单项明细 */
    private List<OrderItemSummary> items;

    @Data
    public static class OrderItemSummary {
        private Long skuId;
        private Long spuId;
        private String spuName;
        private String picUrl;
        private Integer count;
        /** 原价（分） */
        private Integer originalPrice;
        /** 批发价（分） */
        private Integer wholesalePrice;
        /** 小计（分） */
        private Integer subtotal;
    }

}
