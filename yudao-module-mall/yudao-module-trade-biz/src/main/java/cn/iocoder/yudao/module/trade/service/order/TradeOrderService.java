package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface TradeOrderService {

    /**
     * 创建交易订单
     * @param loginUserId 登录用户
     * @param clientIp 用户ip地址
     * @param createReqVO 创建交易订单请求模型
     * @return 交易订单创建结果
     */
    Long createTradeOrder(Long loginUserId, String clientIp, AppTradeOrderCreateReqVO createReqVO);
}
