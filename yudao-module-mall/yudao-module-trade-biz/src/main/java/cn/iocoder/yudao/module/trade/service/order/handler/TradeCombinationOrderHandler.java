package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateRespDTO;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderUpdateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_CREATE_FAIL_EXIST_UNPAID;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS;

/**
 * 拼团订单的 {@link TradeOrderHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeCombinationOrderHandler implements TradeOrderHandler {

    @Resource
    private TradeOrderUpdateService orderUpdateService;
    @Resource
    private TradeOrderQueryService orderQueryService;

    @Resource
    private CombinationRecordApi combinationRecordApi;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 如果不是拼团订单则结束
        if (!TradeOrderTypeEnum.isCombination(order.getType())) {
            return;
        }
        Assert.isTrue(orderItems.size() == 1, "拼团时，只允许选择一个商品");

        // 1. 校验是否满足拼团活动相关限制
        TradeOrderItemDO item = orderItems.get(0);
        combinationRecordApi.validateCombinationRecord(order.getUserId(), order.getCombinationActivityId(),
                order.getCombinationHeadId(), item.getSkuId(), item.getCount());

        // 2. 校验该用户是否存在未支付的拼团活动订单，避免一个拼团可以下多个单子了
        TradeOrderDO activityOrder = orderQueryService.getOrderByUserIdAndStatusAndCombination(
                order.getUserId(), order.getCombinationActivityId(), TradeOrderStatusEnum.UNPAID.getStatus());
        if (activityOrder != null) {
            throw exception(ORDER_CREATE_FAIL_EXIST_UNPAID);
        }
    }

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 1.如果不是拼团订单则结束
        if (!TradeOrderTypeEnum.isCombination(order.getType())) {
            return;
        }
        Assert.isTrue(orderItems.size() == 1, "拼团时，只允许选择一个商品");

        // 2. 创建拼团记录
        TradeOrderItemDO item = orderItems.get(0);
        CombinationRecordCreateRespDTO combinationRecord = combinationRecordApi.createCombinationRecord(
                TradeOrderConvert.INSTANCE.convert(order, item));

        // 3. 更新拼团相关信息到订单。为什么几个字段都要更新？
        // 原因是：如果创建订单时自己是团长的情况下 combinationHeadId 是为 null 的，设置团长编号这个操作时在订单是否后创建拼团记录时才设置的。
        orderUpdateService.updateOrderCombinationInfo(order.getId(), order.getCombinationActivityId(),
                combinationRecord.getCombinationRecordId(), combinationRecord.getCombinationHeadId());
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

