package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayMicropayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayMicropayResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.config.WxPayConfig;
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
    public void testParseRefundNotifyV2() throws WxPayException {
        // 创建 config 配置
        WxPayConfig config = buildWxPayConfigV2();
        // 创建 WxPayService 客户端
        WxPayService client = new WxPayServiceImpl();
        client.setConfig(config);

        // 执行解析
        String xml = "<xml><return_code>SUCCESS</return_code><appid><![CDATA[wx62056c0d5e8db250]]></appid><mch_id><![CDATA[1545083881]]></mch_id><nonce_str><![CDATA[ed8f02c21d15635cede114a42d0525a0]]></nonce_str><req_info><![CDATA[bGp+wB9DAHjoOO9Nw1iSmmIFdN2zZDhsoRWZBYdf/8bcpjowr4T8i2qjLsbMtvKQeVC5kBZOL/Agal3be6UPwnoantil+L+ojZgvLch7dXFKs/AcoxIYcVYyGka+wmnRJfUmuFRBgzt++8HOFsmJz6e2brYv1EAz+93fP2AsJtRuw1FEzodcg8eXm52hbE0KhLNqC2OyNVkn8AbOOrwIxSYobg2jVbuJ4JllYbEGIQ/6kWzNbVmMKhGJGYBy/NbUGKoQsoe4QeTQqcqQqVp08muxaOfJGThaN3B9EEMFSrog/3yT7ykVV6WQ5+Ygt89LplOf5ucWa4Ird7VJhHWtzI92ZePj4Omy1XkT1TRlwtDegA0S5MeQpM4WZ1taMrhxgmNkTUJ0JXFncx5e2KLQvbvD/HOcccx48Xv1c16JBz6G3501k8E++LWXgZ2TeNXwGsk6FyRZb0ApLyQHIx5ZtPo/UET9z3AmJCPXkrUsZ4WK46fDtbzxVPU2r8nTOcGCPbO0LUsGT6wpsuQVC4CisXDJwoZmL6kKwHfKs6mmUL2YZYzNfgoB/KgpJYSpC96kcpQyFvw+xuwqK2SXGZbAl9lADT+a83z04feQHSSIG3PCrX4QEWzpCZZ4+ySEz1Y34aoU20X9GtX+1LSwUjmQgwHrMBSvFm3/B7+IFM8OUqDB+Uvkr9Uvy7P2/KDvfy3Ih7GFcGd0C5NXpSvVTTfu1IlK/T3/t6MR/8iq78pp/2ZTYvO6eNDRJWaXYU+x6sl2dTs9n+2Z4W4AfYTvEyuxlx+aI19SqCJh7WmaFcAxidFl/9iqDjWiplb9+C6ijZv2hJtVjSCuoptIWpGDYItH7RAqlKHrx6flJD+M/5BceMHBv2w4OWCD9vPRLo8gl9o06ip0iflzO1dixhOAgLFjsQmQHNGFtR3EvCID+iS4FUlilwK+hcKNxrr0wp9Btkl9W1R9aTo289CUiIxx45skfCYzHwb+7Hqj3uTiXnep6zhCKZBAnPsDOvISXfBgXKufcFsTNtts09jX8H5/uMc9wyJ179H1cp+At1mIK2duwfo4Q9asfEoffl6Zn1olGdtEruxHGeVU0NwJ8V7RflC/Cx5RXtJ3sPJ/sHmVnBlVyR0=]]></req_info></xml>";
        WxPayRefundNotifyResult response = client.parseRefundNotifyResult(xml);
        System.out.println(response.getReqInfo());
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
//        config.setSignType(WxPayConstants.SignType.MD5);
        config.setKeyPath("/Users/yunai/Downloads/wx_pay/apiclient_cert.p12");
        return config;
    }

}
