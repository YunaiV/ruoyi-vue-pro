package cn.iocoder.yudao.module.trade.api.order.dto;

import lombok.Data;

/**
 * 订单统计 Response DTO
 *
 * @author owen
 */
@Data
public class TradeOrderSummaryRespDTO {

    /**
     * 创建订单数
     */
    private Long orderCreateCount;
    /**
     * 支付订单商品数
     */
    private Integer orderPayCount;
    /**
     * 总支付金额，单位：分
     */
    private Integer orderPayPrice;

}
