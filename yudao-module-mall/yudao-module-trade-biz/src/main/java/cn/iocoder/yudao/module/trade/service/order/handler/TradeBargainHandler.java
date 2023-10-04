package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainActivityApi;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.BARGAIN.getType(), order.getType())) {
            return;
        }
        // 扣减砍价活动的库存
        bargainActivityApi.updateBargainActivityStock(order.getBargainActivityId(),
                orderItems.get(0).getCount());
    }

}
