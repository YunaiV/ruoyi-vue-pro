package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.promotion.api.point.PointActivityApi;
import cn.iocoder.yudao.module.promotion.api.point.dto.PointValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_POINT_TOTAL_LIMIT_COUNT;

/**
 * 积分商城的 {@link TradePriceCalculator} 实现类
 *
 * @author owen
 */
@Component
@Order(TradePriceCalculator.ORDER_POINT_ACTIVITY)
@Slf4j
public class TradePointActivityPriceCalculator implements TradePriceCalculator {

    @Resource
    private PointActivityApi pointActivityApi;
    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 1.1 判断订单类型是否为积分商城活动
        if (ObjectUtil.notEqual(result.getType(), TradeOrderTypeEnum.POINT.getType())) {
            return;
        }
        // 1.2 初始化积分
        MemberUserRespDTO user = memberUserApi.getUser(param.getUserId());
        result.setTotalPoint(user.getPoint()).setUsePoint(0);

        // 1.3 校验用户积分余额
        if (user.getPoint() == null || user.getPoint() <= 0) {
            return;
        }

        Assert.isTrue(param.getItems().size() == 1, "积分商城兑换商品时，只允许选择一个商品");
        // 2. 校验是否可以参与积分商城活动
        TradePriceCalculateRespBO.OrderItem orderItem = result.getItems().get(0);
        PointValidateJoinRespDTO activity = validateJoinSeckill(
                param.getUserId(), param.getPointActivityId(),
                orderItem.getSkuId(), orderItem.getCount());

        // 3.1 记录优惠明细
        int discountPrice = orderItem.getPayPrice(); // 情况一：单使用积分兑换
        Assert.isTrue(activity.getPoint() >= 1, "积分商城商品兑换积分必须大于 1");
        result.setUsePoint(activity.getPoint() * orderItem.getCount());
        orderItem.setUsePoint(activity.getPoint() * orderItem.getCount());
        if (activity.getPrice() != null && activity.getPrice() > 0) { // 情况二：积分 + 金额
            discountPrice = orderItem.getPayPrice() - activity.getPrice() * orderItem.getCount();
        }
        // 3.2 记录优惠明细
        TradePriceCalculatorHelper.addPromotion(result, orderItem,
                param.getPointActivityId(), "积分商城活动", PromotionTypeEnum.POINT.getType(),
                StrUtil.format("积分商城活动：省 {} 元", TradePriceCalculatorHelper.formatPrice(discountPrice)),
                discountPrice);

        // 3.3 更新 SKU 优惠金额
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
        TradePriceCalculatorHelper.recountPayPrice(orderItem);
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    private PointValidateJoinRespDTO validateJoinSeckill(Long userId, Long activityId, Long skuId, Integer count) {
        // 1. 校验是否可以参与积分商城活动
        PointValidateJoinRespDTO pointValidateJoinRespDTO = pointActivityApi.validateJoinPointActivity(activityId, skuId, count);
        // 2. 校验总限购数量，目前只有 trade 有具体下单的数据，需要交给 trade 价格计算使用
        int activityProductCount = tradeOrderQueryService.getActivityProductCount(userId, activityId);
        if (activityProductCount + count > pointValidateJoinRespDTO.getCount()) {
            throw exception(PRICE_CALCULATE_POINT_TOTAL_LIMIT_COUNT);
        }
        return pointValidateJoinRespDTO;
    }

}
