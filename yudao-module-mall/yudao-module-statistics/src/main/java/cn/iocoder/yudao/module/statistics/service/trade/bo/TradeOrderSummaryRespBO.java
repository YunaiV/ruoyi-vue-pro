package cn.iocoder.yudao.module.statistics.service.trade.bo;

import lombok.Data;

/**
 * 订单统计 Response BO
 *
 * @author owen
 */
@Data
public class TradeOrderSummaryRespBO {

    /**
     * 创建订单数
     */
    private Integer orderCreateCount;
    /**
     * 支付订单商品数
     */
    private Integer orderPayCount;
    /**
     * 总支付金额，单位：分
     */
    private Integer orderPayPrice;

}
