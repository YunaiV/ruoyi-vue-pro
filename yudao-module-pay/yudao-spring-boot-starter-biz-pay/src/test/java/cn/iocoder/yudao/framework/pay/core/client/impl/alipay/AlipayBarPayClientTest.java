package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.response.AlipayTradePayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum.CLOSED;
import static cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderStatusRespEnum.WAITING;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * {@link AlipayBarPayClient} 单元测试
 *
 * @author jason
 */
public class AlipayBarPayClientTest extends AbstractAlipayClientTest {

    @InjectMocks
    private AlipayBarPayClient client = new AlipayBarPayClient(randomLongId(), config);

    @Override
    @BeforeEach
    public void setUp() {
        setClient(client);
    }

    @Test
    @DisplayName("支付宝条码支付：非免密码支付下单成功")
    public void testUnifiedOrder_success() throws AlipayApiException {
        // mock 方法
        String outTradeNo = randomString();
        String notifyUrl = randomURL();
        Integer price = randomInteger();
        String authCode = randomString();
        AlipayTradePayResponse response = randomPojo(AlipayTradePayResponse.class, o -> o.setSubCode(""));
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePayRequest>) request -> {
            assertInstanceOf(AlipayTradePayModel.class, request.getBizModel());
            assertEquals(notifyUrl, request.getNotifyUrl());
            AlipayTradePayModel model = (AlipayTradePayModel) request.getBizModel();
            assertEquals(outTradeNo, model.getOutTradeNo());
            assertEquals(String.valueOf(price / 100.0), model.getTotalAmount());
            assertEquals(authCode, model.getAuthCode());
            return true;
        }))).thenReturn(response);
        // 准备请求参数
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(notifyUrl, outTradeNo, price);
        Map<String, String> extraParam = new HashMap<>();
        extraParam.put("auth_code", authCode);
        reqDTO.setChannelExtras(extraParam);

        // 调用方法
        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(outTradeNo, resp.getOutTradeNo());
        assertNull(resp.getChannelOrderNo());
        assertNull(resp.getChannelUserId());
        assertNull(resp.getSuccessTime());
        assertEquals(PayOrderDisplayModeEnum.BAR_CODE.getMode(), resp.getDisplayMode());
        assertEquals("", resp.getDisplayContent());
        assertSame(response, resp.getRawData());
        assertNull(resp.getChannelErrorCode());
        assertNull(resp.getChannelErrorMsg());
    }

    @Test
    @DisplayName("支付宝条码支付：免密码支付下单成功")
    public void testUnifiedOrder_code10000Success() throws AlipayApiException {
        // mock 方法
        String outTradeNo = randomString();
        String channelNo = randomString();
        String channelUserId = randomString();
        Date payTime = randomDate();
        AlipayTradePayResponse response = randomPojo(AlipayTradePayResponse.class, o -> {
            o.setSubCode("");
            o.setCode("10000");
            o.setOutTradeNo(outTradeNo);
            o.setTradeNo(channelNo);
            o.setBuyerUserId(channelUserId);
            o.setGmtPayment(payTime);
        });
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePayRequest>) request -> true)))
                .thenReturn(response);
        // 准备请求参数
        String authCode = randomString();
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), outTradeNo, randomInteger());
        Map<String, String> extraParam = new HashMap<>();
        extraParam.put("auth_code", authCode);
        reqDTO.setChannelExtras(extraParam);

        // 下单请求
        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);
        // 断言
        assertEquals(PayOrderStatusRespEnum.SUCCESS.getStatus(), resp.getStatus());
        assertEquals(outTradeNo, resp.getOutTradeNo());
        assertEquals(channelNo, resp.getChannelOrderNo());
        assertEquals(channelUserId, resp.getChannelUserId());
        assertEquals(LocalDateTimeUtil.of(payTime), resp.getSuccessTime());
        assertEquals(PayOrderDisplayModeEnum.BAR_CODE.getMode(), resp.getDisplayMode());
        assertEquals("", resp.getDisplayContent());
        assertSame(response, resp.getRawData());
        assertNull(resp.getChannelErrorCode());
        assertNull(resp.getChannelErrorMsg());
    }

    @Test
    @DisplayName("支付宝条码支付：没有传条码")
    public void testUnifiedOrder_emptyAuthCode() {
        // 准备参数
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());

        // 调用，并断言
        assertThrows(ServiceException.class, () -> client.unifiedOrder(reqDTO));
    }

    @Test
    @DisplayName("支付宝条码支付：渠道返回失败")
    public void test_unified_order_channel_failed() throws AlipayApiException {
        // mock 方法
        String subCode = randomString();
        String subMsg = randomString();
        AlipayTradePayResponse response = randomPojo(AlipayTradePayResponse.class, o -> {
            o.setSubCode(subCode);
            o.setSubMsg(subMsg);
        });
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePayRequest>) request -> true)))
                .thenReturn(response);
        // 准备请求参数
        String authCode = randomString();
        String outTradeNo = randomString();
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), outTradeNo, randomInteger());
        Map<String, String> extraParam = new HashMap<>();
        extraParam.put("auth_code", authCode);
        reqDTO.setChannelExtras(extraParam);

        // 调用方法
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

}
