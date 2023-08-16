package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.validation.ConstraintViolationException;

import static cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig.MODE_PUBLIC_KEY;
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
public class AlipayQrPayClientTest extends BaseMockitoUnitTest {

    private final AlipayPayClientConfig config = randomPojo(AlipayPayClientConfig.class, t -> {
        t.setServerUrl(randomURL());
        t.setMode(MODE_PUBLIC_KEY);
        t.setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT);
        t.setAppCertContent("");
        t.setAlipayPublicCertContent("");
        t.setRootCertContent("");
    });

    @InjectMocks
    AlipayQrPayClient client = new AlipayQrPayClient(randomLongId(), config);

    @Mock
    private DefaultAlipayClient defaultAlipayClient;

    @Test
    public void testDoInit() {
        client.doInit();
        assertNotSame(defaultAlipayClient, ReflectUtil.getFieldValue(client, "defaultAlipayClient"));
    }

    @Test
    public void testUnifiedOrderSuccess() throws AlipayApiException {
        // 准备返回对象
        String notifyUrl = randomURL();
        String qrCode = randomString();
        AlipayTradePrecreateResponse response = randomPojo(AlipayTradePrecreateResponse.class, o -> {
            o.setQrCode(qrCode);
            o.setSubCode("");
        });
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request -> {
            assertEquals(notifyUrl, request.getNotifyUrl());
            return true;
        }))).thenReturn(response);
        // 准备请求参数
        String outTradeNo = randomString();
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo);

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(PayOrderDisplayModeEnum.QR_CODE.getMode(), resp.getDisplayMode());
        assertEquals(outTradeNo, resp.getOutTradeNo());
        assertEquals(qrCode, resp.getDisplayContent());
        assertSame(response, resp.getRawData());
    }

    @Test
    public void testUnifiedOrderChannelFailed() throws AlipayApiException {
        String notifyUrl = randomURL();
        String subCode = randomString();
        String subMsg = randomString();
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
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo);

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(CLOSED.getStatus(), resp.getStatus());
        assertEquals(subCode, resp.getChannelErrorCode());
        assertEquals(subMsg, resp.getChannelErrorMsg());
        assertSame(response, resp.getRawData());
    }

    @Test
    public void test_unifiedOrder_throw_pay_exception() throws AlipayApiException {
        // 准备请求参数
        String outTradeNo = randomString();
        String notifyUrl = randomURL();
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request -> {
            assertEquals(notifyUrl, request.getNotifyUrl());
            return true;
        }))).thenThrow(new RuntimeException("系统异常"));
        // 准备请求参数
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo);
        // 断言
        assertThrows(PayException.class, () -> client.unifiedOrder(reqDTO));
    }

    @Test
    public void test_unifiedOrder_throw_service_exception() throws AlipayApiException {
        // 准备请求参数
        String outTradeNo = randomString();
        String notifyUrl = randomURL();
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request -> {
            assertEquals(notifyUrl, request.getNotifyUrl());
            return true;
        }))).thenThrow(ServiceExceptionUtil.exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR));
        // 准备请求参数
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo);
        // 断言
        assertThrows(ServiceException.class, () -> client.unifiedOrder(reqDTO));
    }

    @Test
    public void test_unifiedOrder_param_validate() {
        // 准备请求参数
        String outTradeNo = randomString();
        String notifyUrl = randomURL();
        PayOrderUnifiedReqDTO reqDTO = randomPojo(PayOrderUnifiedReqDTO.class, o -> {
            o.setOutTradeNo(outTradeNo);
            o.setNotifyUrl(notifyUrl);
        });
        // 断言
        assertThrows(ConstraintViolationException.class, () -> client.unifiedOrder(reqDTO));
    }

    private PayOrderUnifiedReqDTO buildOrderUnifiedReqDTO(String notifyUrl, String outTradeNo) {
        return randomPojo(PayOrderUnifiedReqDTO.class, o -> {
            o.setOutTradeNo(outTradeNo);
            o.setNotifyUrl(notifyUrl);
            o.setSubject(RandomUtil.randomString(32));
            o.setBody(RandomUtil.randomString(32));
        });
    }
}
