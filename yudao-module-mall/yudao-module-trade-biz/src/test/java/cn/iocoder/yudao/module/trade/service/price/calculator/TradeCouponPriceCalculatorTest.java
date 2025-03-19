package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.coupon.dto.CouponRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Duration;
import java.util.ArrayList;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeCouponPriceCalculator} 的单元测试类
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续修复
public class TradeCouponPriceCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeCouponPriceCalculator tradeCouponPriceCalculator;

    @Mock
    private CouponApi couponApi;

    @Test
    public void testCalculate() {
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(233L).setCouponId(1024L)
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 匹配优惠劵
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true), // 匹配优惠劵
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(4).setSelected(true), // 不匹配优惠劵
                        new TradePriceCalculateReqBO.Item().setSkuId(40L).setCount(5).setSelected(false) // 匹配优惠劵，但是未选中
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
                                .setPrice(30).setSpuId(3L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(40L).setCount(5).setSelected(false)
                                .setPrice(60).setSpuId(1L)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（优惠劵 Coupon 信息）
        CouponRespDTO coupon = randomPojo(CouponRespDTO.class, o -> o.setId(1024L).setName("程序员节")
                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductScopeValues(asList(1L, 2L))
                .setUsePrice(350).setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType())
                .setDiscountPercent(50).setDiscountLimitPrice(70))
                .setValidStartTime(addTime(Duration.ofDays(1))).setValidEndTime(addTime(Duration.ofDays(2)));
        when(couponApi.getCouponListByUserId(eq(233L), eq(CouponStatusEnum.UNUSED.getStatus())))
                .thenReturn(ListUtil.toList(coupon));

        // 调用
        tradeCouponPriceCalculator.calculate(param, result);
        // 断言
        assertEquals(result.getCouponId(), 1024L);
        // 断言：Price 部分
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 470);
        assertEquals(price.getDiscountPrice(), 0);
        assertEquals(price.getPointPrice(), 0);
        assertEquals(price.getDeliveryPrice(), 0);
        assertEquals(price.getCouponPrice(), 70);
        assertEquals(price.getPayPrice(), 400);
        // 断言：SKU 1
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 0);
        assertEquals(orderItem01.getDeliveryPrice(), 0);
        assertEquals(orderItem01.getCouponPrice(), 40);
        assertEquals(orderItem01.getPointPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 160);
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 0);
        assertEquals(orderItem02.getDeliveryPrice(), 0);
        assertEquals(orderItem02.getCouponPrice(), 30);
        assertEquals(orderItem02.getPointPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 120);
        // 断言：SKU 3
        TradePriceCalculateRespBO.OrderItem orderItem03 = result.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 4);
        assertEquals(orderItem03.getPrice(), 30);
        assertEquals(orderItem03.getDiscountPrice(), 0);
        assertEquals(orderItem03.getCouponPrice(), 0);
        assertEquals(orderItem03.getPointPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 120);
        // 断言：SKU 4
        TradePriceCalculateRespBO.OrderItem orderItem04 = result.getItems().get(3);
        assertEquals(orderItem04.getSkuId(), 40L);
        assertEquals(orderItem04.getCount(), 5);
        assertEquals(orderItem04.getPrice(), 60);
        assertEquals(orderItem04.getDiscountPrice(), 0);
        assertEquals(orderItem04.getCouponPrice(), 0);
        assertEquals(orderItem04.getPointPrice(), 0);
        assertEquals(orderItem04.getPayPrice(), 300);
        // 断言：Promotion 部分
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion01 = result.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1024L);
        assertEquals(promotion01.getName(), "程序员节");
        assertEquals(promotion01.getType(), PromotionTypeEnum.COUPON.getType());
        assertEquals(promotion01.getTotalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 70);
        assertTrue(promotion01.getMatch());
        assertEquals(promotion01.getDescription(), "优惠劵：省 0.70 元");
        assertEquals(promotion01.getItems().size(), 2);
        TradePriceCalculateRespBO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getTotalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 40);
        TradePriceCalculateRespBO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getTotalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 30);
    }

}
