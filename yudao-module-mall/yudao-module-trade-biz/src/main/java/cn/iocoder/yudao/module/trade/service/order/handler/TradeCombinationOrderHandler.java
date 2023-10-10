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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS;

/**
 * 拼团订单的 {@link TradeOrderHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeCombinationOrderHandler implements TradeOrderHandler {

    @Resource
    private CombinationRecordApi combinationRecordApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 如果不是拼团订单则结束
        if (!TradeOrderTypeEnum.isCombination(order.getType())) {
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
        if (!TradeOrderTypeEnum.isCombination(order.getType())) {
            return;
        }
        Assert.isTrue(orderItems.size() == 1, "拼团时，只允许选择一个商品");

        // 创建拼团记录
        TradeOrderItemDO item = orderItems.get(0);
        combinationRecordApi.createCombinationRecord(TradeOrderConvert.INSTANCE.convert(order, item));
    }

    @Override
    public void beforeDeliveryOrder(TradeOrderDO order) {
        if (!TradeOrderTypeEnum.isCombination(order.getType())) {
            return;
        }
        // 校验订单拼团是否成功
        if (!combinationRecordApi.isCombinationRecordSuccess(order.getUserId(), order.getId())) {
            throw exception(ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS);
        }
    }

}

