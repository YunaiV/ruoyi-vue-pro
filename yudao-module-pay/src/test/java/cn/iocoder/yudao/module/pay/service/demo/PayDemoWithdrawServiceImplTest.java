package cn.iocoder.yudao.module.pay.service.demo;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoWithdrawDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoWithdrawMapper;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.demo.PayDemoWithdrawStatusEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.DEMO_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR;
import static org.mockito.Mockito.*;

/**
 * {@link PayDemoWithdrawServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class PayDemoWithdrawServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PayDemoWithdrawServiceImpl payDemoWithdrawService;

    @Mock
    private PayDemoWithdrawMapper demoTransferMapper;
    @Mock
    private PayTransferApi payTransferApi;

    @Test
    public void testUpdateDemoWithdrawTransferred_success() {
        // mock 数据
        when(demoTransferMapper.selectById(eq(1L))).thenReturn(buildWithdraw(PayDemoWithdrawStatusEnum.WAITING.getStatus()));
        when(payTransferApi.getTransfer(eq(100L))).thenReturn(buildTransfer(100L));

        // 调用
        payDemoWithdrawService.updateDemoWithdrawTransferred(1L, 100L);
        // 断言
        verify(demoTransferMapper).updateByIdAndStatus(eq(1L), eq(PayDemoWithdrawStatusEnum.WAITING.getStatus()),
                argThat(updateObj -> PayDemoWithdrawStatusEnum.SUCCESS.getStatus().equals(updateObj.getStatus())));
    }

    @Test
    public void testUpdateDemoWithdrawTransferred_repeatNotify() {
        // mock 数据：已结束，且转账单编号相同
        when(demoTransferMapper.selectById(eq(1L))).thenReturn(buildWithdraw(PayDemoWithdrawStatusEnum.SUCCESS.getStatus()));

        // 调用：重复通知，幂等返回
        payDemoWithdrawService.updateDemoWithdrawTransferred(1L, 100L);
        // 断言
        verifyNoInteractions(payTransferApi);
        verify(demoTransferMapper, never()).updateByIdAndStatus(anyLong(), anyInt(), any());
    }

    @Test
    public void testUpdateDemoWithdrawTransferred_waitingPayTransferIdNotBound() {
        // mock 数据：未结束；其它支付应用、或重新转账前，存在相同商户转账编号、金额、渠道的转账单
        when(demoTransferMapper.selectById(eq(1L))).thenReturn(buildWithdraw(PayDemoWithdrawStatusEnum.WAITING.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> payDemoWithdrawService.updateDemoWithdrawTransferred(1L, 200L),
                DEMO_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR);
        // 断言：不查询转账单，也不更新提现单
        verifyNoInteractions(payTransferApi);
        verify(demoTransferMapper, never()).updateByIdAndStatus(anyLong(), anyInt(), any());
    }

    private static PayDemoWithdrawDO buildWithdraw(Integer status) {
        return new PayDemoWithdrawDO().setId(1L).setSubject("提现").setPrice(10).setStatus(status)
                .setPayTransferId(100L).setTransferChannelCode(PayChannelEnum.WALLET.getCode());
    }

    private static PayTransferRespDTO buildTransfer(Long id) {
        return new PayTransferRespDTO().setId(id).setStatus(PayTransferStatusEnum.SUCCESS.getStatus())
                .setPrice(10).setMerchantTransferId("1").setChannelCode(PayChannelEnum.WALLET.getCode());
    }

}
