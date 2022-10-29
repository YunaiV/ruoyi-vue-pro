package cn.iocoder.yudao.module.market.service.price;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.market.enums.common.PromotionLevelEnum;
import cn.iocoder.yudao.module.market.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.market.service.discount.DiscountService;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
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
        PriceCalculateRespDTO.OrderItem orderItem = order.getItems().get(0);
        assertEquals(order.getItems().size(), 1);
        assertEquals(orderItem.getSkuId(), 10L);
        assertEquals(orderItem.getCount(), 2);
        assertEquals(orderItem.getOriginalPrice(), 200);
        assertEquals(orderItem.getOriginalUnitPrice(), 100);
        assertEquals(orderItem.getDiscountPrice(), 20);
        assertEquals(orderItem.getPayPrice(), 180);
        assertEquals(orderItem.getOrderPartPrice(), 0);
        assertEquals(orderItem.getOrderDividePrice(), 180);
        // 断言 Promotion 部分
        PriceCalculateRespDTO.Promotion promotion = priceCalculate.getPromotions().get(0);
        assertEquals(priceCalculate.getPromotions().size(), 1);
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

}
