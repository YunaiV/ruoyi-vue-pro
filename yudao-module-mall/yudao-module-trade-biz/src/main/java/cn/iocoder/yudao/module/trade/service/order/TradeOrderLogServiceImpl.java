package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.service.order.bo.logger.TradeOrderLogCreateReqBO;
import org.springframework.stereotype.Service;

/**
 * 交易下单日志 Service 实现类
 *
 * @author 陈賝
 * @since 2023/7/6 15:44
 */
@Service
public class TradeOrderLogServiceImpl implements TradeOrderLogService {

    @Override
    public void createOrderLog(TradeOrderLogCreateReqBO createReqBO) {
        // TODO 芋艿：存储还没搞
        System.out.println();
    }

}
