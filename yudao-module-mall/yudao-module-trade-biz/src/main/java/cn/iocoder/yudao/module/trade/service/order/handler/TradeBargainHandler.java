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

    // TODO @puhui999：先临时写在这里；在价格计算时，如果是秒杀商品，需要校验如下条件：
    // 1. 商品存在、库存充足、单次限购；
    // 2. 活动进行中、时间段符合

    @Override
    public void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO) {
        // 如果是砍价订单
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.BARGAIN.getType(), reqBO.getOrderType())) {
            return;
        }
        // 扣减砍价活动的库存
        bargainActivityApi.updateBargainActivityStock(reqBO.getBargainActivityId(), reqBO.getCount());
    }

    @Override
    public void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO) {

    }

    @Override
    public void rollback() {

    }

}
