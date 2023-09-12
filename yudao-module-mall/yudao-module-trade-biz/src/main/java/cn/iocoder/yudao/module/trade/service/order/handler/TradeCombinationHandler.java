package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationApi;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterOrderCreateReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeBeforeOrderCreateReqBO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 拼团订单 handler 接口实现类
 *
 * @author HUIHUI
 */
@Component
public class TradeCombinationHandler implements TradeOrderHandler {

    @Resource
    private CombinationApi combinationApi;
    @Resource
    private CombinationRecordApi combinationRecordApi;

    @Override
    public void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO) {
        // 如果是拼团订单；
        if (ObjectUtil.notEqual(TradeOrderTypeEnum.COMBINATION.getType(), reqBO.getOrderType())) {
            return;
        }

        // 校验是否满足拼团活动相关限制
        combinationApi.validateCombination(TradeOrderConvert.INSTANCE.convert1(reqBO));
    }

    @Override
    public void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO) {
        // 创建砍价记录
        combinationRecordApi.createCombinationRecord(TradeOrderConvert.INSTANCE.convert(reqBO));
    }

    @Override
    public void rollbackStock() {

    }

}
