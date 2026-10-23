package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.exception.PayClientException;
import com.github.binarywang.wxpay.bean.request.WxPayRefundQueryV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryV3Result;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
    private WxPayService wxPayService;
    private TransferService transferService;

    @BeforeEach
    public void setUp() {
        WxPayClientConfig config = new WxPayClientConfig();
        config.setApiVersion(API_VERSION_V3);
        config.setAppId("wx-app-id");
        client = new WxLitePayClient(1L, config);
        wxPayService = mock(WxPayService.class);
        transferService = mock(TransferService.class);
        lenient().when(wxPayService.getTransferService()).thenReturn(transferService); // 退款相关的测试不使用
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

    @Test // 发起微信退款发生 IO 异常：退款可能已受理，抛出异常，不误判为退款失败
    public void testUnifiedRefund_ioException() throws Exception {
        // mock 方法
        when(wxPayService.refundV3(any(WxPayRefundV3Request.class))).thenThrow(new WxPayException("timeout",
                new IOException("read timeout")));

        // 调用，并断言异常
        assertThrows(PayClientException.class, () -> client.unifiedRefund(buildRefundUnifiedReqDTO()));
    }

    @Test // 发起微信退款返回系统错误：退款可能已受理，返回结果未知，不误判为退款失败
    public void testUnifiedRefund_systemError() throws Exception {
        // mock 方法
        when(wxPayService.refundV3(any(WxPayRefundV3Request.class))).thenThrow(WxPayException.newBuilder()
                .errCode("SYSTEM_ERROR").errCodeDes("系统错误").build());

        // 调用
        PayRefundRespDTO resp = client.unifiedRefund(buildRefundUnifiedReqDTO());
        // 断言
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), resp.getStatus());
        assertNull(resp.getChannelRefundNo());
        assertEquals("R001", resp.getOutRefundNo());
    }

    @Test // 发起微信退款返回业务错误（例如说：余额不足）：明确的退款失败
    public void testUnifiedRefund_businessError() throws Exception {
        // mock 方法
        when(wxPayService.refundV3(any(WxPayRefundV3Request.class))).thenThrow(WxPayException.newBuilder()
                .errCode("NOT_ENOUGH").errCodeDes("余额不足").build());

        // 调用
        PayRefundRespDTO resp = client.unifiedRefund(buildRefundUnifiedReqDTO());
        // 断言
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), resp.getStatus());
        assertEquals("NOT_ENOUGH", resp.getChannelErrorCode());
        assertEquals("余额不足", resp.getChannelErrorMsg());
    }

    @Test // 查询微信退款单不存在（例如说：退款请求未受理、或者尚未生成退款单）：返回结果未知，不作为退款失败
    public void testGetRefund_notExists() throws Exception {
        // mock 方法
        when(wxPayService.refundQueryV3(any(WxPayRefundQueryV3Request.class))).thenThrow(WxPayException.newBuilder()
                .errCode("RESOURCE_NOT_EXISTS").errCodeDes("退款单不存在").build());

        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), resp.getStatus());
        assertNull(resp.getChannelRefundNo());
        assertEquals("R001", resp.getOutRefundNo());
    }

    @Test // 查询微信退款单处理中：渠道已受理，返回渠道退款单号
    public void testGetRefund_processing() throws Exception {
        // mock 方法
        WxPayRefundQueryV3Result result = new WxPayRefundQueryV3Result();
        result.setStatus("PROCESSING");
        result.setRefundId("WX_REFUND_001");
        when(wxPayService.refundQueryV3(any(WxPayRefundQueryV3Request.class))).thenReturn(result);

        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.WAITING.getStatus(), resp.getStatus());
        assertEquals("WX_REFUND_001", resp.getChannelRefundNo());
    }

    @Test // 查询微信退款单已关闭：明确的退款失败
    public void testGetRefund_closed() throws Exception {
        // mock 方法
        WxPayRefundQueryV3Result result = new WxPayRefundQueryV3Result();
        result.setStatus("CLOSED");
        when(wxPayService.refundQueryV3(any(WxPayRefundQueryV3Request.class))).thenReturn(result);

        // 调用
        PayRefundRespDTO resp = client.getRefund("P001", "R001");
        // 断言
        assertEquals(PayRefundStatusEnum.FAILURE.getStatus(), resp.getStatus());
    }

    @Test // 查询微信退款单发生其它错误（例如说：系统错误）：抛出异常，不作为退款失败
    public void testGetRefund_systemError() throws Exception {
        // mock 方法
        when(wxPayService.refundQueryV3(any(WxPayRefundQueryV3Request.class))).thenThrow(WxPayException.newBuilder()
                .errCode("SYSTEM_ERROR").errCodeDes("系统错误").build());

        // 调用，并断言异常
        assertThrows(PayClientException.class, () -> client.getRefund("P001", "R001"));
    }

    private static PayRefundUnifiedReqDTO buildRefundUnifiedReqDTO() {
        return new PayRefundUnifiedReqDTO().setOutTradeNo("P001").setOutRefundNo("R001")
                .setReason("测试退款").setPayPrice(100).setRefundPrice(10)
                .setNotifyUrl("http://127.0.0.1/refund");
    }

    private static PayTransferUnifiedReqDTO buildTransferUnifiedReqDTO() {
        return new PayTransferUnifiedReqDTO().setUserIp("127.0.0.1").setOutTransferNo("T001")
                .setPrice(100).setSubject("佣金提现").setUserAccount("openid")
                .setNotifyUrl("http://127.0.0.1/transfer")
                .setChannelExtras(Map.of("sceneId", "1000", "sceneReportInfos", "[]"));
    }

}
