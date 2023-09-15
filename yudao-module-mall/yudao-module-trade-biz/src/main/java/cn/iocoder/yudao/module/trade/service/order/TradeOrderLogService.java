package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.service.order.bo.logger.TradeOrderLogCreateReqBO;
import org.springframework.scheduling.annotation.Async;

/**
 * 交易下单日志 Service 接口
 *
 * @author 陈賝
 * @since 2023/7/6 15:44
 */
public interface TradeOrderLogService {

    /**
     * 创建交易下单日志
     *
     * @param logDTO 日志记录
     * @author 陈賝
     * @since 2023/7/6 15:45
     */
    @Async
    void createOrderLog(TradeOrderLogCreateReqBO logDTO);

}
