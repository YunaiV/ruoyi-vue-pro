package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;

import static cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum.CLOSED;
import static cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum.WAITING;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * {@link AlipayQrPayClient} 单元测试
 *
 * @author jason
 */
public class AlipayQrPayClientTest extends AbstractAlipayClientTest {

    @InjectMocks
    private AlipayQrPayClient client = new AlipayQrPayClient(randomLongId(), config);

    @BeforeEach
    public void setUp() {
        setClient(client);
    }

    @Test
    @DisplayName("支付宝扫描支付：下单成功")
    public void testUnifiedOrder_success() throws AlipayApiException {
        // mock 方法
        String notifyUrl = randomURL();
        String qrCode = randomString();
        Integer price = randomInteger();
        AlipayTradePrecreateResponse response = randomPojo(AlipayTradePrecreateResponse.class, o -> {
            o.setQrCode(qrCode);
            o.setSubCode("");
        });
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request -> {
            assertEquals(notifyUrl, request.getNotifyUrl());
            return true;
        }))).thenReturn(response);
        // 准备请求参数
        String outTradeNo = randomString();
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo, price);

        // 调用
        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(outTradeNo, resp.getOutTradeNo());
        assertNull(resp.getChannelOrderNo());
        assertNull(resp.getChannelUserId());
        assertNull(resp.getSuccessTime());
        assertEquals(PayOrderDisplayModeEnum.QR_CODE.getMode(), resp.getDisplayMode());
        assertEquals(response.getQrCode(), resp.getDisplayContent());
        assertSame(response, resp.getRawData());
        assertNull(resp.getChannelErrorCode());
        assertNull(resp.getChannelErrorMsg());
    }

    @Test
    @DisplayName("支付宝扫描支付：渠道返回失败")
    public void testUnifiedOrder_channelFailed() throws AlipayApiException {
        // mock 方法
        String notifyUrl = randomURL();
        String subCode = randomString();
        String subMsg = randomString();
        Integer price = randomInteger();
        AlipayTradePrecreateResponse response = randomPojo(AlipayTradePrecreateResponse.class, o -> {
            o.setSubCode(subCode);
            o.setSubMsg(subMsg);
        });
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request -> {
            assertEquals(notifyUrl, request.getNotifyUrl());
            return true;
        }))).thenReturn(response);
        // 准备请求参数
        String outTradeNo = randomString();
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo, price);

        // 调用
        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(CLOSED.getStatus(), resp.getStatus());
        assertEquals(outTradeNo, resp.getOutTradeNo());
        assertNull(resp.getChannelOrderNo());
        assertNull(resp.getChannelUserId());
        assertNull(resp.getSuccessTime());
        assertNull(resp.getDisplayMode());
        assertNull(resp.getDisplayContent());
        assertSame(response, resp.getRawData());
        assertEquals(subCode, resp.getChannelErrorCode());
        assertEquals(subMsg, resp.getChannelErrorMsg());
    }

    @Test
    @DisplayName("支付宝扫描支付, 抛出系统异常")
    public void testUnifiedOrder_throwPayException() throws AlipayApiException {
        // mock 方法
        String outTradeNo = randomString();
        String notifyUrl = randomURL();
        Integer price = randomInteger();
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request -> {
            assertEquals(notifyUrl, request.getNotifyUrl());
            return true;
        }))).thenThrow(new RuntimeException("系统异常"));
        // 准备请求参数
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo,price);

        // 调用，并断言
        assertThrows(PayException.class, () -> client.unifiedOrder(reqDTO));
    }

    @Test
    @DisplayName("支付宝 Client 统一下单：抛出业务异常")
    public void testUnifiedOrder_throwServiceException() throws AlipayApiException {
        // mock 方法
        String outTradeNo = randomString();
        String notifyUrl = randomURL();
        Integer price = randomInteger();
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request -> {
            assertEquals(notifyUrl, request.getNotifyUrl());
            return true;
        }))).thenThrow(ServiceExceptionUtil.exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR));
        // 准备请求参数
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo, price);

        // 调用，并断言
        assertThrows(ServiceException.class, () -> client.unifiedOrder(reqDTO));
    }

}
