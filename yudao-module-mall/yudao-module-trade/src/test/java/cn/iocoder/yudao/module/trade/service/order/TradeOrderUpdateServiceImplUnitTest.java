package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderItemMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import cn.iocoder.yudao.module.trade.service.order.handler.TradeOrderHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link TradeOrderUpdateServiceImpl} 的单元测试类
 *
 * 重点覆盖 {@link TradeOrderUpdateServiceImpl#updateOrderPaid(Long, Long)}：回调传入的支付单编号，
 * 必须等于订单绑定的支付单编号，避免其它支付应用中，相同商户订单编号、金额的支付单被用于串单
 *
 * @author 芋道源码
 */
public class TradeOrderUpdateServiceImplUnitTest extends BaseMockitoUnitTest {

    private static final long ORDER_ID = 1L;
    private static final long ORDER_USER_ID = 10L;
    private static final long ORDER_ITEM_ID = 50L;
    /**
     * 订单绑定的支付单编号
     */
    private static final long PAY_ORDER_ID = 100L;
    /**
     * 其它支付单编号，例如说其它支付应用中，相同商户订单编号、金额的支付单
     */
    private static final long OTHER_PAY_ORDER_ID = 200L;
    private static final int ORDER_PAY_PRICE = 500;
    private static final String PAY_CHANNEL_CODE = "wx_lite";

    @InjectMocks
    private TradeOrderUpdateServiceImpl tradeOrderUpdateService;

    @Mock
    private TradeOrderMapper tradeOrderMapper;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Mock
    private TradeOrderHandler tradeOrderHandler;
    @Mock
    private PayOrderApi payOrderApi;

    private MockedStatic<TradeOrderLogUtils> tradeOrderLogUtilsMockedStatic;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(tradeOrderUpdateService, "tradeOrderHandlers",
                Collections.singletonList(tradeOrderHandler));
        // mock 订单操作日志的上下文，用于断言是否记录日志
        tradeOrderLogUtilsMockedStatic = mockStatic(TradeOrderLogUtils.class);
    }

    @AfterEach
    public void tearDown() {
        tradeOrderLogUtilsMockedStatic.close();
    }

    @ParameterizedTest
    @NullSource // 绑定的支付单编号尚未回写
    @ValueSource(longs = PAY_ORDER_ID) // 绑定的是其它支付单
    public void testUpdateOrderPaid_unpaidBindingMismatch(Long boundPayOrderId) {
        // mock 数据：未支付的订单
        mockOrder(TradeOrderStatusEnum.UNPAID, false, boundPayOrderId);

        // 调用，并断言异常：在查询支付单前失败
        assertServiceException(() -> tradeOrderUpdateService.updateOrderPaid(ORDER_ID, OTHER_PAY_ORDER_ID),
                ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        assertNoSideEffect();
    }

    @Test
    public void testUpdateOrderPaid_success() {
        // mock 数据：未支付的订单，绑定的支付单与回调一致
        TradeOrderDO order = mockOrder(TradeOrderStatusEnum.UNPAID, false, PAY_ORDER_ID);
        List<TradeOrderItemDO> orderItems = Collections.singletonList(new TradeOrderItemDO().setId(ORDER_ITEM_ID));
        // mock 方法
        when(payOrderApi.getOrder(PAY_ORDER_ID)).thenReturn(buildPayOrder());
        when(tradeOrderMapper.updateByIdAndStatus(eq(ORDER_ID), eq(TradeOrderStatusEnum.UNPAID.getStatus()),
                argThat(update -> Objects.equals(update.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus())
                        && Boolean.TRUE.equals(update.getPayStatus()) && update.getPayTime() != null
                        && Objects.equals(update.getPayChannelCode(), PAY_CHANNEL_CODE)))).thenReturn(1);
        when(tradeOrderItemMapper.selectListByOrderId(ORDER_ID)).thenReturn(orderItems);

        // 调用
        tradeOrderUpdateService.updateOrderPaid(ORDER_ID, PAY_ORDER_ID);
        // 断言：执行后置处理；记录订单日志
        verify(tradeOrderHandler).afterPayOrder(order, orderItems);
        tradeOrderLogUtilsMockedStatic.verify(() -> TradeOrderLogUtils.setOrderInfo(ORDER_ID,
                TradeOrderStatusEnum.UNPAID.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus()));
        tradeOrderLogUtilsMockedStatic.verify(() -> TradeOrderLogUtils.setUserInfo(ORDER_USER_ID,
                UserTypeEnum.MEMBER.getValue()));
    }

    @Test
    public void testUpdateOrderPaid_paidDuplicate() {
        // mock 数据：已支付的订单
        mockOrder(TradeOrderStatusEnum.UNDELIVERED, true, PAY_ORDER_ID);

        // 调用：重复回调，直接返回
        tradeOrderUpdateService.updateOrderPaid(ORDER_ID, PAY_ORDER_ID);
        // 断言
        assertNoSideEffect();
    }

    @Test
    public void testUpdateOrderPaid_paidBindingMismatch() {
        // mock 数据：已支付的订单
        mockOrder(TradeOrderStatusEnum.UNDELIVERED, true, PAY_ORDER_ID);

        // 调用，并断言异常
        assertServiceException(() -> tradeOrderUpdateService.updateOrderPaid(ORDER_ID, OTHER_PAY_ORDER_ID),
                ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        assertNoSideEffect();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPayOrders")
    public void testUpdateOrderPaid_payOrderInvalid(String name, UnaryOperator<PayOrderRespDTO> payOrderModifier,
                                                    ErrorCode errorCode) {
        // mock 数据：未支付的订单，绑定的支付单与回调一致
        mockOrder(TradeOrderStatusEnum.UNPAID, false, PAY_ORDER_ID);
        // mock 方法
        when(payOrderApi.getOrder(PAY_ORDER_ID)).thenReturn(payOrderModifier.apply(buildPayOrder()));

        // 调用，并断言异常：绑定一致时，原有的支付单校验仍然执行
        assertServiceException(() -> tradeOrderUpdateService.updateOrderPaid(ORDER_ID, PAY_ORDER_ID), errorCode);
        verify(tradeOrderMapper).selectById(ORDER_ID);
        verifyNoMoreInteractions(tradeOrderMapper);
        verifyNoInteractions(tradeOrderItemMapper, tradeOrderHandler);
        tradeOrderLogUtilsMockedStatic.verifyNoInteractions();
    }

    private static Stream<Arguments> invalidPayOrders() {
        return Stream.of(
                Arguments.of("支付单不存在", (UnaryOperator<PayOrderRespDTO>) payOrder -> null, ORDER_NOT_FOUND),
                Arguments.of("支付单未支付", (UnaryOperator<PayOrderRespDTO>) payOrder -> payOrder
                        .setStatus(PayOrderStatusEnum.WAITING.getStatus()), ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS),
                Arguments.of("支付金额不一致", (UnaryOperator<PayOrderRespDTO>) payOrder -> payOrder
                        .setPrice(ORDER_PAY_PRICE - 1), ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH),
                Arguments.of("商户订单编号不一致", (UnaryOperator<PayOrderRespDTO>) payOrder -> payOrder
                        .setMerchantOrderId(String.valueOf(ORDER_ID + 1)), ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR));
    }

    private TradeOrderDO mockOrder(TradeOrderStatusEnum status, boolean payStatus, Long payOrderId) {
        TradeOrderDO order = new TradeOrderDO().setId(ORDER_ID).setUserId(ORDER_USER_ID)
                .setStatus(status.getStatus()).setPayStatus(payStatus).setPayOrderId(payOrderId)
                .setPayPrice(ORDER_PAY_PRICE);
        when(tradeOrderMapper.selectById(ORDER_ID)).thenReturn(order);
        return order;
    }

    private static PayOrderRespDTO buildPayOrder() {
        return new PayOrderRespDTO().setId(PAY_ORDER_ID).setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setPrice(ORDER_PAY_PRICE).setMerchantOrderId(String.valueOf(ORDER_ID)).setChannelCode(PAY_CHANNEL_CODE);
    }

    /**
     * 断言：只查询了订单，没有查询支付单、更新订单、执行后置处理或记录日志
     */
    private void assertNoSideEffect() {
        verify(tradeOrderMapper).selectById(ORDER_ID);
        verifyNoMoreInteractions(tradeOrderMapper);
        verifyNoInteractions(payOrderApi, tradeOrderItemMapper, tradeOrderHandler);
        tradeOrderLogUtilsMockedStatic.verifyNoInteractions();
    }

}
