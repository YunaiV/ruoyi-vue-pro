package cn.iocoder.yudao.module.trade.service.brokerage;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.dal.mysql.brokerage.BrokerageWithdrawMapper;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_TRANSFER_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link BrokerageWithdrawServiceImpl} 的单元测试类
 *
 * 重点覆盖 {@link BrokerageWithdrawServiceImpl#updateBrokerageWithdrawTransferred(Long, Long)}：回调传入的转账单编号，
 * 必须等于提现单绑定的转账单编号，避免其它支付应用中，相同商户转账编号、金额的转账单被用于更新状态
 *
 * @author 芋道源码
 */
public class BrokerageWithdrawServiceImplUnitTest extends BaseMockitoUnitTest {

    private static final long WITHDRAW_ID = 3L;
    /**
     * 提现单绑定的转账单编号
     */
    private static final long PAY_TRANSFER_ID = 300L;
    /**
     * 其它转账单编号，例如说其它支付应用中，相同商户转账编号、金额的转账单
     */
    private static final long OTHER_PAY_TRANSFER_ID = 400L;
    private static final int WITHDRAW_PRICE = 1000;
    private static final int WITHDRAW_FEE_PRICE = 100;
    private static final String TRANSFER_CHANNEL_CODE = "wx_lite";

    @InjectMocks
    private BrokerageWithdrawServiceImpl brokerageWithdrawService;

    @Mock
    private BrokerageWithdrawMapper brokerageWithdrawMapper;
    @Mock
    private PayTransferApi payTransferApi;

    @ParameterizedTest
    @NullSource // 绑定的转账单编号尚未回写
    @ValueSource(longs = PAY_TRANSFER_ID) // 绑定的是其它转账单
    public void testUpdateBrokerageWithdrawTransferred_unfinishedBindingMismatch(Long boundPayTransferId) {
        // mock 数据：未结束的提现单
        mockWithdraw(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS, boundPayTransferId);

        // 调用，并断言异常：在查询转账单前失败
        assertServiceException(() -> brokerageWithdrawService.updateBrokerageWithdrawTransferred(WITHDRAW_ID, OTHER_PAY_TRANSFER_ID),
                BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        assertNoSideEffect();
    }

    @ParameterizedTest
    @MethodSource("transferResults")
    public void testUpdateBrokerageWithdrawTransferred_success(PayTransferStatusEnum transferStatus,
                                                               BrokerageWithdrawStatusEnum expectedStatus) {
        // mock 数据：未结束的提现单，绑定的转账单与回调一致
        mockWithdraw(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS, PAY_TRANSFER_ID);
        // mock 方法
        when(payTransferApi.getTransfer(PAY_TRANSFER_ID)).thenReturn(buildPayTransfer().setStatus(transferStatus.getStatus()));

        // 调用
        brokerageWithdrawService.updateBrokerageWithdrawTransferred(WITHDRAW_ID, PAY_TRANSFER_ID);
        // 断言：转账成功时提现成功，转账关闭时提现失败
        verify(brokerageWithdrawMapper).updateByIdAndStatus(eq(WITHDRAW_ID), eq(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus()),
                argThat(update -> Objects.equals(update.getStatus(), expectedStatus.getStatus())));
    }

    private static Stream<Arguments> transferResults() {
        return Stream.of(
                Arguments.of(PayTransferStatusEnum.SUCCESS, BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS),
                Arguments.of(PayTransferStatusEnum.CLOSED, BrokerageWithdrawStatusEnum.WITHDRAW_FAIL));
    }

    @ParameterizedTest
    @EnumSource(value = BrokerageWithdrawStatusEnum.class, names = {"WITHDRAW_SUCCESS", "WITHDRAW_FAIL"})
    public void testUpdateBrokerageWithdrawTransferred_finishedDuplicate(BrokerageWithdrawStatusEnum status) {
        // mock 数据：已结束的提现单
        mockWithdraw(status, PAY_TRANSFER_ID);

        // 调用：重复回调，直接返回
        brokerageWithdrawService.updateBrokerageWithdrawTransferred(WITHDRAW_ID, PAY_TRANSFER_ID);
        // 断言
        assertNoSideEffect();
    }

    @Test
    public void testUpdateBrokerageWithdrawTransferred_finishedBindingMismatch() {
        // mock 数据：已结束的提现单
        mockWithdraw(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS, PAY_TRANSFER_ID);

        // 调用，并断言异常
        assertServiceException(() -> brokerageWithdrawService.updateBrokerageWithdrawTransferred(WITHDRAW_ID, OTHER_PAY_TRANSFER_ID),
                BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        assertNoSideEffect();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPayTransfers")
    public void testUpdateBrokerageWithdrawTransferred_payTransferInvalid(String name,
                                                                          UnaryOperator<PayTransferRespDTO> payTransferModifier,
                                                                          ErrorCode errorCode) {
        // mock 数据：未结束的提现单，绑定的转账单与回调一致
        mockWithdraw(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS, PAY_TRANSFER_ID);
        // mock 方法
        when(payTransferApi.getTransfer(PAY_TRANSFER_ID)).thenReturn(payTransferModifier.apply(buildPayTransfer()));

        // 调用，并断言异常：绑定一致时，原有的转账单校验仍然执行
        assertServiceException(() -> brokerageWithdrawService.updateBrokerageWithdrawTransferred(WITHDRAW_ID, PAY_TRANSFER_ID),
                errorCode);
        verify(brokerageWithdrawMapper).selectById(WITHDRAW_ID);
        verifyNoMoreInteractions(brokerageWithdrawMapper);
    }

    private static Stream<Arguments> invalidPayTransfers() {
        return Stream.of(
                Arguments.of("转账单不存在", (UnaryOperator<PayTransferRespDTO>) payTransfer -> null, PAY_TRANSFER_NOT_FOUND),
                Arguments.of("转账单未结束", (UnaryOperator<PayTransferRespDTO>) payTransfer -> payTransfer
                        .setStatus(PayTransferStatusEnum.PROCESSING.getStatus()),
                        BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_STATUS_NOT_SUCCESS_OR_CLOSED),
                Arguments.of("转账金额不一致", (UnaryOperator<PayTransferRespDTO>) payTransfer -> payTransfer
                        .setPrice(WITHDRAW_PRICE), BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_PRICE_NOT_MATCH),
                Arguments.of("商户转账编号不一致", (UnaryOperator<PayTransferRespDTO>) payTransfer -> payTransfer
                        .setMerchantTransferId(String.valueOf(WITHDRAW_ID + 1)), BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_MERCHANT_EXISTS),
                Arguments.of("转账渠道不一致", (UnaryOperator<PayTransferRespDTO>) payTransfer -> payTransfer
                        .setChannelCode("alipay_pc"), BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_CHANNEL_NOT_MATCH));
    }

    private void mockWithdraw(BrokerageWithdrawStatusEnum status, Long payTransferId) {
        BrokerageWithdrawDO withdraw = new BrokerageWithdrawDO().setId(WITHDRAW_ID).setStatus(status.getStatus())
                .setPayTransferId(payTransferId).setPrice(WITHDRAW_PRICE).setFeePrice(WITHDRAW_FEE_PRICE)
                .setTransferChannelCode(TRANSFER_CHANNEL_CODE);
        when(brokerageWithdrawMapper.selectById(WITHDRAW_ID)).thenReturn(withdraw);
    }

    private static PayTransferRespDTO buildPayTransfer() {
        return new PayTransferRespDTO().setId(PAY_TRANSFER_ID).setStatus(PayTransferStatusEnum.SUCCESS.getStatus())
                .setPrice(WITHDRAW_PRICE - WITHDRAW_FEE_PRICE).setMerchantTransferId(String.valueOf(WITHDRAW_ID))
                .setChannelCode(TRANSFER_CHANNEL_CODE);
    }

    /**
     * 断言：只查询了提现单，没有查询转账单或更新提现单
     */
    private void assertNoSideEffect() {
        verify(brokerageWithdrawMapper).selectById(WITHDRAW_ID);
        verifyNoMoreInteractions(brokerageWithdrawMapper);
        verifyNoInteractions(payTransferApi);
    }

}
