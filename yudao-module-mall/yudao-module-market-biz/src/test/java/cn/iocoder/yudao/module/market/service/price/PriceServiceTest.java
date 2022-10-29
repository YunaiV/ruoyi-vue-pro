package cn.iocoder.yudao.module.market.service.price;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.market.dal.dataobject.discount.DiscountProductDO;
import cn.iocoder.yudao.module.market.enums.common.PromotionLevelEnum;
import cn.iocoder.yudao.module.market.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.market.service.discount.DiscountService;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
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
    private DiscountService discountService;
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
        when(productSkuApi.getSkuList(eq(SetUtils.asSet(10L)))).thenReturn(singletonList(productSku));

        // 调用
        PriceCalculateRespDTO priceCalculate = priceService.calculatePrice(calculateReqDTO);
        // 断言 Order 部分
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        assertEquals(order.getOriginalPrice(), 200);
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
        when(productSkuApi.getSkuList(eq(SetUtils.asSet(10L, 20L)))).thenReturn(asList(productSku01, productSku02));
        // mock 方法（限时折扣 DiscountActivity 信息）
        DiscountProductDO discountProduct01 = randomPojo(DiscountProductDO.class, o -> o.setActivityId(1000L).setActivityName("活动 1000 号")
                .setSkuId(10L).setPromotionPrice(80));
        DiscountProductDO discountProduct02 = randomPojo(DiscountProductDO.class, o -> o.setActivityId(2000L).setActivityName("活动 2000 号")
                .setSkuId(20L).setPromotionPrice(40));
        when(discountService.getMatchDiscountProducts(eq(SetUtils.asSet(10L, 20L)))).thenReturn(
                MapUtil.builder(10L, discountProduct01).put(20L, discountProduct02).map());

        // 调用
        PriceCalculateRespDTO priceCalculate = priceService.calculatePrice(calculateReqDTO);
        // 断言 Order 部分
        PriceCalculateRespDTO.Order order = priceCalculate.getOrder();
        assertEquals(order.getOriginalPrice(), 350);
        assertEquals(order.getDiscountPrice(), 0);
        assertEquals(order.getPointPrice(), 0);
        assertEquals(order.getDeliveryPrice(), 0);
        assertEquals(order.getPayPrice(), 280);
        assertNull(order.getCouponId());
        // 断言 OrderItem 部分
        assertEquals(order.getItems().size(), 2);
        PriceCalculateRespDTO.OrderItem orderItem01 = order.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getOriginalPrice(), 200);
        assertEquals(orderItem01.getOriginalUnitPrice(), 100);
        assertEquals(orderItem01.getDiscountPrice(), 40);
        assertEquals(orderItem01.getPayPrice(), 160);
        assertEquals(orderItem01.getOrderPartPrice(), 0);
        assertEquals(orderItem01.getOrderDividePrice(), 160);
        PriceCalculateRespDTO.OrderItem orderItem02 = order.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getOriginalPrice(), 150);
        assertEquals(orderItem02.getOriginalUnitPrice(), 50);
        assertEquals(orderItem02.getDiscountPrice(), 30);
        assertEquals(orderItem02.getPayPrice(), 120);
        assertEquals(orderItem02.getOrderPartPrice(), 0);
        assertEquals(orderItem02.getOrderDividePrice(), 120);
        // 断言 Promotion 部分
        assertEquals(priceCalculate.getPromotions().size(), 2);
        PriceCalculateRespDTO.Promotion promotion01 = priceCalculate.getPromotions().get(0);
        assertEquals(promotion01.getId(), 1000L);
        assertEquals(promotion01.getName(), "活动 1000 号");
        assertEquals(promotion01.getType(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType());
        assertEquals(promotion01.getLevel(), PromotionLevelEnum.SKU.getLevel());
        assertEquals(promotion01.getOriginalPrice(), 200);
        assertEquals(promotion01.getDiscountPrice(), 40);
        assertTrue(promotion01.getMeet());
        assertEquals(promotion01.getMeetTip(), "限时折扣：省 0.40 元");
        PriceCalculateRespDTO.PromotionItem promotionItem01 = promotion01.getItems().get(0);
        assertEquals(promotion01.getItems().size(), 1);
        assertEquals(promotionItem01.getSkuId(), 10L);
        assertEquals(promotionItem01.getOriginalPrice(), 200);
        assertEquals(promotionItem01.getDiscountPrice(), 40);
        PriceCalculateRespDTO.Promotion promotion02 = priceCalculate.getPromotions().get(1);
        assertEquals(promotion02.getId(), 2000L);
        assertEquals(promotion02.getName(), "活动 2000 号");
        assertEquals(promotion02.getType(), PromotionTypeEnum.DISCOUNT_ACTIVITY.getType());
        assertEquals(promotion02.getLevel(), PromotionLevelEnum.SKU.getLevel());
        assertEquals(promotion02.getOriginalPrice(), 150);
        assertEquals(promotion02.getDiscountPrice(), 30);
        assertTrue(promotion02.getMeet());
        assertEquals(promotion02.getMeetTip(), "限时折扣：省 0.30 元");
        PriceCalculateRespDTO.PromotionItem promotionItem02 = promotion02.getItems().get(0);
        assertEquals(promotion02.getItems().size(), 1);
        assertEquals(promotionItem02.getSkuId(), 20L);
        assertEquals(promotionItem02.getOriginalPrice(), 150);
        assertEquals(promotionItem02.getDiscountPrice(), 30);
    }

}
