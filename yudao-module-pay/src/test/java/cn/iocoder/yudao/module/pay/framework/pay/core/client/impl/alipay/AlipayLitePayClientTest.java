package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.enums.PayOrderDisplayModeEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.exception.PayClientException;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum.CLOSED;
import static cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum.WAITING;
import static cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay.AlipayPayClientConfig.MODE_CERTIFICATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * {@link AlipayLitePayClient} 单元测试
 *
 * @author graypxl
 */
public class AlipayLitePayClientTest extends AbstractAlipayClientTest {

    @InjectMocks
    private AlipayLitePayClient client = new AlipayLitePayClient(randomLongId(), config);

    @Override
    @BeforeEach
    public void setUp() {
        setClient(client);
    }

    @Test
    @DisplayName("支付宝小程序支付：buyer_open_id 下单成功")
    public void testUnifiedOrder_buyerOpenId() throws AlipayApiException {
        String buyerOpenId = "060d4dhFytu7VF5haNiDacG7b-n5toPeevhdLa07Kz9ass8";
        String tradeNo = randomString();
        AlipayTradeCreateResponse response = randomPojo(AlipayTradeCreateResponse.class, o -> {
            o.setSubCode("");
            o.setTradeNo(tradeNo);
        });
        when(defaultAlipayClient.execute(argThat(assertRequest(config.getAppId(), null, buyerOpenId))))
                .thenReturn(response);
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Collections.singletonMap(AlipayLitePayClient.BUYER_OPEN_ID_KEY, buyerOpenId));

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);

        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(reqDTO.getOutTradeNo(), resp.getOutTradeNo());
        assertNull(resp.getChannelOrderNo());
        assertNull(resp.getChannelUserId());
        assertNull(resp.getSuccessTime());
        assertEquals(PayOrderDisplayModeEnum.APP.getMode(), resp.getDisplayMode());
        assertEquals(tradeNo, resp.getDisplayContent());
        assertSame(response, resp.getRawData());
        assertNull(resp.getChannelErrorCode());
        assertNull(resp.getChannelErrorMsg());
    }

    @Test
    @DisplayName("支付宝小程序支付：buyer_id 下单成功")
    public void testUnifiedOrder_buyerId() throws AlipayApiException {
        String buyerId = "2088123456789012";
        AlipayTradeCreateResponse response = randomPojo(AlipayTradeCreateResponse.class, o -> o.setSubCode(""));
        when(defaultAlipayClient.execute(argThat(assertRequest(config.getAppId(), buyerId, null))))
                .thenReturn(response);
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Collections.singletonMap(AlipayLitePayClient.BUYER_ID_KEY, buyerId));

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);

        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(reqDTO.getOutTradeNo(), resp.getOutTradeNo());
        assertEquals(PayOrderDisplayModeEnum.APP.getMode(), resp.getDisplayMode());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝小程序支付：兼容 openid 参数")
    public void testUnifiedOrder_openidAlias() throws AlipayApiException {
        String buyerOpenId = randomString();
        AlipayTradeCreateResponse response = randomPojo(AlipayTradeCreateResponse.class, o -> o.setSubCode(""));
        when(defaultAlipayClient.execute(argThat(assertRequest(config.getAppId(), null, buyerOpenId))))
                .thenReturn(response);
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Collections.singletonMap("openid", buyerOpenId));

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);

        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(reqDTO.getOutTradeNo(), resp.getOutTradeNo());
        assertEquals(PayOrderDisplayModeEnum.APP.getMode(), resp.getDisplayMode());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝小程序支付：证书模式下单成功")
    public void testUnifiedOrder_certificateMode() throws AlipayApiException {
        config.setMode(MODE_CERTIFICATE);
        String buyerOpenId = randomString();
        AlipayTradeCreateResponse response = randomPojo(AlipayTradeCreateResponse.class, o -> o.setSubCode(""));
        when(defaultAlipayClient.certificateExecute(argThat(assertRequest(config.getAppId(), null, buyerOpenId))))
                .thenReturn(response);
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Collections.singletonMap(AlipayLitePayClient.BUYER_OPEN_ID_KEY, buyerOpenId));

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);

        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(reqDTO.getOutTradeNo(), resp.getOutTradeNo());
        assertEquals(PayOrderDisplayModeEnum.APP.getMode(), resp.getDisplayMode());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝小程序支付：渠道返回失败")
    public void testUnifiedOrder_channelFailed() throws AlipayApiException {
        String subCode = randomString();
        String subMsg = randomString();
        AlipayTradeCreateResponse response = randomPojo(AlipayTradeCreateResponse.class, o -> {
            o.setSubCode(subCode);
            o.setSubMsg(subMsg);
        });
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradeCreateRequest>) request -> true)))
                .thenReturn(response);
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Collections.singletonMap(AlipayLitePayClient.BUYER_OPEN_ID_KEY, randomString()));

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);

        assertEquals(CLOSED.getStatus(), resp.getStatus());
        assertEquals(reqDTO.getOutTradeNo(), resp.getOutTradeNo());
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
    @DisplayName("支付宝小程序支付：买家标识为空")
    public void testUnifiedOrder_buyerEmpty() {
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(new HashMap<>());

        assertThrows(ServiceException.class, () -> client.unifiedOrder(reqDTO));
    }

    @Test
    @DisplayName("支付宝小程序支付：抛出业务异常")
    public void testUnifiedOrder_throwServiceException() throws AlipayApiException {
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradeCreateRequest>) request -> true)))
                .thenThrow(ServiceExceptionUtil.exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR));
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Collections.singletonMap(AlipayLitePayClient.BUYER_OPEN_ID_KEY, randomString()));

        assertThrows(ServiceException.class, () -> client.unifiedOrder(reqDTO));
    }

    @Test
    @DisplayName("支付宝小程序支付：抛出系统异常")
    public void testUnifiedOrder_throwPayException() throws AlipayApiException {
        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradeCreateRequest>) request -> true)))
                .thenThrow(new RuntimeException("系统异常"));
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Collections.singletonMap(AlipayLitePayClient.BUYER_OPEN_ID_KEY, randomString()));

        assertThrows(PayClientException.class, () -> client.unifiedOrder(reqDTO));
    }

    private static ArgumentMatcher<AlipayTradeCreateRequest> assertRequest(String appId, String buyerId,
                                                                            String buyerOpenId) {
        return request -> {
            AlipayTradeCreateModel model = (AlipayTradeCreateModel) request.getBizModel();
            assertEquals("JSAPI_PAY", model.getProductCode());
            assertEquals(appId, model.getOpAppId());
            assertEquals(buyerId, model.getBuyerId());
            assertEquals(buyerOpenId, model.getBuyerOpenId());
            return true;
        };
    }

}
