package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.member.api.point.MemberPointApi;
import cn.iocoder.yudao.module.member.api.point.dto.MemberPointConfigRespDTO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

// TODO @疯狂：这个可以搞个单测；
/**
 * 赠送积分的 {@link TradePriceCalculator} 实现类
 *
 * @author owen
 */
@Component
@Order(TradePriceCalculator.ORDER_POINT_GIVE)
@Slf4j
public class TradePointGiveCalculator implements TradePriceCalculator {

    @Resource
    private MemberPointApi memberPointApi;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 1.1 校验积分功能是否开启
        int givePointPerYuan = Optional.ofNullable(memberPointApi.getConfig())
                .filter(config -> BooleanUtil.isTrue(config.getTradeDeductEnable()))
                .map(MemberPointConfigRespDTO::getTradeGivePoint)
                .orElse(0);
        if (givePointPerYuan <= 0) {
            return;
        }
        // 1.2 校验支付金额
        if (result.getPrice().getPayPrice() <= 0) {
            return;
        }

        // 2.1 计算赠送积分
        int givePoint = MoneyUtils.calculateRatePriceFloor(result.getPrice().getPayPrice(), (double) givePointPerYuan);
        // 2.2 计算分摊的赠送积分
        List<TradePriceCalculateRespBO.OrderItem> orderItems = filterList(result.getItems(), TradePriceCalculateRespBO.OrderItem::getSelected);
        List<Integer> dividePoints = TradePriceCalculatorHelper.dividePrice(orderItems, givePoint);

        // 3.2 更新 SKU 赠送积分
        for (int i = 0; i < orderItems.size(); i++) {
            TradePriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            // 商品可能赠送了积分，所以这里要加上
            orderItem.setGivePoint(orderItem.getGivePoint() + dividePoints.get(i));
            TradePriceCalculatorHelper.recountPayPrice(orderItem); // TODO @疯狂：这个应该不用调用哇？不影响支付金额
        }
        // 3.3 更新订单赠送积分
        TradePriceCalculatorHelper.recountAllGivePoint(result);
    }

}
