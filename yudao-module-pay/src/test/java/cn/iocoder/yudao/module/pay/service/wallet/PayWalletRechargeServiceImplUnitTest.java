package cn.iocoder.yudao.module.pay.service.wallet;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargeDO;
import cn.iocoder.yudao.module.pay.dal.mysql.wallet.PayWalletRechargeMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link PayWalletRechargeServiceImpl} 的单元测试类
 *
 * 重点覆盖 {@link PayWalletRechargeServiceImpl#updateWalletRechargerPaid(Long, Long)}：回调传入的支付单编号，
 * 必须等于充值记录绑定的支付单编号，避免其它支付应用中，相同商户订单编号、金额的支付单被用于串单
 *
 * 以及 {@link PayWalletRechargeServiceImpl#updateWalletRechargeRefunded(Long, String, Long)}：回调传入的退款单编号，
 * 必须等于充值记录绑定的退款单编号；已是退款终态的重复回调直接返回；先基于【未退款】状态更新退款字段成功，才操作一次钱包
 *
 * @author 芋道源码
 */
public class PayWalletRechargeServiceImplUnitTest extends BaseMockitoUnitTest {

    private static final long RECHARGE_ID = 41L;
    private static final long WALLET_ID = 1L;
    /**
     * 充值记录绑定的支付单编号
     */
    private static final long PAY_ORDER_ID = 100L;
    /**
     * 其它支付单编号，例如说其它支付应用中，相同商户订单编号、金额的支付单
     */
    private static final long OTHER_PAY_ORDER_ID = 200L;
    private static final int PAY_PRICE = 500;
    private static final int TOTAL_PRICE = 600;
    private static final String CHANNEL_CODE = "wx_lite";
    /**
     * 充值记录绑定的退款单编号
     */
    private static final long PAY_REFUND_ID = 300L;
    /**
     * 其它退款单编号
     */
    private static final long OTHER_PAY_REFUND_ID = 400L;
    private static final String MERCHANT_REFUND_ID = RECHARGE_ID + "-refund";

    @InjectMocks
    private PayWalletRechargeServiceImpl walletRechargeService;

    @Mock
    private PayWalletRechargeMapper walletRechargeMapper;
    @Mock
    private PayWalletService payWalletService;
    @Mock
    private PayOrderService payOrderService;
    @Mock
    private PayRefundApi payRefundApi;

    private MockedStatic<SpringUtil> springUtilMockedStatic;

    @BeforeEach
    public void setUp() {
        // mock getSelf() 使用的 SpringUtil，在正常路径中按需 mock 返回值
        springUtilMockedStatic = mockStatic(SpringUtil.class);
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
    }

    @ParameterizedTest
    @NullSource // 绑定的支付单编号尚未回写
    @ValueSource(longs = PAY_ORDER_ID) // 绑定的是其它支付单
    public void testUpdateWalletRechargerPaid_unpaidBindingMismatch(Long boundPayOrderId) {
        // mock 数据：未支付的充值记录
        mockRecharge(false, boundPayOrderId);

        // 调用，并断言异常：在查询支付单前失败
        assertServiceException(() -> walletRechargeService.updateWalletRechargerPaid(RECHARGE_ID, OTHER_PAY_ORDER_ID),
                WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        assertNoSideEffect();
    }

    @Test
    public void testUpdateWalletRechargerPaid_success() {
        // mock 数据：未支付的充值记录，绑定的支付单与回调一致
        PayWalletRechargeDO recharge = mockRecharge(false, PAY_ORDER_ID);
        // mock 方法（支付单）
        when(payOrderService.getOrder(PAY_ORDER_ID)).thenReturn(buildPayOrder());
        // mock 方法（更新支付状态）
        when(walletRechargeMapper.updateByIdAndPaid(eq(RECHARGE_ID), eq(false), argThat(update ->
                Boolean.TRUE.equals(update.getPayStatus()) && update.getPayTime() != null
                        && Objects.equals(update.getPayChannelCode(), CHANNEL_CODE)))).thenReturn(1);
        // mock 方法（订阅消息，异步执行，这里只断言被调用）
        PayWalletRechargeServiceImpl self = mock(PayWalletRechargeServiceImpl.class);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(PayWalletRechargeServiceImpl.class)).thenReturn(self);

        // 调用
        walletRechargeService.updateWalletRechargerPaid(RECHARGE_ID, PAY_ORDER_ID);
        // 断言：增加钱包余额；发送订阅消息
        verify(payWalletService).addWalletBalance(WALLET_ID, String.valueOf(RECHARGE_ID),
                PayWalletBizTypeEnum.RECHARGE, TOTAL_PRICE);
        verify(self).sendWalletRechargerPaidMessage(PAY_ORDER_ID, recharge);
    }

    @Test
    public void testUpdateWalletRechargerPaid_paidDuplicate() {
        // mock 数据：已支付的充值记录
        mockRecharge(true, PAY_ORDER_ID);

        // 调用：重复回调，直接返回
        walletRechargeService.updateWalletRechargerPaid(RECHARGE_ID, PAY_ORDER_ID);
        // 断言
        assertNoSideEffect();
    }

    @Test
    public void testUpdateWalletRechargerPaid_paidBindingMismatch() {
        // mock 数据：已支付的充值记录
        mockRecharge(true, PAY_ORDER_ID);

        // 调用，并断言异常
        assertServiceException(() -> walletRechargeService.updateWalletRechargerPaid(RECHARGE_ID, OTHER_PAY_ORDER_ID),
                WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        assertNoSideEffect();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPayOrders")
    public void testUpdateWalletRechargerPaid_payOrderInvalid(String name, UnaryOperator<PayOrderDO> payOrderModifier,
                                                              ErrorCode errorCode) {
        // mock 数据：未支付的充值记录，绑定的支付单与回调一致
        mockRecharge(false, PAY_ORDER_ID);
        // mock 方法（支付单）
        when(payOrderService.getOrder(PAY_ORDER_ID)).thenReturn(payOrderModifier.apply(buildPayOrder()));

        // 调用，并断言异常：绑定一致时，原有的支付单校验仍然执行
        assertServiceException(() -> walletRechargeService.updateWalletRechargerPaid(RECHARGE_ID, PAY_ORDER_ID),
                errorCode);
        verify(walletRechargeMapper).selectById(RECHARGE_ID);
        verifyNoMoreInteractions(walletRechargeMapper);
        verifyNoInteractions(payWalletService);
        springUtilMockedStatic.verifyNoInteractions();
    }

    private static Stream<Arguments> invalidPayOrders() {
        return Stream.of(
                Arguments.of("支付单不存在", (UnaryOperator<PayOrderDO>) payOrder -> null,
                        PAY_ORDER_NOT_FOUND),
                Arguments.of("支付单未支付", (UnaryOperator<PayOrderDO>) payOrder -> payOrder
                        .setStatus(PayOrderStatusEnum.WAITING.getStatus()), WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_STATUS_NOT_SUCCESS),
                Arguments.of("支付金额不一致", (UnaryOperator<PayOrderDO>) payOrder -> payOrder
                        .setPrice(PAY_PRICE - 1), WALLET_RECHARGE_UPDATE_PAID_PAY_PRICE_NOT_MATCH),
                Arguments.of("商户订单编号不一致", (UnaryOperator<PayOrderDO>) payOrder -> payOrder
                        .setMerchantOrderId(String.valueOf(RECHARGE_ID + 1)), WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR));
    }

    // ========== updateWalletRechargeRefunded ==========

    @ParameterizedTest
    @EnumSource(value = PayRefundStatusEnum.class, names = {"SUCCESS", "FAILURE"})
    public void testUpdateWalletRechargeRefunded_refundedDuplicate(PayRefundStatusEnum refundStatus) {
        // mock 数据：已是退款终态的充值记录
        mockRefundRecharge(refundStatus, PAY_REFUND_ID);

        // 调用：重复回调，直接返回
        walletRechargeService.updateWalletRechargeRefunded(RECHARGE_ID, MERCHANT_REFUND_ID, PAY_REFUND_ID);
        // 断言：不查询退款单、不更新充值记录、不操作钱包
        assertNoRefundSideEffect();
    }

    @ParameterizedTest
    @EnumSource(value = PayRefundStatusEnum.class, names = {"SUCCESS", "FAILURE"})
    public void testUpdateWalletRechargeRefunded_refundedBindingMismatch(PayRefundStatusEnum refundStatus) {
        // mock 数据：已是退款终态的充值记录
        mockRefundRecharge(refundStatus, PAY_REFUND_ID);

        // 调用，并断言异常
        assertServiceException(() -> walletRechargeService.updateWalletRechargeRefunded(
                RECHARGE_ID, MERCHANT_REFUND_ID, OTHER_PAY_REFUND_ID), WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        assertNoRefundSideEffect();
    }

    @ParameterizedTest
    @NullSource // 绑定的退款单编号尚未回写
    @ValueSource(longs = PAY_REFUND_ID) // 绑定的是其它退款单
    public void testUpdateWalletRechargeRefunded_waitingBindingMismatch(Long boundPayRefundId) {
        // mock 数据：未退款的充值记录
        mockRefundRecharge(PayRefundStatusEnum.WAITING, boundPayRefundId);

        // 调用，并断言异常：在查询退款单前失败
        assertServiceException(() -> walletRechargeService.updateWalletRechargeRefunded(
                RECHARGE_ID, MERCHANT_REFUND_ID, OTHER_PAY_REFUND_ID), WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        assertNoRefundSideEffect();
    }

    @Test
    public void testUpdateWalletRechargeRefunded_success() {
        // mock 数据：未退款的充值记录
        mockRefundRecharge(PayRefundStatusEnum.WAITING, PAY_REFUND_ID);
        // mock 方法（退款单）
        LocalDateTime successTime = LocalDateTime.of(2026, 10, 28, 12, 0);
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(buildPayRefund(PayRefundStatusEnum.SUCCESS)
                .setSuccessTime(successTime));
        // mock 方法（更新退款状态）
        when(walletRechargeMapper.updateByIdAndRefunded(eq(RECHARGE_ID), eq(PayRefundStatusEnum.WAITING.getStatus()),
                argThat(update -> Objects.equals(update.getId(), RECHARGE_ID)
                        && Objects.equals(update.getRefundStatus(), PayRefundStatusEnum.SUCCESS.getStatus())
                        && Objects.equals(update.getRefundTime(), successTime)
                        && Objects.equals(update.getRefundTotalPrice(), TOTAL_PRICE)
                        && Objects.equals(update.getRefundPayPrice(), PAY_PRICE)
                        && Objects.equals(update.getRefundBonusPrice(), TOTAL_PRICE - PAY_PRICE)))).thenReturn(1);

        // 调用
        walletRechargeService.updateWalletRechargeRefunded(RECHARGE_ID, MERCHANT_REFUND_ID, PAY_REFUND_ID);
        // 断言：先认领退款状态，再扣减钱包余额，且只扣减一次
        InOrder inOrder = inOrder(walletRechargeMapper, payWalletService);
        inOrder.verify(walletRechargeMapper).updateByIdAndRefunded(eq(RECHARGE_ID),
                eq(PayRefundStatusEnum.WAITING.getStatus()), any(PayWalletRechargeDO.class));
        inOrder.verify(payWalletService).reduceWalletBalance(WALLET_ID, RECHARGE_ID,
                PayWalletBizTypeEnum.RECHARGE_REFUND, TOTAL_PRICE);
        verifyNoMoreInteractions(payWalletService);
    }

    @Test
    public void testUpdateWalletRechargeRefunded_failure() {
        // mock 数据：未退款的充值记录
        mockRefundRecharge(PayRefundStatusEnum.WAITING, PAY_REFUND_ID);
        // mock 方法（退款单）
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(buildPayRefund(PayRefundStatusEnum.FAILURE));
        // mock 方法（更新退款状态）
        when(walletRechargeMapper.updateByIdAndRefunded(eq(RECHARGE_ID), eq(PayRefundStatusEnum.WAITING.getStatus()),
                argThat(update -> Objects.equals(update.getId(), RECHARGE_ID)
                        && Objects.equals(update.getRefundStatus(), PayRefundStatusEnum.FAILURE.getStatus())
                        && update.getRefundTime() == null && update.getRefundTotalPrice() == null
                        && update.getRefundPayPrice() == null && update.getRefundBonusPrice() == null))).thenReturn(1);

        // 调用
        walletRechargeService.updateWalletRechargeRefunded(RECHARGE_ID, MERCHANT_REFUND_ID, PAY_REFUND_ID);
        // 断言：先认领退款状态，再解冻余额，且只解冻一次
        InOrder inOrder = inOrder(walletRechargeMapper, payWalletService);
        inOrder.verify(walletRechargeMapper).updateByIdAndRefunded(eq(RECHARGE_ID),
                eq(PayRefundStatusEnum.WAITING.getStatus()), any(PayWalletRechargeDO.class));
        inOrder.verify(payWalletService).unfreezePrice(WALLET_ID, TOTAL_PRICE);
        verifyNoMoreInteractions(payWalletService);
    }

    @ParameterizedTest
    @EnumSource(value = PayRefundStatusEnum.class, names = {"SUCCESS", "FAILURE"})
    public void testUpdateWalletRechargeRefunded_updateStatusFail(PayRefundStatusEnum payRefundStatus) {
        // mock 数据：未退款的充值记录
        mockRefundRecharge(PayRefundStatusEnum.WAITING, PAY_REFUND_ID);
        // mock 方法（退款单）
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(buildPayRefund(payRefundStatus));
        // mock 方法（更新退款状态失败，例如说被并发的重复回调处理）
        when(walletRechargeMapper.updateByIdAndRefunded(eq(RECHARGE_ID), eq(PayRefundStatusEnum.WAITING.getStatus()),
                any(PayWalletRechargeDO.class))).thenReturn(0);

        // 调用
        walletRechargeService.updateWalletRechargeRefunded(RECHARGE_ID, MERCHANT_REFUND_ID, PAY_REFUND_ID);
        // 断言：不操作钱包
        verify(walletRechargeMapper).updateByIdAndRefunded(eq(RECHARGE_ID),
                eq(PayRefundStatusEnum.WAITING.getStatus()), any(PayWalletRechargeDO.class));
        verifyNoInteractions(payWalletService);
    }

    @Test
    public void testUpdateWalletRechargeRefunded_refundNotFinished() {
        // mock 数据：未退款的充值记录
        mockRefundRecharge(PayRefundStatusEnum.WAITING, PAY_REFUND_ID);
        // mock 方法（退款单未结束）
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(buildPayRefund(PayRefundStatusEnum.WAITING));

        // 调用
        walletRechargeService.updateWalletRechargeRefunded(RECHARGE_ID, MERCHANT_REFUND_ID, PAY_REFUND_ID);
        // 断言：不更新退款状态、不操作钱包
        verify(walletRechargeMapper).selectById(RECHARGE_ID);
        verifyNoMoreInteractions(walletRechargeMapper);
        verifyNoInteractions(payWalletService);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPayRefunds")
    public void testUpdateWalletRechargeRefunded_payRefundInvalid(String name,
                                                                  UnaryOperator<PayRefundRespDTO> payRefundModifier,
                                                                  ErrorCode errorCode) {
        // mock 数据：未退款的充值记录，绑定的退款单与回调一致
        mockRefundRecharge(PayRefundStatusEnum.WAITING, PAY_REFUND_ID);
        // mock 方法（退款单）
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(
                payRefundModifier.apply(buildPayRefund(PayRefundStatusEnum.SUCCESS)));

        // 调用，并断言异常：绑定一致时，原有的退款单校验仍然执行
        assertServiceException(() -> walletRechargeService.updateWalletRechargeRefunded(
                RECHARGE_ID, MERCHANT_REFUND_ID, PAY_REFUND_ID), errorCode);
        verify(walletRechargeMapper).selectById(RECHARGE_ID);
        verifyNoMoreInteractions(walletRechargeMapper);
        verifyNoInteractions(payWalletService);
    }

    private static Stream<Arguments> invalidPayRefunds() {
        return Stream.of(
                Arguments.of("退款单不存在", (UnaryOperator<PayRefundRespDTO>) payRefund -> null,
                        WALLET_RECHARGE_REFUND_FAIL_REFUND_NOT_FOUND),
                Arguments.of("退款金额不一致", (UnaryOperator<PayRefundRespDTO>) payRefund -> payRefund
                        .setRefundPrice(PAY_PRICE - 1), WALLET_RECHARGE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH),
                Arguments.of("商户退款单编号不一致", (UnaryOperator<PayRefundRespDTO>) payRefund -> payRefund
                        .setMerchantRefundId((RECHARGE_ID + 1) + "-refund"), WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR));
    }

    private PayWalletRechargeDO mockRecharge(boolean payStatus, Long payOrderId) {
        PayWalletRechargeDO recharge = new PayWalletRechargeDO().setId(RECHARGE_ID).setWalletId(WALLET_ID)
                .setPayStatus(payStatus).setPayOrderId(payOrderId)
                .setPayPrice(PAY_PRICE).setBonusPrice(TOTAL_PRICE - PAY_PRICE).setTotalPrice(TOTAL_PRICE);
        when(walletRechargeMapper.selectById(RECHARGE_ID)).thenReturn(recharge);
        return recharge;
    }

    private static PayOrderDO buildPayOrder() {
        return new PayOrderDO().setId(PAY_ORDER_ID).setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setPrice(PAY_PRICE).setMerchantOrderId(String.valueOf(RECHARGE_ID)).setChannelCode(CHANNEL_CODE);
    }

    private void mockRefundRecharge(PayRefundStatusEnum refundStatus, Long payRefundId) {
        PayWalletRechargeDO recharge = new PayWalletRechargeDO().setId(RECHARGE_ID).setWalletId(WALLET_ID)
                .setPayStatus(true).setPayOrderId(PAY_ORDER_ID)
                .setPayPrice(PAY_PRICE).setBonusPrice(TOTAL_PRICE - PAY_PRICE).setTotalPrice(TOTAL_PRICE)
                .setPayRefundId(payRefundId).setRefundStatus(refundStatus.getStatus());
        when(walletRechargeMapper.selectById(RECHARGE_ID)).thenReturn(recharge);
    }

    private static PayRefundRespDTO buildPayRefund(PayRefundStatusEnum status) {
        return new PayRefundRespDTO().setId(PAY_REFUND_ID).setStatus(status.getStatus())
                .setRefundPrice(PAY_PRICE).setMerchantOrderId(String.valueOf(RECHARGE_ID))
                .setMerchantRefundId(MERCHANT_REFUND_ID);
    }

    /**
     * 断言：只查询了充值记录，没有查询支付单、写入充值记录、增加余额或发送消息
     */
    private void assertNoSideEffect() {
        verify(walletRechargeMapper).selectById(RECHARGE_ID);
        verifyNoMoreInteractions(walletRechargeMapper);
        verifyNoInteractions(payOrderService, payWalletService);
        springUtilMockedStatic.verifyNoInteractions();
    }

    /**
     * 断言：只查询了充值记录，没有查询退款单、写入充值记录或操作钱包
     */
    private void assertNoRefundSideEffect() {
        verify(walletRechargeMapper).selectById(RECHARGE_ID);
        verifyNoMoreInteractions(walletRechargeMapper);
        verifyNoInteractions(payRefundApi, payWalletService);
    }

}
