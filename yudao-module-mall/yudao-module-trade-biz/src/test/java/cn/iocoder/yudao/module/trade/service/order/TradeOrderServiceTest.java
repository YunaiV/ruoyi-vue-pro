package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.promotion.api.price.PriceApi;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.orderitem.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeOrderServiceImpl} 的单元测试类
 *
 * @author LeeYan9
 * @since 2022-09-07
 */
@Import({TradeOrderServiceImpl.class, TradeOrderConfig.class})
class TradeOrderServiceTest extends BaseDbUnitTest {

    @Resource
    private TradeOrderServiceImpl tradeOrderService;

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;

    @MockBean
    private ProductSpuApi productSpuApi;
    @MockBean
    private ProductSkuApi productSkuApi;
    @MockBean
    private PriceApi priceApi;
    @MockBean
    private PayOrderApi payOrderApi;
    @MockBean
    private AddressApi addressApi;

    // 1, 3 个，50 块;打折 20；总和 = 60；42;
    // 2, 4 个，20 块；打折 10；总和 = 40；28;
    // 优惠劵，满 100 减 30

    @Test
    public void testCreateTradeOrder_success() {
        // 准备参数
        Long userId = 100L;
        String clientIp = "127.0.0.1";
        AppTradeOrderCreateReqVO reqVO = new AppTradeOrderCreateReqVO()
                .setAddressId(10L).setCouponId(101L).setRemark("我是备注").setFromCart(true)
                .setItems(Arrays.asList(new AppTradeOrderCreateReqVO.Item().setSkuId(1L).setCount(3),
                        new AppTradeOrderCreateReqVO.Item().setSkuId(2L).setCount(4)));
        // mock 方法（商品 SKU 检查）
        ProductSkuRespDTO sku01 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(1L).setSpuId(11L)
                .setPrice(50).setStock(100).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        ProductSkuRespDTO sku02 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(2L).setSpuId(21L)
                .setPrice(20).setStock(50).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(productSkuApi.getSkuList(eq(asSet(1L, 2L)))).thenReturn(Arrays.asList(sku01, sku02));
        // mock 方法（商品 SPU 检查）
        ProductSpuRespDTO spu01 = randomPojo(ProductSpuRespDTO.class, o -> o.setId(11L)
                .setStatus(ProductSpuStatusEnum.ENABLE.getStatus()));
        ProductSpuRespDTO spu02 = randomPojo(ProductSpuRespDTO.class, o -> o.setId(21L)
                .setStatus(ProductSpuStatusEnum.ENABLE.getStatus()));
        when(productSpuApi.getSpuList(eq(asSet(11L, 21L)))).thenReturn(Arrays.asList(spu01, spu02));
        // mock 方法（用户收件地址的校验）
        AddressRespDTO addressRespDTO = new AddressRespDTO().setId(10L).setUserId(userId).setName("芋艿")
                .setMobile("15601691300").setAreaId(3306L).setPostCode("85757").setDetailAddress("土豆村");
        when(addressApi.getAddress(eq(userId), eq(10L))).thenReturn(addressRespDTO);
        // mock 方法（价格计算）
        PriceCalculateRespDTO.OrderItem priceOrderItem01 = new PriceCalculateRespDTO.OrderItem()
                .setSpuId(11L).setSkuId(1L).setCount(3).setOriginalPrice(150).setOriginalUnitPrice(50)
                .setDiscountPrice(20).setPayPrice(130).setOrderPartPrice(7).setOrderDividePrice(35);
        PriceCalculateRespDTO.OrderItem priceOrderItem02 = new PriceCalculateRespDTO.OrderItem()
                .setSpuId(21L).setSkuId(2L).setCount(4).setOriginalPrice(80).setOriginalUnitPrice(20)
                .setDiscountPrice(40).setPayPrice(40).setOrderPartPrice(15).setOrderDividePrice(25);
        PriceCalculateRespDTO.Order priceOrder = new PriceCalculateRespDTO.Order()
                .setOriginalPrice(230).setOrderPrice(100).setDiscountPrice(0).setCouponPrice(30)
                .setPointPrice(10).setDeliveryPrice(20).setPayPrice(80).setCouponId(101L).setCouponPrice(30)
                .setItems(Arrays.asList(priceOrderItem01, priceOrderItem02));
        when(priceApi.calculatePrice(argThat(priceCalculateReqDTO -> {
            assertEquals(priceCalculateReqDTO.getUserId(), 100L);
            assertEquals(priceCalculateReqDTO.getCouponId(), 101L);
            assertEquals(priceCalculateReqDTO.getItems().get(0).getSkuId(), 1L);
            assertEquals(priceCalculateReqDTO.getItems().get(0).getCount(), 3);
            assertEquals(priceCalculateReqDTO.getItems().get(1).getSkuId(), 2L);
            assertEquals(priceCalculateReqDTO.getItems().get(1).getCount(), 4);
            return true;
        }))).thenReturn(new PriceCalculateRespDTO().setOrder(priceOrder));

        // 调用方法
        Long tradeOrderId = tradeOrderService.createTradeOrder(userId, clientIp, reqVO);
        System.out.println(tradeOrderId);

//        // mock 价格信息
//        PriceCalculateRespDTO calculateRespDTO = randomPojo(PriceCalculateRespDTO.class, priceCalculateRespDTO -> {
//            PriceCalculateRespDTO.OrderItem item = priceCalculateRespDTO.getOrder().getItems().get(0);
//            item.setSkuId(1L);
//            item.setCount(2);
//            priceCalculateRespDTO.getOrder().setItems(Collections.singletonList(item));
//        });
//        when(priceApi.calculatePrice(any())).thenReturn(calculateRespDTO);
//        //mock 支付订单信息
//        when(payOrderApi.createPayOrder(any())).thenReturn(1L);


//        // 创建交易订单,支付订单记录
//        Long payOrderId = tradeOrderService.createTradeOrder(1L, "127.0.0.1", tradeOrderCreateReqVO);
//        //断言交易订单
//        TradeOrderDO tradeOrderDO = tradeOrderMapper.selectOne(TradeOrderDO::getUserId, 1L);
//        assertNotNull(tradeOrderDO);
//        //价格&用户
//        assertEquals(calculateRespDTO.getOrder().getPayPrice(), tradeOrderDO.getPayPrice());
//        assertEquals(1L, tradeOrderDO.getUserId());
//        //断言交易订单项
//        TradeOrderItemDO tradeOrderItemDO = tradeOrderItemMapper.selectOne(TradeOrderItemDO::getOrderId, tradeOrderDO.getId());
//        assertNotNull(tradeOrderDO);
//        //商品&用户
//        assertEquals(skuInfoRespDTO.getId(), tradeOrderItemDO.getSkuId());
//        assertEquals(1L, tradeOrderItemDO.getUserId());
//        //价格
//        assertEquals(calculateRespDTO.getOrder().getItems().get(0).getPresentPrice(), tradeOrderItemDO.getPresentPrice());
    }

}
