package cn.iocoder.yudao.module.trade.service.order;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.product.api.comment.ProductCommentApi;
import cn.iocoder.yudao.module.promotion.api.combination.CombinationRecordApi;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.redis.no.TradeNoRedisDAO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderRefundStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.framework.order.config.TradeOrderProperties;
import cn.iocoder.yudao.module.trade.framework.order.core.aop.TradeOrderLogAspect;
import cn.iocoder.yudao.module.trade.service.cart.CartService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreService;
import cn.iocoder.yudao.module.trade.service.message.TradeMessageService;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import cn.iocoder.yudao.module.trade.service.price.TradePriceService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link TradeOrderUpdateServiceImpl} 的单元测试类
 *
 * @author LeeYan9
 * @since 2022-09-07
 */
// DONE 芋艿：后续 fix 补充的单测
@Import(TradeOrderUpdateServiceImpl.class)
public class TradeOrderUpdateServiceTest extends BaseDbUnitTest {

    @Resource
    private TradeOrderUpdateServiceImpl tradeOrderUpdateService;

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private TradeNoRedisDAO tradeNoRedisDAO;
    @MockitoBean
    private TradeOrderHandler tradeOrderHandler;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private TradePriceService tradePriceService;
    @MockitoBean
    private DeliveryExpressService deliveryExpressService;
    @MockitoBean
    private TradeMessageService tradeMessageService;
    @MockitoBean
    private DeliveryPickUpStoreService pickUpStoreService;
    @MockitoBean
    private PayOrderApi payOrderApi;
    @MockitoBean
    private MemberAddressApi addressApi;
    @MockitoBean
    private ProductCommentApi productCommentApi;
    @MockitoBean
    private SocialClientApi socialClientApi;
    @MockitoBean
    private PayRefundApi payRefundApi;
    @MockitoBean
    private CombinationRecordApi combinationRecordApi;
    @MockitoBean
    private TradeOrderProperties tradeOrderProperties;

    @BeforeEach
    public void setUp() {
        when(tradeOrderProperties.getPayAppKey()).thenReturn("mall");
        when(tradeOrderProperties.getPayExpireTime()).thenReturn(Duration.ofDays(1));
        when(tradeNoRedisDAO.generate(anyString())).thenReturn(IdUtil.randomUUID());
    }

    @AfterEach
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        // 清理订单日志的上下文，单测没有 TradeOrderLogAspect 切面清理
        ReflectionTestUtils.invokeMethod(TradeOrderLogAspect.class, "clear");
    }

    @Test
    public void testCreateOrder_success() {
        // DONE 芋艿：重新高下
        // 准备参数
        Long userId = 100L;
        String userIp = "127.0.0.1";
        AppTradeOrderCreateReqVO reqVO = new AppTradeOrderCreateReqVO();
        reqVO.setRemark("我是备注");
        reqVO.setItems(Arrays.asList(new AppTradeOrderSettlementReqVO.Item().setCartId(1L),
                new AppTradeOrderSettlementReqVO.Item().setCartId(2L)));
        reqVO.setCouponId(101L);
        reqVO.setPointStatus(false);
        reqVO.setDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        reqVO.setAddressId(10L);
        // mock 请求（用户 IP、终端）
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(userIp);
        request.addHeader(WebFrameworkUtils.HEADER_TERMINAL, TerminalEnum.H5.getTerminal());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        // mock 方法（购物车）
        when(cartService.getCartList(eq(userId), eq(asSet(1L, 2L)))).thenReturn(Arrays.asList(
                new CartDO().setId(1L).setUserId(userId).setSpuId(11L).setSkuId(1L).setCount(3).setSelected(true),
                new CartDO().setId(2L).setUserId(userId).setSpuId(21L).setSkuId(2L).setCount(4).setSelected(true)));
        // mock 方法（价格计算）
        TradePriceCalculateRespBO.OrderItem priceOrderItem01 = new TradePriceCalculateRespBO.OrderItem()
                .setSpuId(11L).setSkuId(1L).setCount(3).setCartId(1L).setSelected(true).setSpuName("商品 1")
                .setPrice(50).setDiscountPrice(20).setDeliveryPrice(10).setCouponPrice(10).setPointPrice(5)
                .setVipPrice(0).setPayPrice(105);
        TradePriceCalculateRespBO.OrderItem priceOrderItem02 = new TradePriceCalculateRespBO.OrderItem()
                .setSpuId(21L).setSkuId(2L).setCount(4).setCartId(2L).setSelected(true).setSpuName("商品 2")
                .setPrice(20).setDiscountPrice(10).setDeliveryPrice(10).setCouponPrice(20).setPointPrice(5)
                .setVipPrice(0).setPayPrice(55);
        TradePriceCalculateRespBO.Price price = new TradePriceCalculateRespBO.Price()
                .setTotalPrice(230).setDiscountPrice(30).setDeliveryPrice(20).setCouponPrice(30)
                .setPointPrice(10).setVipPrice(0).setPayPrice(160);
        when(tradePriceService.calculateOrderPrice(argThat(calculateReqBO -> {
            assertEquals(userId, calculateReqBO.getUserId());
            assertEquals(101L, calculateReqBO.getCouponId());
            assertEquals(2, calculateReqBO.getItems().size());
            assertEquals(1L, calculateReqBO.getItems().get(0).getSkuId());
            assertEquals(3, calculateReqBO.getItems().get(0).getCount());
            assertEquals(2L, calculateReqBO.getItems().get(1).getSkuId());
            assertEquals(4, calculateReqBO.getItems().get(1).getCount());
            return true;
        }))).thenReturn(new TradePriceCalculateRespBO().setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(price).setItems(Arrays.asList(priceOrderItem01, priceOrderItem02)));
        // mock 方法（用户收件地址）
        when(addressApi.getAddress(eq(10L), eq(userId))).thenReturn(new MemberAddressRespDTO().setId(10L)
                .setUserId(userId).setName("芋艿").setMobile("15601691300").setAreaId(3306).setDetailAddress("土豆村"));
        // mock 方法（创建支付单）
        when(payOrderApi.createOrder(argThat(createReqDTO -> {
            assertEquals("mall", createReqDTO.getAppKey());
            assertEquals(userIp, createReqDTO.getUserIp());
            assertEquals(userId, createReqDTO.getUserId());
            assertNotNull(createReqDTO.getMerchantOrderId()); // 由于 tradeOrderId 后生成，只能校验非空
            assertEquals("商品 1", createReqDTO.getSubject());
            assertEquals(160, createReqDTO.getPrice());
            assertNotNull(createReqDTO.getExpireTime());
            return true;
        }))).thenReturn(1000L);

        // 调用
        TradeOrderDO order = tradeOrderUpdateService.createOrder(userId, reqVO);
        // 断言 TradeOrderDO 订单
        List<TradeOrderDO> tradeOrderDOs = tradeOrderMapper.selectList();
        assertEquals(1, tradeOrderDOs.size());
        TradeOrderDO tradeOrderDO = tradeOrderDOs.get(0);
        assertEquals(order.getId(), tradeOrderDO.getId());
        assertNotNull(tradeOrderDO.getNo());
        assertEquals(TradeOrderTypeEnum.NORMAL.getType(), tradeOrderDO.getType());
        assertEquals(TerminalEnum.H5.getTerminal(), tradeOrderDO.getTerminal());
        assertEquals(userId, tradeOrderDO.getUserId());
        assertEquals(userIp, tradeOrderDO.getUserIp());
        assertEquals(TradeOrderStatusEnum.UNPAID.getStatus(), tradeOrderDO.getStatus());
        assertEquals(7, tradeOrderDO.getProductCount());
        assertEquals("我是备注", tradeOrderDO.getUserRemark());
        assertFalse(tradeOrderDO.getPayStatus());
        assertNull(tradeOrderDO.getPayTime());
        assertEquals(1000L, tradeOrderDO.getPayOrderId());
        assertEquals(230, tradeOrderDO.getTotalPrice());
        assertEquals(30, tradeOrderDO.getDiscountPrice());
        assertEquals(20, tradeOrderDO.getDeliveryPrice());
        assertEquals(0, tradeOrderDO.getAdjustPrice());
        assertEquals(160, tradeOrderDO.getPayPrice());
        assertEquals(DeliveryTypeEnum.EXPRESS.getType(), tradeOrderDO.getDeliveryType());
        assertEquals("芋艿", tradeOrderDO.getReceiverName());
        assertEquals("15601691300", tradeOrderDO.getReceiverMobile());
        assertEquals(3306, tradeOrderDO.getReceiverAreaId());
        assertEquals("土豆村", tradeOrderDO.getReceiverDetailAddress());
        assertEquals(TradeOrderRefundStatusEnum.NONE.getStatus(), tradeOrderDO.getRefundStatus());
        assertEquals(0, tradeOrderDO.getRefundPrice());
        assertEquals(101L, tradeOrderDO.getCouponId());
        assertEquals(30, tradeOrderDO.getCouponPrice());
        assertEquals(10, tradeOrderDO.getPointPrice());
        // 断言 TradeOrderItemDO 订单项
        List<TradeOrderItemDO> tradeOrderItemDOs = tradeOrderItemMapper.selectListByOrderId(order.getId());
        assertEquals(2, tradeOrderItemDOs.size());
        assertOrderItem(tradeOrderItemDOs.get(0), order, priceOrderItem01);
        assertOrderItem(tradeOrderItemDOs.get(1), order, priceOrderItem02);
        // 断言调用
        verify(tradeOrderHandler).beforeOrderCreate(any(), any());
        verify(tradeOrderHandler).afterOrderCreate(any(), any());
        verify(cartService).deleteCart(eq(userId), eq(asSet(1L, 2L)));
    }

    private static void assertOrderItem(TradeOrderItemDO orderItem, TradeOrderDO order,
                                        TradePriceCalculateRespBO.OrderItem priceOrderItem) {
        assertNotNull(orderItem.getId());
        assertEquals(order.getUserId(), orderItem.getUserId());
        assertEquals(order.getId(), orderItem.getOrderId());
        assertEquals(priceOrderItem.getSpuId(), orderItem.getSpuId());
        assertEquals(priceOrderItem.getSpuName(), orderItem.getSpuName()); // DONE 找不到spuName
        assertEquals(priceOrderItem.getSkuId(), orderItem.getSkuId());
        assertEquals(priceOrderItem.getCartId(), orderItem.getCartId());
        assertEquals(priceOrderItem.getCount(), orderItem.getCount());
        assertEquals(priceOrderItem.getPrice(), orderItem.getPrice());
        assertEquals(priceOrderItem.getDiscountPrice(), orderItem.getDiscountPrice());
        assertEquals(priceOrderItem.getPayPrice(), orderItem.getPayPrice());
        assertEquals(TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(), orderItem.getAfterSaleStatus());
        assertFalse(orderItem.getCommentStatus());
    }

    @Test
    public void testUpdateOrderPaid() {
        // mock 数据（TradeOrder）
        TradeOrderDO order = randomPojo(TradeOrderDO.class, o -> {
            o.setId(1L).setStatus(TradeOrderStatusEnum.UNPAID.getStatus());
            o.setPayOrderId(10L).setPayStatus(false).setPayPrice(100).setPayTime(null);
        });
        tradeOrderMapper.insert(order);
        // 准备参数
        Long id = 1L;
        Long payOrderId = 10L;
        // mock 方法（支付单）
        when(payOrderApi.getOrder(eq(10L))).thenReturn(randomPojo(PayOrderRespDTO.class,
                o -> o.setStatus(PayOrderStatusEnum.SUCCESS.getStatus()).setChannelCode("wx_pub")
                        .setMerchantOrderId("1")).setPrice(100));

        // 调用
        tradeOrderUpdateService.updateOrderPaid(id, payOrderId);
        // 断言
        TradeOrderDO dbOrder = tradeOrderMapper.selectById(id);
        assertEquals(dbOrder.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus());
        assertTrue(dbOrder.getPayStatus());
        assertNotNull(dbOrder.getPayTime());
        assertEquals(dbOrder.getPayChannelCode(), "wx_pub");
        verify(tradeOrderHandler).afterPayOrder(argThat(o -> o.getId().equals(id)), anyList());
    }

    @Test
    public void testDeliveryOrder() {
        // mock 数据（TradeOrder）
        TradeOrderDO order = randomPojo(TradeOrderDO.class, o -> {
            o.setId(1L).setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus());
            o.setLogisticsId(null).setLogisticsNo(null).setDeliveryTime(null);
            o.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
            o.setDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        });
        tradeOrderMapper.insert(order);
        // 准备参数
        TradeOrderDeliveryReqVO deliveryReqVO = new TradeOrderDeliveryReqVO().setId(1L)
                .setLogisticsId(20L).setLogisticsNo("100");
        // mock 方法（物流公司）
        when(deliveryExpressService.validateDeliveryExpress(eq(20L))).thenReturn(
                randomPojo(DeliveryExpressDO.class, o -> o.setId(20L)));

        // 调用
        tradeOrderUpdateService.deliveryOrder(deliveryReqVO);
        // 断言
        TradeOrderDO dbOrder = tradeOrderMapper.selectById(1L);
        assertEquals(dbOrder.getStatus(), TradeOrderStatusEnum.DELIVERED.getStatus());
        assertPojoEquals(dbOrder, deliveryReqVO);
        assertNotNull(dbOrder.getDeliveryTime());
        // 断言调用（站内信）
        verify(tradeMessageService).sendMessageWhenDeliveryOrder(argThat(reqBO ->
                reqBO.getOrderId().equals(1L) && reqBO.getUserId().equals(order.getUserId())));
    }

    @Test // 已发货的订单，允许修改物流信息
    public void testDeliveryOrder_delivered() {
        // mock 数据（TradeOrder）
        TradeOrderDO order = randomDeliveryOrder(TradeOrderStatusEnum.DELIVERED.getStatus());
        tradeOrderMapper.insert(order);
        // 准备参数
        TradeOrderDeliveryReqVO deliveryReqVO = new TradeOrderDeliveryReqVO().setId(1L)
                .setLogisticsId(20L).setLogisticsNo("200");
        // mock 方法（物流公司）
        when(deliveryExpressService.validateDeliveryExpress(eq(20L))).thenReturn(
                randomPojo(DeliveryExpressDO.class, o -> o.setId(20L)));

        // 调用
        tradeOrderUpdateService.deliveryOrder(deliveryReqVO);
        // 断言
        TradeOrderDO dbOrder = tradeOrderMapper.selectById(1L);
        assertEquals(TradeOrderStatusEnum.DELIVERED.getStatus(), dbOrder.getStatus());
        assertEquals(20L, dbOrder.getLogisticsId());
        assertEquals("200", dbOrder.getLogisticsNo());
        assertNotNull(dbOrder.getDeliveryTime());
        verify(tradeOrderHandler).afterDeliveryOrder(argThat(o -> o.getId().equals(1L)));
    }

    @Test
    public void testDeliveryOrder_statusInvalid() {
        // 准备参数：待支付、已完成、已取消、无效状态
        List<Integer> statuses = Arrays.asList(TradeOrderStatusEnum.UNPAID.getStatus(),
                TradeOrderStatusEnum.COMPLETED.getStatus(), TradeOrderStatusEnum.CANCELED.getStatus(), 99);
        for (int i = 0; i < statuses.size(); i++) {
            // mock 数据（TradeOrder）
            Long id = i + 1L;
            Integer status = statuses.get(i);
            tradeOrderMapper.insert(randomDeliveryOrder(status).setId(id));
            TradeOrderDeliveryReqVO deliveryReqVO = new TradeOrderDeliveryReqVO().setId(id)
                    .setLogisticsId(20L).setLogisticsNo("200");

            // 调用，并断言异常
            assertServiceException(() -> tradeOrderUpdateService.deliveryOrder(deliveryReqVO),
                    ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
            // 断言：订单未变更
            assertDeliveryOrderUnchanged(id, status);
        }
        // 断言：前置处理、物流公司、消息都未执行
        verifyNoInteractions(tradeOrderHandler, deliveryExpressService, tradeMessageService, socialClientApi);
    }

    @Test
    public void testDeliveryOrder_refundStatusNotNone() {
        // mock 数据（TradeOrder）
        TradeOrderDO order = randomDeliveryOrder(TradeOrderStatusEnum.UNDELIVERED.getStatus())
                .setRefundStatus(TradeOrderRefundStatusEnum.PART.getStatus());
        tradeOrderMapper.insert(order);
        // 准备参数
        TradeOrderDeliveryReqVO deliveryReqVO = new TradeOrderDeliveryReqVO().setId(1L)
                .setLogisticsId(20L).setLogisticsNo("200");

        // 调用，并断言异常
        assertServiceException(() -> tradeOrderUpdateService.deliveryOrder(deliveryReqVO),
                ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE);
        // 断言
        assertDeliveryOrderUnchanged(1L, TradeOrderStatusEnum.UNDELIVERED.getStatus());
        verifyNoInteractions(tradeOrderHandler, deliveryExpressService, tradeMessageService, socialClientApi);
    }

    @Test // 读取订单后，订单被并发取消
    public void testDeliveryOrder_concurrentCancel() {
        assertDeliveryOrderConcurrentStatusChanged(TradeOrderStatusEnum.UNDELIVERED.getStatus(),
                TradeOrderStatusEnum.CANCELED.getStatus());
    }

    @Test // 读取订单后，订单被并发收货
    public void testDeliveryOrder_concurrentReceive() {
        assertDeliveryOrderConcurrentStatusChanged(TradeOrderStatusEnum.DELIVERED.getStatus(),
                TradeOrderStatusEnum.COMPLETED.getStatus());
    }

    private void assertDeliveryOrderConcurrentStatusChanged(Integer status, Integer newStatus) {
        // mock 数据（TradeOrder）
        TradeOrderDO order = randomDeliveryOrder(status);
        tradeOrderMapper.insert(order);
        // 准备参数
        TradeOrderDeliveryReqVO deliveryReqVO = new TradeOrderDeliveryReqVO().setId(1L)
                .setLogisticsId(20L).setLogisticsNo("200");
        // mock 方法（物流公司）：校验物流公司时，订单状态被其它事务修改并提交
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        when(deliveryExpressService.validateDeliveryExpress(eq(20L))).thenAnswer(invocation -> {
            transactionTemplate.executeWithoutResult(transactionStatus ->
                    tradeOrderMapper.updateById(new TradeOrderDO().setId(1L).setStatus(newStatus)));
            return randomPojo(DeliveryExpressDO.class, o -> o.setId(20L));
        });

        // 调用，并断言异常
        assertServiceException(() -> tradeOrderUpdateService.deliveryOrder(deliveryReqVO),
                ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        // 断言：保留并发修改后的状态，物流信息未被覆盖
        assertDeliveryOrderUnchanged(1L, newStatus);
        // 断言：发货后的消息、后置处理都未执行
        verify(tradeMessageService, never()).sendMessageWhenDeliveryOrder(any());
        verify(socialClientApi, never()).sendWxaSubscribeMessage(any());
        verify(tradeOrderHandler, never()).afterDeliveryOrder(any());
    }

    private void assertDeliveryOrderUnchanged(Long id, Integer status) {
        TradeOrderDO dbOrder = tradeOrderMapper.selectById(id);
        assertEquals(status, dbOrder.getStatus());
        assertEquals(10L, dbOrder.getLogisticsId());
        assertEquals("100", dbOrder.getLogisticsNo());
        assertEquals(buildTime(2023, 2, 10), dbOrder.getDeliveryTime());
    }

    private static TradeOrderDO randomDeliveryOrder(Integer status) {
        return randomPojo(TradeOrderDO.class, o -> {
            o.setId(1L).setStatus(status);
            o.setLogisticsId(10L).setLogisticsNo("100").setDeliveryTime(buildTime(2023, 2, 10));
            o.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
            o.setDeliveryType(DeliveryTypeEnum.EXPRESS.getType());
        });
    }

    @Test
    public void testReceiveOrder() {
        // mock 数据（TradeOrder）
        TradeOrderDO order = randomPojo(TradeOrderDO.class, o -> {
            o.setId(1L).setUserId(10L).setStatus(TradeOrderStatusEnum.DELIVERED.getStatus());
            o.setReceiveTime(null);
        });
        tradeOrderMapper.insert(order);
        // 准备参数
        Long id = 1L;
        Long userId = 10L;

        // 调用
        tradeOrderUpdateService.receiveOrderByMember(userId, id);
        // 断言
        TradeOrderDO dbOrder = tradeOrderMapper.selectById(1L);
        assertEquals(dbOrder.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());
        assertNotNull(dbOrder.getReceiveTime());
        verify(tradeOrderHandler).afterReceiveOrder(argThat(o -> o.getId().equals(id)));
    }

}
