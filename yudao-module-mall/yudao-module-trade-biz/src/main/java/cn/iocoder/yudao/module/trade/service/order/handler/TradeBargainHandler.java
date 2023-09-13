package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainActivityApi;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterOrderCreateReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeBeforeOrderCreateReqBO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 砍价订单 handler 实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeBargainHandler implements TradeOrderHandler {

    @Resource
    private BargainActivityApi bargainActivityApi;

    @Override
    public void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO) {
        // 如果是砍价订单
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.BARGAIN.getType(), reqBO.getOrderType())) {
            return;
        }

        // 额外扣减砍价的库存
        bargainActivityApi.updateBargainActivityStock(reqBO.getBargainActivityId(), reqBO.getCount());
    }

    @Override
    public void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO) {

    }

    @Override
    public void rollback() {

    }

}
