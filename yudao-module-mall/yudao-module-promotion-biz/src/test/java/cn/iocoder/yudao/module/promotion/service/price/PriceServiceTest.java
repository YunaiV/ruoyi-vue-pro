package cn.iocoder.yudao.module.promotion.service.price;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.CouponMeetRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.enums.common.*;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import cn.iocoder.yudao.module.promotion.service.discount.DiscountActivityService;
import cn.iocoder.yudao.module.promotion.service.discount.bo.DiscountProductDetailBO;
import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_VALID_TIME_NOT_NOW;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * {@link PriceServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class PriceServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PriceServiceImpl priceService;

    @Mock
    private DiscountActivityService discountService;
    @Mock
    private RewardActivityService rewardActivityService;
    @Mock
    private CouponService couponService;
    @Mock
    private ProductSkuApi productSkuApi;

    @Test
    public void testCalculatePrice_memberDiscount() {
        // 准备参数
        // TODO 芋艿：userId = 1，实现 9 折；后续改成 mock
        PriceCalculateReqDTO calculateReqDTO = new PriceCalculateReqDTO().setUserId(1L)
                .setItems(singletonList(new PriceCalculateReqDTO.Item().setSkuId(10L).setCount(2)));
        // mock 方法(商品 SKU 信息)
        ProductSkuRespDTO productSku = randomPojo(ProductSkuRespDTO.class, o -> o.setId(10L).setPrice(100));
        when(productSkuApi.getSkuList(eq(asSet(10L)))).thenReturn(singletonList(productSku));

        // 调用
        PriceCalculateRespDTO priceCalculate = priceService.calculatePrice(calculateReqDTO);
        // 断言 Order 部分
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        assertEquals(order.getOriginalPrice(), 200);
        assertEquals(order.getOrderPrice(), 180);
        assertEquals(order.getDiscountPrice(), 0);
        assertEquals(order.getPointPrice(), 0);
        assertEquals(order.getDeliveryPrice(), 0);
        assertEquals(order.getPayPrice(), 180);
        assertNull(order.getCouponId());
        // 断言 OrderItem 部分
        assertEquals(order.getItems().size(), 1);
        PriceCalculateRespDTO.OrderItem orderItem = order.getItems().get(0);
        assertEquals(orderItem.getSkuId(), 10L);
        assertEquals(orderItem.getCount(), 2);
        assertEquals(orderItem.getOriginalPrice(), 200);
        assertEquals(orderItem.getOriginalUnitPrice(), 100);
        assertEquals(orderItem.getDiscountPrice(), 20);
        assertEquals(orderItem.getPayPrice(), 180);
        assertEquals(orderItem.getOrderPartPrice(), 0);
        assertEquals(orderItem.getOrderDividePrice(), 180);
        // 断言 Promotion 部分
        assertEquals(priceCalculate.getPromotions().size(), 1);
        PriceCalculateRespDTO.Promotion promotion = priceCalculate.getPromotions().get(0);
        assertNull(promotion.getId());
        assertEquals(promotion.getName(), "会员折扣");
        assertEquals(promotion.getType(), PromotionTypeEnum.MEMBER.getType());
        assertEquals(promotion.getLevel(), PromotionLevelEnum.SKU.getLevel());
        assertEquals(promotion.getOriginalPrice(), 200);
        assertEquals(promotion.getDiscountPrice(), 20);
        assertTrue(promotion.getMeet());
        assertEquals(promotion.getMeetTip(), "会员折扣：省 0.20 元");
        PriceCalculateRespDTO.PromotionItem promotionItem = promotion.getItems().get(0);
        assertEquals(promotion.getItems().size(), 1);
        assertEquals(promotionItem.getSkuId(), 10L);
        assertEquals(promotionItem.getOriginalPrice(), 200);
        assertEquals(promotionItem.getDiscountPrice(), 20);
    }

    @Test
    public void testCalculatePrice_discountActivity() {
        // 准备参数
        PriceCalculateReqDTO calculateReqDTO = new PriceCalculateReqDTO().setUserId(randomLongId())
                .setItems(asList(new PriceCalculateReqDTO.Item().setSkuId(10L).setCount(2),
                        new PriceCalculateReqDTO.Item().setSkuId(20L).setCount(3)));
        // mock 方法(商品 SKU 信息)
        ProductSkuRespDTO productSku01 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(10L).setPrice(100));
        ProductSkuRespDTO productSku02 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(20L).setPrice(50));
        when(productSkuApi.getSkuList(eq(asSet(10L, 20L)))).thenReturn(asList(productSku01, productSku02));
        // mock 方法（限时折扣 DiscountActivity 信息）
        DiscountProductDetailBO discountProduct01 = randomPojo(DiscountProductDetailBO.class, o -> o.setActivityId(1000L)
                .setActivityName("活动 1000 号").setSkuId(10L)
                .setDiscountType(PromotionDiscountTypeEnum.PRICE.getType()).setDiscountPrice(40));
        DiscountProductDetailBO discountProduct02 = randomPojo(DiscountProductDetailBO.class, o -> o.setActivityId(2000L)
                .setActivityName("活动 2000 号").setSkuId(20L)
                .setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType()).setDiscountPercent(60));
        when(discountService.getMatchDiscountProducts(eq(asSet(10L, 20L)))).thenReturn(
                MapUtil.builder(10L, discountProduct01).put(20L, discountProduct02).map());

        // 10L: 100 * 2 - 40 * 2 = 120
        // 20L：50 * 3 - 50 * 3 * 0.4 = 90

        // 调用
        PriceCalculateRespDTO priceCalculate = priceService.calculatePrice(calculateReqDTO);
        // 断言 Order 部分
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        assertEquals(order.getOriginalPrice(), 350);
        assertEquals(order.getOrderPrice(), 210);
        assertEquals(order.getDiscountPrice(), 0);
        assertEquals(order.getPointPrice(), 0);
        assertEquals(order.getDeliveryPrice(), 0);
        assertEquals(order.getPayPrice(), 210);
        assertNull(order.getCouponId());
        // 断言 OrderItem 部分
        assertEquals(order.getItems().size(), 2);
        PriceCalculateRespDTO.OrderItem orderItem01 = order.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getOriginalPrice(), 200);
        assertEquals(orderItem01.getOriginalUnitPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 80);
        assertEquals(orderItem01.getPayPrice(), 120);
        assertEquals(orderItem01.getOrderPartPrice(), 0);
        assertEquals(orderItem01.getOrderDividePrice(), 120);
        PriceCalculateRespDTO.OrderItem orderItem02 = order.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getOriginalPrice(), 150);
        assertEquals(orderItem02.getOriginalUnitPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 60);
        assertEquals(orderItem02.getPayPrice(), 90);
        assertEquals(orderItem02.getOrderPartPrice(), 0);
        assertEquals(orderItem02.getOrderDividePrice(), 90);
        // 断言 Promotion 部分
        assertEquals(priceCalculate.getPromotions().size(), 2);
        PriceCalculateRespDTO.Promotion promotion01 = priceCalculate.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1000L);
        assertEquals(promotion01.getName(), "活动 1000 号");
        assertEquals(promotion01.getType(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType());
        assertEquals(promotion01.getLevel(), PromotionLevelEnum.SKU.getLevel());
        assertEquals(promotion01.getOriginalPrice(), 200);
        assertEquals(promotion01.getDiscountPrice(), 80);
        assertTrue(promotion01.getMeet());
        assertEquals(promotion01.getMeetTip(), "限时折扣：省 0.80 元");
        PriceCalculateRespDTO.PromotionItem promotionItem01 = promotion01.getItems().get(0);
        assertEquals(promotion01.getItems().size(), 1);
        assertEquals(promotionItem01.getSkuId(), 10L);
        assertEquals(promotionItem01.getOriginalPrice(), 200);
        assertEquals(promotionItem01.getDiscountPrice(), 80);
        PriceCalculateRespDTO.Promotion promotion02 = priceCalculate.getPromotions().get(1);
        assertEquals(promotion02.getId(), 2000L);
        assertEquals(promotion02.getName(), "活动 2000 号");
        assertEquals(promotion02.getType(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType());
        assertEquals(promotion02.getLevel(), PromotionLevelEnum.SKU.getLevel());
        assertEquals(promotion02.getOriginalPrice(), 150);
        assertEquals(promotion02.getDiscountPrice(), 60);
        assertTrue(promotion02.getMeet());
        assertEquals(promotion02.getMeetTip(), "限时折扣：省 0.60 元");
        PriceCalculateRespDTO.PromotionItem promotionItem02 = promotion02.getItems().get(0);
        assertEquals(promotion02.getItems().size(), 1);
        assertEquals(promotionItem02.getSkuId(), 20L);
        assertEquals(promotionItem02.getOriginalPrice(), 150);
        assertEquals(promotionItem02.getDiscountPrice(), 60);
    }

    /**
     * 测试满减送活动，匹配的情况
     */
    @Test
    public void testCalculatePrice_rewardActivity() {
        // 准备参数
        PriceCalculateReqDTO calculateReqDTO = new PriceCalculateReqDTO().setUserId(randomLongId())
                .setItems(asList(new PriceCalculateReqDTO.Item().setSkuId(10L).setCount(2),
                        new PriceCalculateReqDTO.Item().setSkuId(20L).setCount(3),
                        new PriceCalculateReqDTO.Item().setSkuId(30L).setCount(4)));
        // mock 方法(商品 SKU 信息)
        ProductSkuRespDTO productSku01 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(10L).setPrice(100).setSpuId(1L));
        ProductSkuRespDTO productSku02 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(20L).setPrice(50).setSpuId(2L));
        ProductSkuRespDTO productSku03 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(30L).setPrice(30).setSpuId(3L));
        when(productSkuApi.getSkuList(eq(asSet(10L, 20L, 30L)))).thenReturn(asList(productSku01, productSku02, productSku03));
        // mock 方法（限时折扣 DiscountActivity 信息）
        RewardActivityDO rewardActivity01 = randomPojo(RewardActivityDO.class, o -> o.setId(1000L).setName("活动 1000 号")
                .setProductSpuIds(asList(10L, 20L)).setConditionType(PromotionConditionTypeEnum.PRICE.getType())
                .setRules(singletonList(new RewardActivityDO.Rule().setLimit(200).setDiscountPrice(70))));
        RewardActivityDO rewardActivity02 = randomPojo(RewardActivityDO.class, o -> o.setId(2000L).setName("活动 2000 号")
                .setProductSpuIds(singletonList(30L)).setConditionType(PromotionConditionTypeEnum.COUNT.getType())
                .setRules(asList(new RewardActivityDO.Rule().setLimit(1).setDiscountPrice(10),
                        new RewardActivityDO.Rule().setLimit(2).setDiscountPrice(60), // 最大可满足，因为是 4 个
                        new RewardActivityDO.Rule().setLimit(10).setDiscountPrice(100))));
        Map<RewardActivityDO, Set<Long>> matchRewardActivities = new LinkedHashMap<>();
        matchRewardActivities.put(rewardActivity01, asSet(1L, 2L));
        matchRewardActivities.put(rewardActivity02, asSet(3L));
        when(rewardActivityService.getMatchRewardActivities(eq(asSet(1L, 2L, 3L)))).thenReturn(matchRewardActivities);

        // 调用
        PriceCalculateRespDTO priceCalculate = priceService.calculatePrice(calculateReqDTO);
        // 断言 Order 部分
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        assertEquals(order.getOriginalPrice(), 470);
        assertEquals(order.getOrderPrice(), 470);
        assertEquals(order.getDiscountPrice(), 130);
        assertEquals(order.getPointPrice(), 0);
        assertEquals(order.getDeliveryPrice(), 0);
        assertEquals(order.getPayPrice(), 340);
        assertNull(order.getCouponId());
        // 断言 OrderItem 部分
        assertEquals(order.getItems().size(), 3);
        PriceCalculateRespDTO.OrderItem orderItem01 = order.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getOriginalPrice(), 200);
        assertEquals(orderItem01.getOriginalUnitPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 200);
        assertEquals(orderItem01.getOrderPartPrice(), 40);
        assertEquals(orderItem01.getOrderDividePrice(), 160);
        PriceCalculateRespDTO.OrderItem orderItem02 = order.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getOriginalPrice(), 150);
        assertEquals(orderItem02.getOriginalUnitPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 150);
        assertEquals(orderItem02.getOrderPartPrice(), 30);
        assertEquals(orderItem02.getOrderDividePrice(), 120);
        PriceCalculateRespDTO.OrderItem orderItem03 = order.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 4);
        assertEquals(orderItem03.getOriginalPrice(), 120);
        assertEquals(orderItem03.getOriginalUnitPrice(), 30);
        assertEquals(orderItem03.getDiscountPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 120);
        assertEquals(orderItem03.getOrderPartPrice(), 60);
        assertEquals(orderItem03.getOrderDividePrice(), 60);
        // 断言 Promotion 部分（第一个）
        assertEquals(priceCalculate.getPromotions().size(), 2);
        PriceCalculateRespDTO.Promotion promotion01 = priceCalculate.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1000L);
        assertEquals(promotion01.getName(), "活动 1000 号");
        assertEquals(promotion01.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertEquals(promotion01.getLevel(), PromotionLevelEnum.ORDER.getLevel());
        assertEquals(promotion01.getOriginalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 70);
        assertTrue(promotion01.getMeet());
        assertEquals(promotion01.getMeetTip(), "满减送：省 0.70 元");
        assertEquals(promotion01.getItems().size(), 2);
        PriceCalculateRespDTO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getOriginalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 40);
        PriceCalculateRespDTO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getOriginalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 30);
        // 断言 Promotion 部分（第二个）
        PriceCalculateRespDTO.Promotion promotion02 = priceCalculate.getPromotions().get(1);
        assertEquals(promotion02.getId(), 2000L);
        assertEquals(promotion02.getName(), "活动 2000 号");
        assertEquals(promotion02.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertEquals(promotion02.getLevel(), PromotionLevelEnum.ORDER.getLevel());
        assertEquals(promotion02.getOriginalPrice(), 120);
        assertEquals(promotion02.getDiscountPrice(), 60);
        assertTrue(promotion02.getMeet());
        assertEquals(promotion02.getMeetTip(), "满减送：省 0.60 元");
        PriceCalculateRespDTO.PromotionItem promotionItem02 = promotion02.getItems().get(0);
        assertEquals(promotion02.getItems().size(), 1);
        assertEquals(promotionItem02.getSkuId(), 30L);
        assertEquals(promotionItem02.getOriginalPrice(), 120);
        assertEquals(promotionItem02.getDiscountPrice(), 60);
    }

    /**
     * 测试满减送活动，不匹配的情况
     */
    @Test
    public void testCalculatePrice_rewardActivityNotMeet() {
        // 准备参数
        PriceCalculateReqDTO calculateReqDTO = new PriceCalculateReqDTO().setUserId(randomLongId())
                .setItems(asList(new PriceCalculateReqDTO.Item().setSkuId(10L).setCount(2),
                        new PriceCalculateReqDTO.Item().setSkuId(20L).setCount(3)));
        // mock 方法(商品 SKU 信息)
        ProductSkuRespDTO productSku01 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(10L).setPrice(100).setSpuId(1L));
        ProductSkuRespDTO productSku02 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(20L).setPrice(50).setSpuId(2L));
        when(productSkuApi.getSkuList(eq(asSet(10L, 20L)))).thenReturn(asList(productSku01, productSku02));
        // mock 方法（限时折扣 DiscountActivity 信息）
        RewardActivityDO rewardActivity01 = randomPojo(RewardActivityDO.class, o -> o.setId(1000L).setName("活动 1000 号")
                .setProductSpuIds(asList(10L, 20L)).setConditionType(PromotionConditionTypeEnum.PRICE.getType())
                .setRules(singletonList(new RewardActivityDO.Rule().setLimit(351).setDiscountPrice(70))));
        Map<RewardActivityDO, Set<Long>> matchRewardActivities = new LinkedHashMap<>();
        matchRewardActivities.put(rewardActivity01, asSet(1L, 2L));
        when(rewardActivityService.getMatchRewardActivities(eq(asSet(1L, 2L)))).thenReturn(matchRewardActivities);

        // 调用
        PriceCalculateRespDTO priceCalculate = priceService.calculatePrice(calculateReqDTO);
        // 断言 Order 部分
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        assertEquals(order.getOriginalPrice(), 350);
        assertEquals(order.getOrderPrice(), 350);
        assertEquals(order.getDiscountPrice(), 0);
        assertEquals(order.getPointPrice(), 0);
        assertEquals(order.getDeliveryPrice(), 0);
        assertEquals(order.getPayPrice(), 350);
        assertNull(order.getCouponId());
        // 断言 OrderItem 部分
        assertEquals(order.getItems().size(), 2);
        PriceCalculateRespDTO.OrderItem orderItem01 = order.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getOriginalPrice(), 200);
        assertEquals(orderItem01.getOriginalUnitPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 200);
        assertEquals(orderItem01.getOrderPartPrice(), 0);
        assertEquals(orderItem01.getOrderDividePrice(), 200);
        PriceCalculateRespDTO.OrderItem orderItem02 = order.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getOriginalPrice(), 150);
        assertEquals(orderItem02.getOriginalUnitPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 150);
        assertEquals(orderItem02.getOrderPartPrice(), 0);
        assertEquals(orderItem02.getOrderDividePrice(), 150);
        // 断言 Promotion 部分
        assertEquals(priceCalculate.getPromotions().size(), 1);
        PriceCalculateRespDTO.Promotion promotion01 = priceCalculate.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1000L);
        assertEquals(promotion01.getName(), "活动 1000 号");
        assertEquals(promotion01.getType(), PromotionTypeEnum.REWARD_ACTIVITY.getType());
        assertEquals(promotion01.getLevel(), PromotionLevelEnum.ORDER.getLevel());
        assertEquals(promotion01.getOriginalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 0);
        assertFalse(promotion01.getMeet());
        assertEquals(promotion01.getMeetTip(), "TODO"); // TODO 芋艿：后面再想想
        assertEquals(promotion01.getItems().size(), 2);
        PriceCalculateRespDTO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getOriginalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 0);
        PriceCalculateRespDTO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getOriginalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 0);
    }

    @Test
    public void testCalculatePrice_coupon() {
        // 准备参数
        PriceCalculateReqDTO calculateReqDTO = new PriceCalculateReqDTO().setUserId(randomLongId())
                .setItems(asList(new PriceCalculateReqDTO.Item().setSkuId(10L).setCount(2),
                        new PriceCalculateReqDTO.Item().setSkuId(20L).setCount(3),
                        new PriceCalculateReqDTO.Item().setSkuId(30L).setCount(4)))
                .setCouponId(1024L);
        // mock 方法(商品 SKU 信息)
        ProductSkuRespDTO productSku01 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(10L).setPrice(100).setSpuId(1L));
        ProductSkuRespDTO productSku02 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(20L).setPrice(50).setSpuId(2L));
        ProductSkuRespDTO productSku03 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(30L).setPrice(30).setSpuId(3L));
        when(productSkuApi.getSkuList(eq(asSet(10L, 20L, 30L)))).thenReturn(asList(productSku01, productSku02, productSku03));
        // mock 方法（优惠劵 Coupon 信息）
        CouponDO coupon = randomPojo(CouponDO.class, o -> o.setId(1024L).setName("程序员节")
                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductSpuIds(asList(1L, 2L))
                .setUsePrice(350).setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType())
                .setDiscountPercent(50).setDiscountLimitPrice(70));
        when(couponService.validCoupon(eq(1024L), eq(calculateReqDTO.getUserId()))).thenReturn(coupon);

        // 调用
        PriceCalculateRespDTO priceCalculate = priceService.calculatePrice(calculateReqDTO);
        // 断言 Order 部分
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        assertEquals(order.getOriginalPrice(), 470);
        assertEquals(order.getOrderPrice(), 470);
        assertEquals(order.getDiscountPrice(), 0);
        assertEquals(order.getPointPrice(), 0);
        assertEquals(order.getDeliveryPrice(), 0);
        assertEquals(order.getPayPrice(), 400);
        assertEquals(order.getCouponId(), 1024L);
        assertEquals(order.getCouponPrice(), 70);
        // 断言 OrderItem 部分
        assertEquals(order.getItems().size(), 3);
        PriceCalculateRespDTO.OrderItem orderItem01 = order.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getOriginalPrice(), 200);
        assertEquals(orderItem01.getOriginalUnitPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 200);
        assertEquals(orderItem01.getOrderPartPrice(), 40);
        assertEquals(orderItem01.getOrderDividePrice(), 160);
        PriceCalculateRespDTO.OrderItem orderItem02 = order.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getOriginalPrice(), 150);
        assertEquals(orderItem02.getOriginalUnitPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 150);
        assertEquals(orderItem02.getOrderPartPrice(), 30);
        assertEquals(orderItem02.getOrderDividePrice(), 120);
        PriceCalculateRespDTO.OrderItem orderItem03 = order.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 4);
        assertEquals(orderItem03.getOriginalPrice(), 120);
        assertEquals(orderItem03.getOriginalUnitPrice(), 30);
        assertEquals(orderItem03.getDiscountPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 120);
        assertEquals(orderItem03.getOrderPartPrice(), 0);
        assertEquals(orderItem03.getOrderDividePrice(), 120);
        // 断言 Promotion 部分
        assertEquals(priceCalculate.getPromotions().size(), 1);
        PriceCalculateRespDTO.Promotion promotion01 = priceCalculate.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1024L);
        assertEquals(promotion01.getName(), "程序员节");
        assertEquals(promotion01.getType(), PromotionTypeEnum.COUPON.getType());
        assertEquals(promotion01.getLevel(), PromotionLevelEnum.COUPON.getLevel());
        assertEquals(promotion01.getOriginalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 70);
        assertTrue(promotion01.getMeet());
        assertEquals(promotion01.getMeetTip(), "优惠劵：省 0.70 元");
        assertEquals(promotion01.getItems().size(), 2);
        PriceCalculateRespDTO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getOriginalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 40);
        PriceCalculateRespDTO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getOriginalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 30);
    }

    @Test
    public void testGetMeetCouponList() {
        // 准备参数
        PriceCalculateReqDTO calculateReqDTO = new PriceCalculateReqDTO().setUserId(1024L)
                .setItems(singletonList(new PriceCalculateReqDTO.Item().setSkuId(10L).setCount(2)));
        // mock 方法(商品 SKU 信息)
        ProductSkuRespDTO productSku = randomPojo(ProductSkuRespDTO.class, o -> o.setId(10L).setPrice(100));
        when(productSkuApi.getSkuList(eq(asSet(10L)))).thenReturn(singletonList(productSku));
        // mock 方法(情况一：优惠劵未到使用时间)
        CouponDO coupon01 = randomPojo(CouponDO.class);
        doThrow(new ServiceException(COUPON_VALID_TIME_NOT_NOW)).when(couponService).validCoupon(coupon01);
        // mock 方法(情况二：所结算商品没有符合条件的商品)
        CouponDO coupon02 = randomPojo(CouponDO.class);
        // mock 方法(情况三：使用金额不足)
        CouponDO coupon03 = randomPojo(CouponDO.class, o -> o.setProductScope(PromotionProductScopeEnum.ALL.getScope())
                .setUsePrice(300));
        // mock 方法(情况五：满足条件)
        CouponDO coupon04 = randomPojo(CouponDO.class, o -> o.setProductScope(PromotionProductScopeEnum.ALL.getScope())
                .setUsePrice(190));
        // mock 方法（获得用户的待使用优惠劵）
        when(couponService.getCouponList(eq(1024L), eq(CouponStatusEnum.UNUSED.getStatus())))
                .thenReturn(asList(coupon01, coupon02, coupon03, coupon04));
        // 调用
        List<CouponMeetRespDTO> list = priceService.getMeetCouponList(calculateReqDTO);
        // 断言
        assertEquals(list.size(), 4);
        // 断言情况一：优惠劵未到使用时间
        CouponMeetRespDTO couponMeetRespDTO01 = list.get(0);
        assertPojoEquals(couponMeetRespDTO01, coupon01);
        assertFalse(couponMeetRespDTO01.getMeet());
        assertEquals(couponMeetRespDTO01.getMeetTip(), "优惠劵未到使用时间");
        // 断言情况二：所结算商品没有符合条件的商品
        CouponMeetRespDTO couponMeetRespDTO02 = list.get(1);
        assertPojoEquals(couponMeetRespDTO02, coupon02);
        assertFalse(couponMeetRespDTO02.getMeet());
        assertEquals(couponMeetRespDTO02.getMeetTip(), "所结算商品没有符合条件的商品");
        // 断言情况三：差 %s 元可用优惠劵
        CouponMeetRespDTO couponMeetRespDTO03 = list.get(2);
        assertPojoEquals(couponMeetRespDTO03, coupon03);
        assertFalse(couponMeetRespDTO03.getMeet());
        assertEquals(couponMeetRespDTO03.getMeetTip(), "所结算的商品中未满足使用的金额");
        // 断言情况四：满足条件
        CouponMeetRespDTO couponMeetRespDTO04 = list.get(3);
        assertPojoEquals(couponMeetRespDTO04, coupon04);
        assertTrue(couponMeetRespDTO04.getMeet());
        assertNull(couponMeetRespDTO04.getMeetTip());
    }

}
