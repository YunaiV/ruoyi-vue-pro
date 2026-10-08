package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.exception.PayClientException;
import com.github.binarywang.wxpay.bean.transfer.TransferBillsGetResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.TransferService;
import com.github.binarywang.wxpay.service.WxPayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin.WxPayClientConfig.API_VERSION_V3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AbstractWxPayClient} 的单元测试类
 *
 * @author 芋道源码
 */
public class AbstractWxPayClientTest extends BaseMockitoUnitTest {

    private WxLitePayClient client;
    private TransferService transferService;

    @BeforeEach
    public void setUp() {
        WxPayClientConfig config = new WxPayClientConfig();
        config.setApiVersion(API_VERSION_V3);
        config.setAppId("wx-app-id");
        client = new WxLitePayClient(1L, config);
        WxPayService wxPayService = mock(WxPayService.class);
        transferService = mock(TransferService.class);
        when(wxPayService.getTransferService()).thenReturn(transferService);
        ReflectionTestUtils.setField(client, "client", wxPayService);
    }

    @Test // 查询微信转账关闭时保留渠道转账单号，用于关闭单重试时换号
    public void testGetTransfer_closed() throws Exception {
        TransferBillsGetResult result = new TransferBillsGetResult();
        result.setState("CANCELLED");
        result.setOutBillNo("T001");
        result.setTransferBillNo("WX_TRANSFER_001");
        result.setFailReason("用户超时未确认");
        when(transferService.getBillsByOutBillNo("T001")).thenReturn(result);

        PayTransferRespDTO resp = client.getTransfer("T001");

        assertEquals(PayTransferStatusEnum.CLOSED.getStatus(), resp.getStatus());
        assertEquals("WX_TRANSFER_001", resp.getChannelTransferNo());
    }

    @Test // 微信取消中的转账仍处于处理中
    public void testGetTransfer_canceling() throws Exception {
        TransferBillsGetResult result = new TransferBillsGetResult();
        result.setState("CANCELING");
        result.setOutBillNo("T001");
        result.setTransferBillNo("WX_TRANSFER_001");
        when(transferService.getBillsByOutBillNo("T001")).thenReturn(result);

        PayTransferRespDTO resp = client.getTransfer("T001");

        assertEquals(PayTransferStatusEnum.PROCESSING.getStatus(), resp.getStatus());
    }

    @Test // 发起微信转账发生 IO 异常时保留未知状态，不误判为关闭
    public void testUnifiedTransfer_ioException() throws Exception {
        when(transferService.transferBills(any())).thenThrow(new WxPayException("timeout",
                new IOException("connect timeout")));

        assertThrows(PayClientException.class, () -> client.unifiedTransfer(buildTransferUnifiedReqDTO()));
        verify(transferService).transferBills(any());
    }

    private static PayTransferUnifiedReqDTO buildTransferUnifiedReqDTO() {
        return new PayTransferUnifiedReqDTO().setUserIp("127.0.0.1").setOutTransferNo("T001")
                .setPrice(100).setSubject("佣金提现").setUserAccount("openid")
                .setNotifyUrl("http://127.0.0.1/transfer")
                .setChannelExtras(Map.of("sceneId", "1000", "sceneReportInfos", "[]"));
    }

}
