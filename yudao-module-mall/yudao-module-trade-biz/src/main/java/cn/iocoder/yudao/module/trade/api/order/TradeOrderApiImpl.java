package cn.iocoder.yudao.module.trade.api.order;

import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderSummaryRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;

/**
 * 订单 API 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class TradeOrderApiImpl implements TradeOrderApi {

    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @Override
    public Integer getOrderStatus(Long id) {
        TradeOrderDO order = tradeOrderQueryService.getOrder(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order.getStatus();
    }

    @Override
    public TradeOrderSummaryRespDTO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderQueryService.getOrderSummary(beginTime, endTime);
    }

}
