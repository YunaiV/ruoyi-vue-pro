package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 拼团订单 handler 接口实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeCombinationHandler implements TradeOrderHandler {

    @Resource
    private CombinationRecordApi combinationRecordApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 如果不是拼团订单则结束
        if (TradeOrderTypeEnum.isCombination(order.getType())) {
            return;
        }
        Assert.isTrue(orderItems.size() == 1, "拼团时，只允许选择一个商品");

        // 校验是否满足拼团活动相关限制
        TradeOrderItemDO item = orderItems.get(0);
        combinationRecordApi.validateCombinationRecord(order.getUserId(), order.getCombinationActivityId(),
                order.getCombinationHeadId(), item.getSkuId(), item.getCount());
        // TODO @puhui999：这里还要限制下，是不是已经 createOrder；就是还没支付的时候，重复下单了；需要校验下；不然的话，一个拼团可以下多个单子了；
    }

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 如果不是拼团订单则结束
        if (TradeOrderTypeEnum.isCombination(order.getType())) {
            return;
        }
        Assert.isTrue(orderItems.size() == 1, "拼团时，只允许选择一个商品");

        // 获取商品信息
        TradeOrderItemDO item = orderItems.get(0);
        // 创建拼团记录
        combinationRecordApi.createCombinationRecord(TradeOrderConvert.INSTANCE.convert(order, item));
    }

    @Override
    public void cancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (TradeOrderTypeEnum.isCombination(order.getType())) {
            return;
        }
    }

}
