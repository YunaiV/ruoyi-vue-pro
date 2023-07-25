package cn.iocoder.yudao.module.trade.api.order;

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

}
