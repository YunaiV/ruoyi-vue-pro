package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.http.Method;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;

import static cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum.CLOSED;
import static cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum.WAITING;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link  AlipayPcPayClient} 单元测试
 *
 * @author jason
 */
public class AlipayPcPayClientTest extends AbstractAlipayClientTest {

    @InjectMocks
    private AlipayPcPayClient client = new AlipayPcPayClient(randomLongId(), config);

    @Override
    @BeforeEach
    public void setUp() {
        setClient(client);
    }

    @Test
    @DisplayName("支付宝 PC 网站支付 URL Display Mode 下单成功")
    public void test_unified_order_url_display_mode_success() throws AlipayApiException {
        // 准备返回对象
        String notifyUrl = randomURL();
        AlipayTradePagePayResponse response = randomPojo(AlipayTradePagePayResponse.class, o -> o.setSubCode(""));
        // mock
        when(defaultAlipayClient.pageExecute(argThat((ArgumentMatcher<AlipayTradePagePayRequest>) request -> true),
                eq(Method.GET.name()))).thenReturn(response);
        // 准备请求参数
        String outTradeNo = randomString();
        Integer price = randomInteger();
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo, price);
        // 设置  displayMode 为 null.
        reqDTO.setDisplayMode(null);

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(PayOrderDisplayModeEnum.URL.getMode(), resp.getDisplayMode());
        assertEquals(outTradeNo, resp.getOutTradeNo());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝 PC 网站支付 FORM Display Mode 下单成功")
    public void test_unified_order_form_display_mode_success() throws AlipayApiException {
        // 准备返回对象
        String notifyUrl = randomURL();
        AlipayTradePagePayResponse response = randomPojo(AlipayTradePagePayResponse.class, o -> o.setSubCode(""));
        // mock
        when(defaultAlipayClient.pageExecute(argThat((ArgumentMatcher<AlipayTradePagePayRequest>) request -> true),
                eq(Method.POST.name()))).thenReturn(response);
        // 准备请求参数
        String outTradeNo = randomString();
        Integer price = randomInteger();
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo, price);
        reqDTO.setDisplayMode(PayOrderDisplayModeEnum.FORM.getMode());

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(PayOrderDisplayModeEnum.FORM.getMode(), resp.getDisplayMode());
        assertEquals(outTradeNo, resp.getOutTradeNo());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝 PC 网站支付,渠道返回失败")
    public void test_unified_order_channel_failed() throws AlipayApiException {
        // 准备响应对象
        String subCode = randomString();
        String subMsg = randomString();
        AlipayTradePagePayResponse response = randomPojo(AlipayTradePagePayResponse.class, o -> {
            o.setSubCode(subCode);
            o.setSubMsg(subMsg);
        });
        // mock
        when(defaultAlipayClient.pageExecute(argThat((ArgumentMatcher<AlipayTradePagePayRequest>) request -> true),
                eq(Method.GET.name()))).thenReturn(response);
        // 准备请求参数
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setDisplayMode(PayOrderDisplayModeEnum.URL.getMode());

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(CLOSED.getStatus(), resp.getStatus());
        assertEquals(subCode, resp.getChannelErrorCode());
        assertEquals(subMsg, resp.getChannelErrorMsg());
        assertSame(response, resp.getRawData());
    }
}
