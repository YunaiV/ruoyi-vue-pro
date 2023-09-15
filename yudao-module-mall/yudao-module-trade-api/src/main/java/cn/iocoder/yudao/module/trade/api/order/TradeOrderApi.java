package cn.iocoder.yudao.module.trade.api.order;

import java.util.Collection;

/**
 * 订单 API 接口
 *
 * @author HUIHUI
 */
public interface TradeOrderApi {

    /**
     * 验证订单
     *
     * @param userId      用户 id
     * @param orderItemId 订单项 id
     * @return 校验通过返回订单 id
     */
    Long validateOrder(Long userId, Long orderItemId);

    /**
     * 获取订单项商品购买数量总和
     *
     * @param orderIds 订单编号
     * @param skuIds   sku 编号
     * @return 订单项商品购买数量总和
     */
    Integer getOrderItemCountSumByOrderIdAndSkuId(Collection<Long> orderIds, Collection<Long> skuIds);

}
