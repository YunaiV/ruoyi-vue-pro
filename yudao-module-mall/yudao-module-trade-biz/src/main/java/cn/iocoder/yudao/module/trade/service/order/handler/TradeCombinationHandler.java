package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterOrderCreateReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterPayOrderReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeBeforeOrderCreateReqBO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 拼团订单 handler 接口实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeCombinationHandler extends TradeOrderDefaultHandler {

    @Resource
    private CombinationRecordApi combinationRecordApi;

    @Override
    public void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO) {
        // 如果不是拼团订单则结束
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.COMBINATION.getType(), reqBO.getOrderType())) {
            return;
        }

        // 获取商品信息
        TradeBeforeOrderCreateReqBO.Item item = reqBO.getItems().get(0);
        // 校验是否满足拼团活动相关限制
        combinationRecordApi.validateCombinationRecord(reqBO.getCombinationActivityId(), reqBO.getUserId(), item.getSkuId(), item.getCount());
    }

    @Override
    public void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO) {
        if (reqBO.getCombinationActivityId() == null) {
            return;
        }

        // 创建砍价记录
        combinationRecordApi.createCombinationRecord(TradeOrderConvert.INSTANCE.convert(reqBO));
    }

    @Override
    public void afterPayOrder(TradeAfterPayOrderReqBO reqBO) {
        // 如果不是拼团订单则结束
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.COMBINATION.getType(), reqBO.getOrderType())) {
            return;
        }

        // 更新拼团状态 TODO puhui999：订单支付失败或订单支付过期删除这条拼团记录
        combinationRecordApi.updateRecordStatusToInProgress(reqBO.getUserId(), reqBO.getOrderId(), reqBO.getPayTime());
    }

}
