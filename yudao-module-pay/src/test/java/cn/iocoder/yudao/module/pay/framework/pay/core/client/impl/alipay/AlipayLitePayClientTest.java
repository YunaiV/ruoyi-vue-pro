package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.alipay;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.enums.PayOrderDisplayModeEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;

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
 */
public class AlipayLitePayClientTest extends AbstractAlipayClientTest {

    @InjectMocks
    private AlipayLitePayClient client = new AlipayLitePayClient(randomLongId(), config);

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
        reqDTO.setChannelExtras(Map.of(AlipayLitePayClient.BUYER_OPEN_ID_KEY, buyerOpenId));

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);

        assertEquals(WAITING.getStatus(), resp.getStatus());
        assertEquals(PayOrderDisplayModeEnum.APP.getMode(), resp.getDisplayMode());
        assertEquals(tradeNo, resp.getDisplayContent());
        assertSame(response, resp.getRawData());
    }

    @Test
    @DisplayName("支付宝小程序支付：buyer_id 下单成功")
    public void testUnifiedOrder_buyerId() throws AlipayApiException {
        String buyerId = "2088123456789012";
        AlipayTradeCreateResponse response = randomPojo(AlipayTradeCreateResponse.class, o -> o.setSubCode(""));
        when(defaultAlipayClient.execute(argThat(assertRequest(config.getAppId(), buyerId, null))))
                .thenReturn(response);
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Map.of(AlipayLitePayClient.BUYER_ID_KEY, buyerId));

        assertEquals(WAITING.getStatus(), client.unifiedOrder(reqDTO).getStatus());
    }

    @Test
    @DisplayName("支付宝小程序支付：兼容 openid 参数")
    public void testUnifiedOrder_openidAlias() throws AlipayApiException {
        String buyerOpenId = randomString();
        AlipayTradeCreateResponse response = randomPojo(AlipayTradeCreateResponse.class, o -> o.setSubCode(""));
        when(defaultAlipayClient.execute(argThat(assertRequest(config.getAppId(), null, buyerOpenId))))
                .thenReturn(response);
        PayOrderUnifiedReqDTO reqDTO = buildOrderUnifiedReqDTO(randomURL(), randomString(), randomInteger());
        reqDTO.setChannelExtras(Map.of("openid", buyerOpenId));

        assertEquals(WAITING.getStatus(), client.unifiedOrder(reqDTO).getStatus());
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
        reqDTO.setChannelExtras(Map.of(AlipayLitePayClient.BUYER_OPEN_ID_KEY, buyerOpenId));

        assertEquals(WAITING.getStatus(), client.unifiedOrder(reqDTO).getStatus());
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
        reqDTO.setChannelExtras(Map.of(AlipayLitePayClient.BUYER_OPEN_ID_KEY, randomString()));

        PayOrderRespDTO resp = client.unifiedOrder(reqDTO);

        assertEquals(CLOSED.getStatus(), resp.getStatus());
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
