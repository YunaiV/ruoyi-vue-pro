package cn.iocoder.yudao.module.trade.service.aftersale;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.mysql.aftersale.AfterSaleMapper;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleOperateTypeEnum;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleStatusEnum;
import cn.iocoder.yudao.module.trade.framework.aftersale.core.utils.AfterSaleLogUtils;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderUpdateService;
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

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AfterSaleServiceImpl} 的单元测试类
 *
 * 重点覆盖 {@link AfterSaleServiceImpl#updateAfterSaleRefunded(Long, Long, Long)}：回调传入的退款单编号，
 * 必须等于售后单绑定的退款单编号，避免其它支付应用、或重新退款前的退款单被用于更新状态
 *
 * @author 芋道源码
 */
public class AfterSaleServiceImplUnitTest extends BaseMockitoUnitTest {

    private static final long AFTER_SALE_ID = 5L;
    private static final long ORDER_ID = 1L;
    private static final long ORDER_ITEM_ID = 50L;
    /**
     * 售后单绑定的退款单编号
     */
    private static final long PAY_REFUND_ID = 500L;
    /**
     * 其它退款单编号，例如说其它支付应用中，相同商户退款编号、金额的退款单
     */
    private static final long OTHER_PAY_REFUND_ID = 600L;
    private static final int REFUND_PRICE = 300;

    @InjectMocks
    private AfterSaleServiceImpl afterSaleService;

    @Mock
    private AfterSaleMapper tradeAfterSaleMapper;
    @Mock
    private PayRefundApi payRefundApi;
    @Mock
    private TradeOrderUpdateService tradeOrderUpdateService;

    private MockedStatic<AfterSaleLogUtils> afterSaleLogUtilsMockedStatic;

    @BeforeEach
    public void setUp() {
        // mock 售后操作日志的上下文，用于断言是否记录日志
        afterSaleLogUtilsMockedStatic = mockStatic(AfterSaleLogUtils.class);
    }

    @AfterEach
    public void tearDown() {
        afterSaleLogUtilsMockedStatic.close();
    }

    @ParameterizedTest
    @NullSource // 绑定的退款单编号尚未回写
    @ValueSource(longs = PAY_REFUND_ID) // 绑定的是其它退款单
    public void testUpdateAfterSaleRefunded_bindingMismatch(Long boundPayRefundId) {
        // mock 数据：待退款的售后单
        mockAfterSale(AfterSaleStatusEnum.WAIT_REFUND, boundPayRefundId);

        // 调用，并断言异常：在查询退款单前失败
        assertServiceException(() -> afterSaleService.updateAfterSaleRefunded(AFTER_SALE_ID, ORDER_ID, OTHER_PAY_REFUND_ID),
                AFTER_SALE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        assertNoSideEffect();
    }

    @Test
    public void testUpdateAfterSaleRefunded_refundSuccess() {
        // mock 数据：待退款的售后单，绑定的退款单与回调一致
        mockAfterSale(AfterSaleStatusEnum.WAIT_REFUND, PAY_REFUND_ID);
        // mock 方法
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(buildPayRefund());
        when(tradeAfterSaleMapper.updateByIdAndStatus(eq(AFTER_SALE_ID), eq(AfterSaleStatusEnum.WAIT_REFUND.getStatus()),
                argThat(update -> Objects.equals(update.getStatus(), AfterSaleStatusEnum.COMPLETE.getStatus())
                        && update.getRefundTime() != null))).thenReturn(1);

        // 调用
        afterSaleService.updateAfterSaleRefunded(AFTER_SALE_ID, ORDER_ID, PAY_REFUND_ID);
        // 断言：更新订单项的售后状态；记录售后日志
        verify(tradeOrderUpdateService).updateOrderItemWhenAfterSaleSuccess(ORDER_ITEM_ID, REFUND_PRICE);
        afterSaleLogUtilsMockedStatic.verify(() -> AfterSaleLogUtils.setAfterSaleInfo(AFTER_SALE_ID,
                AfterSaleStatusEnum.WAIT_REFUND.getStatus(), AfterSaleStatusEnum.COMPLETE.getStatus()));
    }

    @Test
    public void testUpdateAfterSaleRefunded_refundFailure() {
        // mock 数据：待退款的售后单，绑定的退款单与回调一致
        mockAfterSale(AfterSaleStatusEnum.WAIT_REFUND, PAY_REFUND_ID);
        // mock 方法
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(buildPayRefund()
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus()));

        // 调用
        afterSaleService.updateAfterSaleRefunded(AFTER_SALE_ID, ORDER_ID, PAY_REFUND_ID);
        // 断言：售后单状态不变，只记录退款失败日志
        verify(tradeAfterSaleMapper).selectById(AFTER_SALE_ID);
        verifyNoMoreInteractions(tradeAfterSaleMapper);
        verifyNoInteractions(tradeOrderUpdateService);
        afterSaleLogUtilsMockedStatic.verify(() -> AfterSaleLogUtils.setAfterSaleOperateType(
                AfterSaleOperateTypeEnum.SYSTEM_REFUND_FAIL));
        afterSaleLogUtilsMockedStatic.verify(() -> AfterSaleLogUtils.setAfterSaleInfo(AFTER_SALE_ID,
                AfterSaleStatusEnum.WAIT_REFUND.getStatus(), AfterSaleStatusEnum.WAIT_REFUND.getStatus()));
    }

    @Test
    public void testUpdateAfterSaleRefunded_completedRepeat() {
        // mock 数据：已完成的售后单，绑定的退款单与回调一致
        mockAfterSale(AfterSaleStatusEnum.COMPLETE, PAY_REFUND_ID);

        // 调用，并断言异常：重复回调仍是原有的状态错误
        assertServiceException(() -> afterSaleService.updateAfterSaleRefunded(AFTER_SALE_ID, ORDER_ID, PAY_REFUND_ID),
                AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND);
        assertNoSideEffect();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPayRefunds")
    public void testUpdateAfterSaleRefunded_payRefundInvalid(String name, UnaryOperator<PayRefundRespDTO> payRefundModifier,
                                                             ErrorCode errorCode) {
        // mock 数据：待退款的售后单，绑定的退款单与回调一致
        mockAfterSale(AfterSaleStatusEnum.WAIT_REFUND, PAY_REFUND_ID);
        // mock 方法
        when(payRefundApi.getRefund(PAY_REFUND_ID)).thenReturn(payRefundModifier.apply(buildPayRefund()));

        // 调用，并断言异常：绑定一致时，原有的退款单校验仍然执行
        assertServiceException(() -> afterSaleService.updateAfterSaleRefunded(AFTER_SALE_ID, ORDER_ID, PAY_REFUND_ID),
                errorCode);
        verify(tradeAfterSaleMapper).selectById(AFTER_SALE_ID);
        verifyNoMoreInteractions(tradeAfterSaleMapper);
        verifyNoInteractions(tradeOrderUpdateService);
        afterSaleLogUtilsMockedStatic.verifyNoInteractions();
    }

    private static Stream<Arguments> invalidPayRefunds() {
        return Stream.of(
                Arguments.of("退款单不存在", (UnaryOperator<PayRefundRespDTO>) payRefund -> null,
                        AFTER_SALE_REFUND_FAIL_REFUND_NOT_FOUND),
                Arguments.of("退款单无退款结果", (UnaryOperator<PayRefundRespDTO>) payRefund -> payRefund
                        .setStatus(PayRefundStatusEnum.WAITING.getStatus()), AFTER_SALE_REFUND_FAIL_REFUND_NOT_SUCCESS_OR_FAILURE),
                Arguments.of("退款金额不一致", (UnaryOperator<PayRefundRespDTO>) payRefund -> payRefund
                        .setRefundPrice(REFUND_PRICE - 1), AFTER_SALE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH),
                Arguments.of("商户退款编号不一致", (UnaryOperator<PayRefundRespDTO>) payRefund -> payRefund
                        .setMerchantRefundId(String.valueOf(AFTER_SALE_ID + 1)), AFTER_SALE_REFUND_FAIL_REFUND_ORDER_ID_ERROR));
    }

    private void mockAfterSale(AfterSaleStatusEnum status, Long payRefundId) {
        AfterSaleDO afterSale = new AfterSaleDO().setId(AFTER_SALE_ID).setStatus(status.getStatus())
                .setOrderId(ORDER_ID).setOrderItemId(ORDER_ITEM_ID)
                .setPayRefundId(payRefundId).setRefundPrice(REFUND_PRICE);
        when(tradeAfterSaleMapper.selectById(AFTER_SALE_ID)).thenReturn(afterSale);
    }

    private static PayRefundRespDTO buildPayRefund() {
        return new PayRefundRespDTO().setId(PAY_REFUND_ID).setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setRefundPrice(REFUND_PRICE).setMerchantRefundId(String.valueOf(AFTER_SALE_ID));
    }

    /**
     * 断言：只查询了售后单，没有查询退款单、更新售后单、更新订单项或记录日志
     */
    private void assertNoSideEffect() {
        verify(tradeAfterSaleMapper).selectById(AFTER_SALE_ID);
        verifyNoMoreInteractions(tradeAfterSaleMapper);
        verifyNoInteractions(payRefundApi, tradeOrderUpdateService);
        afterSaleLogUtilsMockedStatic.verifyNoInteractions();
    }

}
