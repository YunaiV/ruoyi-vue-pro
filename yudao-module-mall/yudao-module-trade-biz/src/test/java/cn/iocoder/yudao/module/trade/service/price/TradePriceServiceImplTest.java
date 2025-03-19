package cn.iocoder.yudao.module.trade.service.price;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.calculator.TradePriceCalculator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link TradePriceServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
public class TradePriceServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradePriceServiceImpl tradePriceService;

    @Mock
    private ProductSkuApi productSkuApi;
    @Mock
    private ProductSpuApi productSpuApi;
    @Mock
    private List<TradePriceCalculator> priceCalculators;

    @Test
    public void testCalculatePrice() {
        // 准备参数
        TradePriceCalculateReqBO calculateReqBO = new TradePriceCalculateReqBO()
                .setUserId(10L)
                .setCouponId(20L).setAddressId(30L)
                .setItems(Arrays.asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(100L).setCount(1).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(200L).setCount(3).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(300L).setCount(6).setCartId(233L).setSelected(false)
                ));
        // mock 方法
        List<ProductSkuRespDTO> skuList = Arrays.asList(
                new ProductSkuRespDTO().setId(100L).setStock(500).setPrice(1000).setPicUrl("https://t.cn/1.png").setSpuId(1001L)
                        .setProperties(singletonList(new ProductPropertyValueDetailRespDTO().setPropertyId(1L).setPropertyName("颜色")
                                .setValueId(2L).setValueName("红色"))),
                new ProductSkuRespDTO().setId(200L).setStock(400).setPrice(2000).setPicUrl("https://t.cn/2.png").setSpuId(1001L)
                        .setProperties(singletonList(new ProductPropertyValueDetailRespDTO().setPropertyId(1L).setPropertyName("颜色")
                                .setValueId(3L).setValueName("黄色"))),
                new ProductSkuRespDTO().setId(300L).setStock(600).setPrice(3000).setPicUrl("https://t.cn/3.png").setSpuId(1001L)
                        .setProperties(singletonList(new ProductPropertyValueDetailRespDTO().setPropertyId(1L).setPropertyName("颜色")
                                .setValueId(4L).setValueName("黑色")))
        );
        when(productSkuApi.getSkuList(Mockito.eq(asSet(100L, 200L, 300L)))).thenReturn(skuList);
        when(productSpuApi.getSpuList(Mockito.eq(asSet(1001L))))
                .thenReturn(singletonList(new ProductSpuRespDTO().setId(1001L).setName("小菜").setCategoryId(666L)
                        .setStatus(ProductSpuStatusEnum.ENABLE.getStatus())));

        // 调用
        TradePriceCalculateRespBO calculateRespBO = tradePriceService.calculateOrderPrice(calculateReqBO);
        // 断言
        assertEquals(TradeOrderTypeEnum.NORMAL.getType(), calculateRespBO.getType());
        assertEquals(0, calculateRespBO.getPromotions().size());
        assertNull(calculateRespBO.getCouponId());
        // 断言：订单价格
        assertEquals(7000, calculateRespBO.getPrice().getTotalPrice());
        assertEquals(0, calculateRespBO.getPrice().getDiscountPrice());
        assertEquals(0, calculateRespBO.getPrice().getDeliveryPrice());
        assertEquals(0, calculateRespBO.getPrice().getCouponPrice());
        assertEquals(0, calculateRespBO.getPrice().getPointPrice());
        assertEquals(7000, calculateRespBO.getPrice().getPayPrice());
        // 断言：SKU 1
        assertEquals(1001L, calculateRespBO.getItems().get(0).getSpuId());
        assertEquals(100L, calculateRespBO.getItems().get(0).getSkuId());
        assertEquals(1, calculateRespBO.getItems().get(0).getCount());
        assertNull(calculateRespBO.getItems().get(0).getCartId());
        assertTrue(calculateRespBO.getItems().get(0).getSelected());
        assertEquals(1000, calculateRespBO.getItems().get(0).getPrice());
        assertEquals(0, calculateRespBO.getItems().get(0).getDiscountPrice());
        assertEquals(0, calculateRespBO.getItems().get(0).getDeliveryPrice());
        assertEquals(0, calculateRespBO.getItems().get(0).getCouponPrice());
        assertEquals(0, calculateRespBO.getItems().get(0).getPointPrice());
        assertEquals(1000, calculateRespBO.getItems().get(0).getPayPrice());
        assertEquals("小菜", calculateRespBO.getItems().get(0).getSpuName());
        assertEquals("https://t.cn/1.png", calculateRespBO.getItems().get(0).getPicUrl());
        assertEquals(666L, calculateRespBO.getItems().get(0).getCategoryId());
        assertEquals(skuList.get(0).getProperties(), calculateRespBO.getItems().get(0).getProperties());
        // 断言：SKU 2
        assertEquals(1001L, calculateRespBO.getItems().get(1).getSpuId());
        assertEquals(200L, calculateRespBO.getItems().get(1).getSkuId());
        assertEquals(3, calculateRespBO.getItems().get(1).getCount());
        assertNull(calculateRespBO.getItems().get(1).getCartId());
        assertTrue(calculateRespBO.getItems().get(1).getSelected());
        assertEquals(2000, calculateRespBO.getItems().get(1).getPrice());
        assertEquals(0, calculateRespBO.getItems().get(1).getDiscountPrice());
        assertEquals(0, calculateRespBO.getItems().get(1).getDeliveryPrice());
        assertEquals(0, calculateRespBO.getItems().get(1).getCouponPrice());
        assertEquals(0, calculateRespBO.getItems().get(1).getPointPrice());
        assertEquals(6000, calculateRespBO.getItems().get(1).getPayPrice());
        assertEquals("小菜", calculateRespBO.getItems().get(1).getSpuName());
        assertEquals("https://t.cn/2.png", calculateRespBO.getItems().get(1).getPicUrl());
        assertEquals(666L, calculateRespBO.getItems().get(1).getCategoryId());
        assertEquals(skuList.get(1).getProperties(), calculateRespBO.getItems().get(1).getProperties());
        // 断言：SKU 3
        assertEquals(1001L, calculateRespBO.getItems().get(2).getSpuId());
        assertEquals(300L, calculateRespBO.getItems().get(2).getSkuId());
        assertEquals(6, calculateRespBO.getItems().get(2).getCount());
        assertEquals(233L, calculateRespBO.getItems().get(2).getCartId());
        assertFalse(calculateRespBO.getItems().get(2).getSelected());
        assertEquals(3000, calculateRespBO.getItems().get(2).getPrice());
        assertEquals(0, calculateRespBO.getItems().get(2).getDiscountPrice());
        assertEquals(0, calculateRespBO.getItems().get(2).getDeliveryPrice());
        assertEquals(0, calculateRespBO.getItems().get(2).getCouponPrice());
        assertEquals(0, calculateRespBO.getItems().get(2).getPointPrice());
        assertEquals(18000, calculateRespBO.getItems().get(2).getPayPrice());
        assertEquals("小菜", calculateRespBO.getItems().get(2).getSpuName());
        assertEquals("https://t.cn/3.png", calculateRespBO.getItems().get(2).getPicUrl());
        assertEquals(666L, calculateRespBO.getItems().get(2).getCategoryId());
        assertEquals(skuList.get(2).getProperties(), calculateRespBO.getItems().get(2).getProperties());
    }

}
