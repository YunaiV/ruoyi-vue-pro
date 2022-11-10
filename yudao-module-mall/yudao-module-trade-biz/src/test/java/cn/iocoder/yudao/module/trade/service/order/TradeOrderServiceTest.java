package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.promotion.api.coupon.CouponApi;
import cn.iocoder.yudao.module.promotion.api.price.PriceApi;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.orderitem.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderConfig;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
    @MockBean
    private CouponApi couponApi;

    @MockBean
    private TradeOrderProperties tradeOrderProperties;

    @BeforeEach
    public void setUp() {
        when(tradeOrderProperties.getAppId()).thenReturn(888L);
        when(tradeOrderProperties.getExpireTime()).thenReturn(Duration.ofDays(1));
    }

    @Test
    public void testCreateTradeOrder_success() {
        // 准备参数
        Long userId = 100L;
        String userIp = "127.0.0.1";
        AppTradeOrderCreateReqVO reqVO = new AppTradeOrderCreateReqVO()
                .setAddressId(10L).setCouponId(101L).setRemark("我是备注").setFromCart(true)
                .setItems(Arrays.asList(new AppTradeOrderCreateReqVO.Item().setSkuId(1L).setCount(3),
                        new AppTradeOrderCreateReqVO.Item().setSkuId(2L).setCount(4)));
        // mock 方法（商品 SKU 检查）
        ProductSkuRespDTO sku01 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(1L).setSpuId(11L)
                .setPrice(50).setStock(100).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setProperties(singletonList(new ProductSkuRespDTO.Property().setPropertyId(111L).setValueId(222L))));
        ProductSkuRespDTO sku02 = randomPojo(ProductSkuRespDTO.class, o -> o.setId(2L).setSpuId(21L)
                .setPrice(20).setStock(50).setStatus(CommonStatusEnum.ENABLE.getStatus()))
                .setProperties(singletonList(new ProductSkuRespDTO.Property().setPropertyId(333L).setValueId(444L)));
        when(productSkuApi.getSkuList(eq(asSet(1L, 2L)))).thenReturn(Arrays.asList(sku01, sku02));
        // mock 方法（商品 SPU 检查）
        ProductSpuRespDTO spu01 = randomPojo(ProductSpuRespDTO.class, o -> o.setId(11L)
                .setStatus(ProductSpuStatusEnum.ENABLE.getStatus()).setName("商品 1"));
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
        // mock 方法（创建支付单）
        when(payOrderApi.createPayOrder(argThat(createReqDTO -> {
            assertEquals(createReqDTO.getAppId(), 888L);
            assertEquals(createReqDTO.getUserIp(), userIp);
            assertNotNull(createReqDTO.getMerchantOrderId()); // 由于 tradeOrderId 后生成，只能校验非空
            assertEquals(createReqDTO.getSubject(), "商品 1 等多件");
            assertNull(createReqDTO.getBody());
            assertEquals(createReqDTO.getAmount(), 80);
            assertNotNull(createReqDTO.getExpireTime());
            return true;
        }))).thenReturn(1000L);

        // 调用方法
        Long tradeOrderId = tradeOrderService.createTradeOrder(userId, userIp, reqVO);
        // 断言 TradeOrderDO 订单
        List<TradeOrderDO> tradeOrderDOs = tradeOrderMapper.selectList();
        assertEquals(tradeOrderDOs.size(), 1);
        TradeOrderDO tradeOrderDO = tradeOrderDOs.get(0);
        assertEquals(tradeOrderDO.getId(), tradeOrderId);
        assertNotNull(tradeOrderDO.getNo());
        assertEquals(tradeOrderDO.getType(), TradeOrderTypeEnum.NORMAL.getType());
        assertEquals(tradeOrderDO.getTerminal(), TerminalEnum.H5.getTerminal());
        assertEquals(tradeOrderDO.getUserId(), userId);
        assertEquals(tradeOrderDO.getUserIp(), userIp);
        assertEquals(tradeOrderDO.getStatus(), TradeOrderStatusEnum.WAITING_PAYMENT.getStatus());
        assertEquals(tradeOrderDO.getProductCount(), 7);
        assertNull(tradeOrderDO.getFinishTime());
        assertNull(tradeOrderDO.getCancelTime());
        assertNull(tradeOrderDO.getCancelType());
        assertEquals(tradeOrderDO.getUserRemark(), "我是备注");
        assertNull(tradeOrderDO.getRemark());
        assertFalse(tradeOrderDO.getPayed());
        assertNull(tradeOrderDO.getPayTime());
        assertEquals(tradeOrderDO.getOriginalPrice(), 230);
        assertEquals(tradeOrderDO.getOrderPrice(), 100);
        assertEquals(tradeOrderDO.getDiscountPrice(), 0);
        assertEquals(tradeOrderDO.getAdjustPrice(), 0);
        assertEquals(tradeOrderDO.getPayPrice(), 80);
        assertEquals(tradeOrderDO.getPayOrderId(), 1000L);
        assertNull(tradeOrderDO.getPayChannel());
        assertNull(tradeOrderDO.getDeliveryTemplateId());
        assertNull(tradeOrderDO.getExpressNo());
        assertFalse(tradeOrderDO.getDeliveryStatus());
        assertNull(tradeOrderDO.getDeliveryTime());
        assertNull(tradeOrderDO.getReceiveTime());
        assertEquals(tradeOrderDO.getReceiverName(), "芋艿");
        assertEquals(tradeOrderDO.getReceiverMobile(), "15601691300");
        assertEquals(tradeOrderDO.getReceiverAreaId(), 3306L);
        assertEquals(tradeOrderDO.getReceiverPostCode(), 85757);
        assertEquals(tradeOrderDO.getReceiverDetailAddress(), "土豆村");
        assertEquals(tradeOrderDO.getRefundStatus(), TradeOrderRefundStatusEnum.NONE.getStatus());
        assertEquals(tradeOrderDO.getRefundPrice(), 0);
        assertEquals(tradeOrderDO.getCouponPrice(), 30);
        assertEquals(tradeOrderDO.getPointPrice(), 10);
        // 断言 TradeOrderItemDO 订单（第 1 个）
        List<TradeOrderItemDO> tradeOrderItemDOs = tradeOrderItemMapper.selectList();
        assertEquals(tradeOrderItemDOs.size(), 2);
        TradeOrderItemDO tradeOrderItemDO01 = tradeOrderItemDOs.get(0);
        assertNotNull(tradeOrderItemDO01.getId());
        assertEquals(tradeOrderItemDO01.getUserId(), userId);
        assertEquals(tradeOrderItemDO01.getOrderId(), tradeOrderId);
        assertEquals(tradeOrderItemDO01.getSpuId(), 11L);
        assertEquals(tradeOrderItemDO01.getSkuId(), 1L);
        assertEquals(tradeOrderItemDO01.getProperties().size(), 1);
        assertEquals(tradeOrderItemDO01.getProperties().get(0).getPropertyId(), 111L);
        assertEquals(tradeOrderItemDO01.getProperties().get(0).getValueId(), 222L);
        assertEquals(tradeOrderItemDO01.getName(), sku01.getName());
        assertEquals(tradeOrderItemDO01.getPicUrl(), sku01.getPicUrl());
        assertEquals(tradeOrderItemDO01.getCount(), 3);
        assertEquals(tradeOrderItemDO01.getOriginalPrice(), 150);
        assertEquals(tradeOrderItemDO01.getOriginalUnitPrice(), 50);
        assertEquals(tradeOrderItemDO01.getDiscountPrice(), 20);
        assertEquals(tradeOrderItemDO01.getPayPrice(), 130);
        assertEquals(tradeOrderItemDO01.getOrderPartPrice(), 7);
        assertEquals(tradeOrderItemDO01.getOrderDividePrice(), 35);
        assertEquals(tradeOrderItemDO01.getRefundStatus(), TradeOrderItemRefundStatusEnum.NONE.getStatus());
        assertEquals(tradeOrderItemDO01.getRefundTotal(), 0);
        // 断言 TradeOrderItemDO 订单（第 2 个）
        TradeOrderItemDO tradeOrderItemDO02 = tradeOrderItemDOs.get(1);
        assertNotNull(tradeOrderItemDO02.getId());
        assertEquals(tradeOrderItemDO02.getUserId(), userId);
        assertEquals(tradeOrderItemDO02.getOrderId(), tradeOrderId);
        assertEquals(tradeOrderItemDO02.getSpuId(), 21L);
        assertEquals(tradeOrderItemDO02.getSkuId(), 2L);
        assertEquals(tradeOrderItemDO02.getProperties().size(), 1);
        assertEquals(tradeOrderItemDO02.getProperties().get(0).getPropertyId(), 333L);
        assertEquals(tradeOrderItemDO02.getProperties().get(0).getValueId(), 444L);
        assertEquals(tradeOrderItemDO02.getName(), sku02.getName());
        assertEquals(tradeOrderItemDO02.getPicUrl(), sku02.getPicUrl());
        assertEquals(tradeOrderItemDO02.getCount(), 4);
        assertEquals(tradeOrderItemDO02.getOriginalPrice(), 80);
        assertEquals(tradeOrderItemDO02.getOriginalUnitPrice(), 20);
        assertEquals(tradeOrderItemDO02.getDiscountPrice(), 40);
        assertEquals(tradeOrderItemDO02.getPayPrice(), 40);
        assertEquals(tradeOrderItemDO02.getOrderPartPrice(), 15);
        assertEquals(tradeOrderItemDO02.getOrderDividePrice(), 25);
        assertEquals(tradeOrderItemDO02.getRefundStatus(), TradeOrderItemRefundStatusEnum.NONE.getStatus());
        assertEquals(tradeOrderItemDO02.getRefundTotal(), 0);
        // 校验调用
        verify(productSkuApi).updateSkuStock(argThat(new ArgumentMatcher<ProductSkuUpdateStockReqDTO>() {

            @Override
            public boolean matches(ProductSkuUpdateStockReqDTO updateStockReqDTO) {
                assertEquals(updateStockReqDTO.getItems().size(), 2);
                assertEquals(updateStockReqDTO.getItems().get(0).getId(), 1L);
                assertEquals(updateStockReqDTO.getItems().get(0).getIncrCount(), 3);
                assertEquals(updateStockReqDTO.getItems().get(1).getId(), 2L);
                assertEquals(updateStockReqDTO.getItems().get(1).getIncrCount(), 4);
                return true;
            }

        }));
        verify(couponApi).useCoupon(argThat(reqDTO -> {
            assertEquals(reqDTO.getId(), reqVO.getCouponId());
            assertEquals(reqDTO.getUserId(), userId);
            assertEquals(reqDTO.getOrderId(), tradeOrderId);
            return true;
        }));
//        //mock 支付订单信息
//        when(payOrderApi.createPayOrder(any())).thenReturn(1L);

//        //价格
//        assertEquals(calculateRespDTO.getOrder().getItems().get(0).getPresentPrice(), tradeOrderItemDO.getPresentPrice());
    }

}
