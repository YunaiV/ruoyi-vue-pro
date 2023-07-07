package cn.iocoder.yudao.module.trade.framework.order.core.service;

import cn.iocoder.yudao.module.trade.framework.order.core.dto.TradeOrderLogCreateReqDTO;

/**
 * 交易下单日志 Service 接口
 *
 * @author 陈賝
 * @since 2023/7/6 15:44
 */
public interface OrderLogService {

    /**
     * 创建交易下单日志
     *
     * @param logDTO 日志记录
     * @author 陈賝
     * @since 2023/7/6 15:45
     */
    void createLog(TradeOrderLogCreateReqDTO logDTO);

}
