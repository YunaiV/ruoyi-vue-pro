package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
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
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.COMBINATION.getType(), order.getType())) {
            return;
        }
        Assert.isTrue(orderItems.size() == 1, "拼团时，只允许选择一个商品");

        // 获取商品信息
        TradeOrderItemDO item = orderItems.get(0);
        // 校验是否满足拼团活动相关限制
        combinationRecordApi.validateCombinationRecord(order.getUserId(), order.getCombinationActivityId(),
                order.getCombinationHeadId(), item.getSkuId(), item.getCount());
    }

    @Override
    public void afterOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 如果不是拼团订单则结束
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.COMBINATION.getType(), order.getType())) {
            return;
        }
        Assert.isTrue(orderItems.size() == 1, "拼团时，只允许选择一个商品");

        // 获取商品信息
        TradeOrderItemDO item = orderItems.get(0);
        // 创建拼团记录
        // TODO puhui：这里应该先不创建；等支付好，才去创建；另外，创建好后，需要更新编号到订单；
        combinationRecordApi.createCombinationRecord(TradeOrderConvert.INSTANCE.convert(order, item));
    }

    @Override
    public void afterPayOrder(TradeOrderDO order) {
        // 如果不是拼团订单则结束
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.COMBINATION.getType(), order.getType())) {
            return;
        }

        // 更新拼团状态 TODO puhui999：订单支付失败或订单支付过期删除这条拼团记录
        combinationRecordApi.updateRecordStatusToInProgress(order.getUserId(), order.getId(), order.getPayTime());
    }

}
