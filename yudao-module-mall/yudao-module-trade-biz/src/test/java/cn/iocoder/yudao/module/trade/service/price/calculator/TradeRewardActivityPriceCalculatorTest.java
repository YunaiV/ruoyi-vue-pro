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

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
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
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 匹配活动 1
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true), // 匹配活动 1
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(4).setSelected(true) // 匹配活动 2
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(30L).setCount(4).setSelected(true)
                                .setPrice(30).setSpuId(3L)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（限时折扣 DiscountActivity 信息）
        when(rewardActivityApi.getMatchRewardActivityList(eq(asSet(1L, 2L, 3L)))).thenReturn(asList(
                randomPojo(RewardActivityMatchRespDTO.class, o -> o.setId(1000L).setName("活动 1000 号")
                        .setProductScopeValues(asList(1L, 2L)).setConditionType(PromotionConditionTypeEnum.PRICE.getType())
                        .setRules(singletonList(new RewardActivityMatchRespDTO.Rule().setLimit(200).setDiscountPrice(70)))),
                randomPojo(RewardActivityMatchRespDTO.class, o -> o.setId(2000L).setName("活动 2000 号")
                        .setProductScopeValues(singletonList(3L)).setConditionType(PromotionConditionTypeEnum.COUNT.getType())
                        .setRules(asList(new RewardActivityMatchRespDTO.Rule().setLimit(1).setDiscountPrice(10),
                                new RewardActivityMatchRespDTO.Rule().setLimit(2).setDiscountPrice(60), // 最大可满足，因为是 4 个
                                new RewardActivityMatchRespDTO.Rule().setLimit(10).setDiscountPrice(100))))
        ));

        // 调用
        tradeRewardActivityPriceCalculator.calculate(param, result);
        // 断言 Order 部分
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 470);
        assertEquals(price.getDiscountPrice(), 130);
        assertEquals(price.getPointPrice(), 0);
        assertEquals(price.getDeliveryPrice(), 0);
        assertEquals(price.getCouponPrice(), 0);
        assertEquals(price.getPayPrice(), 340);
        assertNull(result.getCouponId());
        // 断言：SKU 1
        assertEquals(result.getItems().size(), 3);
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 40);
        assertEquals(orderItem01.getDeliveryPrice(), 0);
        assertEquals(orderItem01.getCouponPrice(), 0);
        assertEquals(orderItem01.getPointPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 160);
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 30);
        assertEquals(orderItem02.getDeliveryPrice(), 0);
        assertEquals(orderItem02.getCouponPrice(), 0);
        assertEquals(orderItem02.getPointPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 120);
        // 断言：SKU 3
        TradePriceCalculateRespBO.OrderItem orderItem03 = result.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 4);
        assertEquals(orderItem03.getPrice(), 30);
        assertEquals(orderItem03.getDiscountPrice(), 60);
        assertEquals(orderItem03.getDeliveryPrice(), 0);
        assertEquals(orderItem03.getCouponPrice(), 0);
        assertEquals(orderItem03.getPointPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 60);
        // 断言：Promotion 部分（第一个）
        assertEquals(result.getPromotions().size(), 2);
        TradePriceCalculateRespBO.Promotion promotion01 = result.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1000L);
        assertEquals(promotion01.getName(), "活动 1000 号");
        assertEquals(promotion01.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertEquals(promotion01.getTotalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 70);
        assertTrue(promotion01.getMatch());
        assertEquals(promotion01.getDescription(), "满减送：省 0.70 元");
        assertEquals(promotion01.getItems().size(), 2);
        TradePriceCalculateRespBO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getTotalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 40);
        TradePriceCalculateRespBO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getTotalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 30);
        // 断言：Promotion 部分（第二个）
        TradePriceCalculateRespBO.Promotion promotion02 = result.getPromotions().get(1);
        assertEquals(promotion02.getId(), 2000L);
        assertEquals(promotion02.getName(), "活动 2000 号");
        assertEquals(promotion02.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertEquals(promotion02.getTotalPrice(), 120);
        assertEquals(promotion02.getDiscountPrice(), 60);
        assertTrue(promotion02.getMatch());
        assertEquals(promotion02.getDescription(), "满减送：省 0.60 元");
        TradePriceCalculateRespBO.PromotionItem promotionItem02 = promotion02.getItems().get(0);
        assertEquals(promotion02.getItems().size(), 1);
        assertEquals(promotionItem02.getSkuId(), 30L);
        assertEquals(promotionItem02.getTotalPrice(), 120);
        assertEquals(promotionItem02.getDiscountPrice(), 60);
    }

    @Test
    public void testCalculate_notMatch() {
        // 准备参数
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
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（限时折扣 DiscountActivity 信息）
        when(rewardActivityApi.getMatchRewardActivityList(eq(asSet(1L, 2L)))).thenReturn(singletonList(
                randomPojo(RewardActivityMatchRespDTO.class, o -> o.setId(1000L).setName("活动 1000 号")
                        .setProductScopeValues(asList(1L, 2L)).setConditionType(PromotionConditionTypeEnum.PRICE.getType())
                        .setRules(singletonList(new RewardActivityMatchRespDTO.Rule().setLimit(351).setDiscountPrice(70))))
        ));

        // 调用
        tradeRewardActivityPriceCalculator.calculate(param, result);
        // 断言 Order 部分
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 350);
        assertEquals(price.getDiscountPrice(), 0);
        assertEquals(price.getPointPrice(), 0);
        assertEquals(price.getDeliveryPrice(), 0);
        assertEquals(price.getCouponPrice(), 0);
        assertEquals(price.getPayPrice(), 350);
        assertNull(result.getCouponId());
        // 断言：SKU 1
        assertEquals(result.getItems().size(), 2);
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 0);
        assertEquals(orderItem01.getDeliveryPrice(), 0);
        assertEquals(orderItem01.getCouponPrice(), 0);
        assertEquals(orderItem01.getPointPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 200);
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 0);
        assertEquals(orderItem02.getDeliveryPrice(), 0);
        assertEquals(orderItem02.getCouponPrice(), 0);
        assertEquals(orderItem02.getPointPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 150);
        // 断言 Promotion 部分
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion01 = result.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1000L);
        assertEquals(promotion01.getName(), "活动 1000 号");
        assertEquals(promotion01.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertEquals(promotion01.getTotalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 0);
        assertFalse(promotion01.getMatch());
        assertEquals(promotion01.getDescription(), "TODO"); // TODO 芋艿：后面再想想
        assertEquals(promotion01.getItems().size(), 2);
        TradePriceCalculateRespBO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getTotalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 0);
        TradePriceCalculateRespBO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getTotalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 0);
    }

}
