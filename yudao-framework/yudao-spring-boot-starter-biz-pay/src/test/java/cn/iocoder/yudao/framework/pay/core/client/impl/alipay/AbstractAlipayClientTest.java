package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import cn.iocoder.yudao.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.DefaultSigner;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

import javax.validation.ConstraintViolationException;
import java.util.Date;

import static cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig.MODE_PUBLIC_KEY;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * @author jason
 */
public abstract class AbstractAlipayClientTest extends BaseMockitoUnitTest {

    private final String privateKey = randomString();

    protected AlipayPayClientConfig config = randomPojo(AlipayPayClientConfig.class, t -> {
        t.setServerUrl(randomURL());
        t.setPrivateKey(privateKey);
        t.setMode(MODE_PUBLIC_KEY);
        t.setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT);
        t.setAppCertContent("");
        t.setAlipayPublicCertContent("");
        t.setRootCertContent("");
    });

    @Mock
    protected DefaultAlipayClient defaultAlipayClient;

    private AbstractAlipayPayClient client;

    /**
     * 子类需要实现该方法. 设置 client 的具体实现
     */
    @BeforeEach
    public abstract void setUp();

    public void setClient(AbstractAlipayPayClient client) {
        this.client = client;
    }

    @Test
    @DisplayName("支付宝 Client 初始化")
    public void test_do_init() {
        client.doInit();
        DefaultAlipayClient realClient = (DefaultAlipayClient) ReflectUtil.getFieldValue(client, "client");
        assertNotSame(defaultAlipayClient, realClient);
        assertInstanceOf(DefaultSigner.class, realClient.getSigner());
        assertEquals(privateKey, ((DefaultSigner) realClient.getSigner()).getPrivateKey());
    }

    @Test
    @DisplayName("支付宝 Client 统一退款成功")
    public void test_unified_refund_success() throws AlipayApiException {
        // 准备返回对象
        String notifyUrl = randomURL();
        Date refundTime = randomDate();
        String outRefundNo = randomString();
        String outTradeNo = randomString();
        Integer refundAmount = randomInteger();
        AlipayTradeRefundResponse response = randomPojo(AlipayTradeRefundResponse.class, o -> {
            o.setSubCode("");
            o.setGmtRefundPay(refundTime);
        });
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradeRefundRequest>) request -> {
            assertInstanceOf(AlipayTradeRefundModel.class, request.getBizModel());
            AlipayTradeRefundModel bizModel = (AlipayTradeRefundModel) request.getBizModel();
            assertEquals(outRefundNo, bizModel.getOutRequestNo());
            assertEquals(outTradeNo, bizModel.getOutTradeNo());
            assertEquals(String.valueOf(refundAmount / 100.0), bizModel.getRefundAmount());
            return true;
        }))).thenReturn(response);
        // 准备请求参数
        PayRefundUnifiedReqDTO refundReqDTO = randomPojo(PayRefundUnifiedReqDTO.class, o -> {
            o.setOutRefundNo(outRefundNo);
            o.setOutTradeNo(outTradeNo);
            o.setNotifyUrl(notifyUrl);
            o.setRefundPrice(refundAmount);
        });
        PayRefundRespDTO resp = client.unifiedRefund(refundReqDTO);
        // 断言
        assertEquals(PayRefundStatusRespEnum.SUCCESS.getStatus(), resp.getStatus());
        assertNull(resp.getChannelRefundNo());
        assertEquals(LocalDateTimeUtil.of(refundTime), resp.getSuccessTime());
        assertEquals(outRefundNo, resp.getOutRefundNo());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝 Client 统一退款,渠道返回失败")
    public void test_unified_refund_channel_failed() throws AlipayApiException {
        // 准备返回对象
        String notifyUrl = randomURL();
        String subCode = randomString();
        String subMsg = randomString();
        AlipayTradeRefundResponse response = randomPojo(AlipayTradeRefundResponse.class, o -> {
            o.setSubCode(subCode);
            o.setSubMsg(subMsg);
        });
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradeRefundRequest>) request -> {
            assertInstanceOf(AlipayTradeRefundModel.class, request.getBizModel());
            return true;
        }))).thenReturn(response);
        // 准备请求参数
        String outRefundNo = randomString();
        String outTradeNo = randomString();
        PayRefundUnifiedReqDTO refundReqDTO = randomPojo(PayRefundUnifiedReqDTO.class, o -> {
            o.setOutRefundNo(outRefundNo);
            o.setOutTradeNo(outTradeNo);
            o.setNotifyUrl(notifyUrl);
        });
        PayRefundRespDTO resp = client.unifiedRefund(refundReqDTO);
        // 断言
        assertEquals(PayRefundStatusRespEnum.FAILURE.getStatus(), resp.getStatus());
        assertNull(resp.getChannelRefundNo());
        assertEquals(subCode, resp.getChannelErrorCode());
        assertEquals(subMsg, resp.getChannelErrorMsg());
        assertNull(resp.getSuccessTime());
        assertEquals(outRefundNo, resp.getOutRefundNo());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝 Client 统一退款,参数校验不通过")
    public void test_unified_refund_param_validate() {
        // 准备请求参数
        String notifyUrl = randomURL();
        PayRefundUnifiedReqDTO refundReqDTO = randomPojo(PayRefundUnifiedReqDTO.class, o -> {
            o.setOutTradeNo("");
            o.setNotifyUrl(notifyUrl);
        });
        // 断言
        assertThrows(ConstraintViolationException.class, () -> client.unifiedRefund(refundReqDTO));
    }

    @Test
    @DisplayName("支付宝 Client 统一退款,抛出业务异常")
    public void test_unified_refund_throw_service_exception() throws AlipayApiException {
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradeRefundRequest>) request -> true)))
                .thenThrow(ServiceExceptionUtil.exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR));
        // 准备请求参数
        String notifyUrl = randomURL();
        PayRefundUnifiedReqDTO refundReqDTO = randomPojo(PayRefundUnifiedReqDTO.class, o -> o.setNotifyUrl(notifyUrl));
        // 断言
        assertThrows(ServiceException.class, () -> client.unifiedRefund(refundReqDTO));
    }

    @Test
    @DisplayName("支付宝 Client 统一退款,抛出系统异常")
    public void test_unified_refund_throw_pay_exception() throws AlipayApiException {
        // mock
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradeRefundRequest>) request -> true)))
                .thenThrow(new RuntimeException("系统异常"));
        // 准备请求参数
        String notifyUrl = randomURL();
        PayRefundUnifiedReqDTO refundReqDTO = randomPojo(PayRefundUnifiedReqDTO.class, o -> o.setNotifyUrl(notifyUrl));
        // 断言
        assertThrows(PayException.class, () -> client.unifiedRefund(refundReqDTO));
    }

    @Test
    @DisplayName("支付宝 Client 统一下单, 参数校验不通过")
    public void test_unified_order_param_validate() {
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

    protected PayOrderUnifiedReqDTO buildOrderUnifiedReqDTO(String notifyUrl, String outTradeNo, Integer price) {
        return randomPojo(PayOrderUnifiedReqDTO.class, o -> {
            o.setOutTradeNo(outTradeNo);
            o.setNotifyUrl(notifyUrl);
            o.setPrice(price);
            o.setSubject(RandomUtil.randomString(32));
            o.setBody(RandomUtil.randomString(32));
        });
    }
}
