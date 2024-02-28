package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static cn.iocoder.yudao.framework.pay.core.client.impl.weixin.AbstractWxPayClient.formatDateV3;

/**
 * {@link WxNativePayClient} 的集成测试，用于快速调试微信扫码支付
 *
 * @author 芋道源码
 */
@Disabled
public class WxNativePayClientIntegrationTest {

    @Test
    public void testPayV3() throws WxPayException {
        // 创建 config 配置
        WxPayConfig config = buildWxPayConfigV3();
        // 创建 WxPayService 客户端
        WxPayService client = new WxPayServiceImpl();
        client.setConfig(config);

        // 执行发起支付
        WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request()
                .setOutTradeNo(String.valueOf(System.currentTimeMillis()))
                .setDescription("测试支付-body")
                .setAmount(new WxPayUnifiedOrderV3Request.Amount().setTotal(1)) // 单位分
                .setTimeExpire(formatDateV3(LocalDateTimeUtils.addTime(Duration.ofMinutes(2))))
                .setSceneInfo(new WxPayUnifiedOrderV3Request.SceneInfo().setPayerClientIp("127.0.0.1"))
                .setNotifyUrl("http://127.0.0.1:48080");
        System.out.println("========= request ==========");
        System.out.println(JsonUtils.toJsonPrettyString(request));
        String response = client.createOrderV3(TradeTypeEnum.NATIVE, request);
        System.out.println("========= response ==========");
        System.out.println(JsonUtils.toJsonPrettyString(response));
    }

    @Test
    public void testRefundV3() throws WxPayException {
        // 创建 config 配置
        WxPayConfig config = buildWxPayConfigV3();
        // 创建 WxPayService 客户端
        WxPayService client = new WxPayServiceImpl();
        client.setConfig(config);

        // 执行发起退款
        WxPayRefundV3Request request = new WxPayRefundV3Request()
                .setOutTradeNo("1689545729695")
                .setOutRefundNo(String.valueOf(System.currentTimeMillis()))
                .setAmount(new WxPayRefundV3Request.Amount().setTotal(1).setRefund(1).setCurrency("CNY"))
                .setReason("就是想退了");
        System.out.println("========= request ==========");
        System.out.println(JsonUtils.toJsonPrettyString(request));
        WxPayRefundV3Result response = client.refundV3(request);
        System.out.println("========= response ==========");
        System.out.println(JsonUtils.toJsonPrettyString(response));
    }

    private WxPayConfig buildWxPayConfigV3() {
        WxPayConfig config = new WxPayConfig();
        config.setAppId("wx62056c0d5e8db250");
        config.setMchId("1545083881");
        config.setApiV3Key("459arNsYHl1mgkiO6H9ZH5KkhFXSxaA4");
//        config.setCertSerialNo(serialNo);
        config.setPrivateCertPath("/Users/yunai/Downloads/wx_pay/apiclient_cert.pem");
        config.setPrivateKeyPath("/Users/yunai/Downloads/wx_pay/apiclient_key.pem");
        return config;
    }

}
