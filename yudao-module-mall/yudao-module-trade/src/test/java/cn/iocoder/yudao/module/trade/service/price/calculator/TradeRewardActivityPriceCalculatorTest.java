package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.api.reward.RewardActivityApi;
import cn.iocoder.yudao.module.promotion.api.reward.dto.RewardActivityMatchRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeRewardActivityPriceCalculator} 的单元测试类
 *
 * @author 芋道源码
 */
public class TradeRewardActivityPriceCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeRewardActivityPriceCalculator tradeRewardActivityPriceCalculator;

    @Mock
    private RewardActivityApi rewardActivityApi;

    @Test
    public void testCalculate_match() {
        // 准备参数：SPU 1、SPU 2 命中活动；SPU 3 不在活动范围
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(4).setSelected(true)
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setGiveCouponTemplateCounts(new LinkedHashMap<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L).setGivePoint(0),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L).setGivePoint(0),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(30L).setCount(4).setSelected(true)
                                .setPrice(30).setSpuId(3L).setGivePoint(0)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（满减送活动）
        when(rewardActivityApi.getMatchRewardActivityListBySpuIds(eq(asSet(1L, 2L, 3L)))).thenReturn(singletonList(
                randomPojo(RewardActivityMatchRespDTO.class, o -> o.setId(1000L).setName("活动 1000 号")
                        .setConditionType(PromotionConditionTypeEnum.PRICE.getType())
                        .setSpuIds(asList(1L, 2L))
                        .setRules(asList(
                                new RewardActivityMatchRespDTO.Rule().setLimit(100).setDiscountPrice(20)
                                        .setPoint(50).setFreeDelivery(false),
                                new RewardActivityMatchRespDTO.Rule().setLimit(200).setDiscountPrice(50)
                                        .setPoint(100).setFreeDelivery(true))))));

        // 调用：SKU1+SKU2 总 payPrice=200+150=350, 命中 limit=200 规则
        tradeRewardActivityPriceCalculator.calculate(param, result);

        // 断言：Price 部分（SKU3 仍按原价计入）
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 470);
        assertEquals(price.getDiscountPrice(), 50);
        assertEquals(price.getPayPrice(), 420);
        // 断言：包邮标记
        assertTrue(result.getFreeDelivery());

        // 断言：SKU 1 优惠 50 * 200/350 = 28
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getDiscountPrice(), 28);
        assertEquals(orderItem01.getPayPrice(), 172);
        assertEquals(orderItem01.getGivePoint(), 57); // 100 * 200/350
        // 断言：SKU 2 优惠 50 - 28 = 22
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getDiscountPrice(), 22);
        assertEquals(orderItem02.getPayPrice(), 128);
        assertEquals(orderItem02.getGivePoint(), 43); // 100 - 57
        // 断言：SKU 3 不在活动范围，不参与
        TradePriceCalculateRespBO.OrderItem orderItem03 = result.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getDiscountPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 120);
        assertEquals(orderItem03.getGivePoint(), 0);

        // 断言：Promotion 部分
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion = result.getPromotions().get(0);
        assertEquals(promotion.getId(), 1000L);
        assertEquals(promotion.getName(), "活动 1000 号");
        assertEquals(promotion.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertEquals(promotion.getTotalPrice(), 350);
        assertEquals(promotion.getDiscountPrice(), 50);
        assertTrue(promotion.getMatch());
        assertEquals(promotion.getDescription(), "满减送：省 0.50 元");
        assertEquals(promotion.getItems().size(), 2);
    }

    @Test
    public void testCalculate_notMatch() {
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setItems(singletonList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true)
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setGiveCouponTemplateCounts(new LinkedHashMap<>())
                .setItems(singletonList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L).setGivePoint(0)
                ));
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法：要求 limit=300，订单总额 200 不满足
        when(rewardActivityApi.getMatchRewardActivityListBySpuIds(eq(asSet(1L)))).thenReturn(singletonList(
                randomPojo(RewardActivityMatchRespDTO.class, o -> o.setId(1000L).setName("活动 1000 号")
                        .setConditionType(PromotionConditionTypeEnum.PRICE.getType())
                        .setSpuIds(singletonList(1L))
                        .setRules(singletonList(new RewardActivityMatchRespDTO.Rule()
                                .setLimit(300).setDiscountPrice(70).setDescription("满 3 元减 0.7 元"))))));

        // 调用
        tradeRewardActivityPriceCalculator.calculate(param, result);

        // 断言：未优惠
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 200);
        assertEquals(price.getDiscountPrice(), 0);
        assertEquals(price.getPayPrice(), 200);

        // 断言：促销明细记录为不匹配
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion = result.getPromotions().get(0);
        assertEquals(promotion.getId(), 1000L);
        assertEquals(promotion.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertFalse(promotion.getMatch());
        assertEquals(promotion.getDiscountPrice(), 0);
        assertEquals(promotion.getDescription(), "满减送：满 3 元减 0.7 元");
    }

    @Test
    public void testCalculate_skipNonNormalOrder() {
        // 准备参数：秒杀订单
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setItems(singletonList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true)
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.SECKILL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(singletonList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L)
                ));
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // 调用：非普通订单直接跳过，不会调用 API
        tradeRewardActivityPriceCalculator.calculate(param, result);

        // 断言：未产生促销
        assertEquals(result.getPromotions().size(), 0);
    }

}
