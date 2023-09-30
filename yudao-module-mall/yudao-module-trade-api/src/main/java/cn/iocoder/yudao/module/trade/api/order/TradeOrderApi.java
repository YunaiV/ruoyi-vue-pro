package cn.iocoder.yudao.module.trade.api.order;

/**
 * 订单 API 接口
 *
 * @author HUIHUI
 */
public interface TradeOrderApi {

    /**
     * 获取订单状态
     *
     * @param id 订单编号
     * @return 订单状态
     */
    Integer getOrderStatus(Long id);

}
