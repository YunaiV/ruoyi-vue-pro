package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.api.discount.DiscountActivityApi;
import cn.iocoder.yudao.module.promotion.api.discount.dto.DiscountProductRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeDiscountActivityPriceCalculator} 的单元测试类
 *
 * @author 芋道源码
 */
public class TradeDiscountActivityPriceCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeDiscountActivityPriceCalculator tradeDiscountActivityPriceCalculator;

    @Mock
    private DiscountActivityApi discountActivityApi;

    @Test
    public void testCalculate() {
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 匹配活动，且已选中
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(false) // 匹配活动，但未选中
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(false)
                                .setPrice(50)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（限时折扣活动）
        when(discountActivityApi.getMatchDiscountProductList(eq(asSet(10L, 20L)))).thenReturn(asList(
                randomPojo(DiscountProductRespDTO.class, o -> o.setActivityId(1000L)
                        .setActivityName("活动 1000 号").setSkuId(10L)
                        .setDiscountType(PromotionDiscountTypeEnum.PRICE.getType()).setDiscountPrice(40)),
                randomPojo(DiscountProductRespDTO.class, o -> o.setActivityId(2000L)
                        .setActivityName("活动 2000 号").setSkuId(20L)
                        .setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType()).setDiscountPercent(60))
        ));
        // 10L: 100 * 2 - 40 * 2 = 120
        // 20L：50 * 3 - 50 * 3 * 0.4 = 90

        // 调用
        tradeDiscountActivityPriceCalculator.calculate(param, result);
        // 断言：Price 部分
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 200);
        assertEquals(price.getDiscountPrice(), 80);
        assertEquals(price.getPointPrice(), 0);
        assertEquals(price.getDeliveryPrice(), 0);
        assertEquals(price.getCouponPrice(), 0);
        assertEquals(price.getPayPrice(), 120);
        assertNull(result.getCouponId());
        // 断言：SKU 1
        assertEquals(result.getItems().size(), 2);
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 80);
        assertEquals(orderItem01.getDeliveryPrice(), 0);
        assertEquals(orderItem01.getCouponPrice(), 0);
        assertEquals(orderItem01.getPointPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 120);
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 60);
        assertEquals(orderItem02.getDeliveryPrice(), 0);
        assertEquals(orderItem02.getCouponPrice(), 0);
        assertEquals(orderItem02.getPointPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 90);
        // 断言：Promotion 部分
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion01 = result.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1000L);
        assertEquals(promotion01.getName(), "活动 1000 号");
        assertEquals(promotion01.getType(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType());
        assertEquals(promotion01.getTotalPrice(), 200);
        assertEquals(promotion01.getDiscountPrice(), 80);
        assertTrue(promotion01.getMatch());
        assertEquals(promotion01.getDescription(), "限时折扣：省 0.80 元");
        TradePriceCalculateRespBO.PromotionItem promotionItem01 = promotion01.getItems().get(0);
        assertEquals(promotion01.getItems().size(), 1);
        assertEquals(promotionItem01.getSkuId(), 10L);
        assertEquals(promotionItem01.getTotalPrice(), 200);
        assertEquals(promotionItem01.getDiscountPrice(), 80);
    }

}
