package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.github.binarywang.wxpay.bean.request.WxPayMicropayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayMicropayResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static cn.iocoder.yudao.framework.pay.core.client.impl.weixin.AbstractWxPayClient.formatDateV2;

/**
 * {@link WxBarPayClient} 的集成测试，用于快速调试微信条码支付
 *
 * @author 芋道源码
 */
@Disabled
public class WxBarPayClientIntegrationTest {

    @Test
    public void testPayV2() throws WxPayException {
        // 创建 config 配置
        WxPayConfig config = buildWxPayConfigV2();
        // 创建 WxPayService 客户端
        WxPayService client = new WxPayServiceImpl();
        client.setConfig(config);

        // 执行发起支付
        WxPayMicropayRequest request = WxPayMicropayRequest.newBuilder()
                .outTradeNo(String.valueOf(System.currentTimeMillis()))
                .body("测试支付-body")
                .detail("测试支付-detail")
                .totalFee(1) // 单位分
                .timeExpire(formatDateV2(LocalDateTimeUtils.addTime(Duration.ofMinutes(2))))
                .spbillCreateIp("127.0.0.1")
                .authCode("134298744426278497")
                .build();
        System.out.println("========= request ==========");
        System.out.println(JsonUtils.toJsonPrettyString(request));
        WxPayMicropayResult response = client.micropay(request);
        System.out.println("========= response ==========");
        System.out.println(JsonUtils.toJsonPrettyString(response));
    }

    @Test
    public void testRefundV2() throws WxPayException {
        // 创建 config 配置
        WxPayConfig config = buildWxPayConfigV2();
        // 创建 WxPayService 客户端
        WxPayService client = new WxPayServiceImpl();
        client.setConfig(config);

        // 执行发起退款
        WxPayRefundRequest request = new WxPayRefundRequest()
                .setOutTradeNo("1689545667276")
                .setOutRefundNo(String.valueOf(System.currentTimeMillis()))
                .setRefundFee(1)
                .setRefundDesc("就是想退了")
                .setTotalFee(1);
        System.out.println("========= request ==========");
        System.out.println(JsonUtils.toJsonPrettyString(request));
        WxPayRefundResult response = client.refund(request);
        System.out.println("========= response ==========");
        System.out.println(JsonUtils.toJsonPrettyString(response));
    }

    @Test
    public void testRefundV3() throws WxPayException {
        // 创建 config 配置
        WxPayConfig config = buildWxPayConfigV2();
        // 创建 WxPayService 客户端
        WxPayService client = new WxPayServiceImpl();
        client.setConfig(config);

        // 执行发起退款
        WxPayRefundV3Request request = new WxPayRefundV3Request()
                .setOutTradeNo("1689506325635")
                .setOutRefundNo(String.valueOf(System.currentTimeMillis()))
                .setAmount(new WxPayRefundV3Request.Amount().setTotal(1).setRefund(1).setCurrency("CNY"))
                .setReason("就是想退了");
        System.out.println("========= request ==========");
        System.out.println(JsonUtils.toJsonPrettyString(request));
        WxPayRefundV3Result response = client.refundV3(request);
        System.out.println("========= response ==========");
        System.out.println(JsonUtils.toJsonPrettyString(response));
    }

    private WxPayConfig buildWxPayConfigV2() {
        WxPayConfig config = new WxPayConfig();
        config.setAppId("wx62056c0d5e8db250");
        config.setMchId("1545083881");
        config.setMchKey("dS1ngeN63JLr3NRbvPH9AJy3MyUxZdim");
        config.setSignType(WxPayConstants.SignType.MD5);
        config.setKeyPath("/Users/yunai/Downloads/wx_pay/apiclient_cert.p12");
        return config;
    }

}
