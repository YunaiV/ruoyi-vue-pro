package cn.iocoder.yudao.module.trade.api.order;

import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderRespDTO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 订单 API 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class TradeOrderApiImpl implements TradeOrderApi {

    @Resource
    private TradeOrderService tradeOrderService;

    @Override
    public TradeOrderRespDTO getOrder(Long id) {
        return TradeOrderConvert.INSTANCE.convert(tradeOrderService.getOrder(id));
    }

}
