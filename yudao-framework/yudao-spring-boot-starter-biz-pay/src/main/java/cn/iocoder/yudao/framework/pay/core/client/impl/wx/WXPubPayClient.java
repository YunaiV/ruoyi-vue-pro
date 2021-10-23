package cn.iocoder.yudao.framework.pay.core.client.impl.wx;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXCodeMapping.CODE_SUCCESS;
import static cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXCodeMapping.MESSAGE_SUCCESS;

/**
 * 微信支付（公众号）的 PayClient 实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class WXPubPayClient extends AbstractPayClient<WXPayClientConfig> {

    private WxPayService client;

    public WXPubPayClient(Long channelId, String channelCode, WXPayClientConfig config) {
        super(channelId, channelCode, config, new WXCodeMapping());
    }

    @Override
    protected void doInit() {
        WxPayConfig payConfig = new WxPayConfig();
        BeanUtil.copyProperties(config, payConfig, "keyContent");
        payConfig.setTradeType(WxPayConstants.TradeType.JSAPI); // 设置使用 JS API 支付方式
//        if (StrUtil.isNotEmpty(config.getKeyContent())) {
//            payConfig.setKeyContent(config.getKeyContent().getBytes(StandardCharsets.UTF_8));
//        }
        if (StrUtil.isNotEmpty(config.getPrivateKeyContent())) {
            // weixin-pay-java 存在 BUG，无法直接设置内容，所以创建临时文件来解决
            payConfig.setPrivateKeyPath(FileUtils.createTempFile(config.getPrivateKeyContent()).getPath());
        }
        if (StrUtil.isNotEmpty(config.getPrivateCertContent())) {
            // weixin-pay-java 存在 BUG，无法直接设置内容，所以创建临时文件来解决
            payConfig.setPrivateCertPath(FileUtils.createTempFile(config.getPrivateCertContent()).getPath());
        }
        // 真实客户端
        this.client = new WxPayServiceImpl();
        client.setConfig(payConfig);
    }

    @Override
    public CommonResult<WxPayMpOrderResult> unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        WxPayMpOrderResult response;
        try {
            switch (config.getApiVersion()) {
                case WXPayClientConfig.API_VERSION_V2:
                    response = this.unifiedOrderV2(reqDTO);
                    break;
                case WXPayClientConfig.API_VERSION_V3:
                    WxPayUnifiedOrderV3Result.JsapiResult responseV3 = this.unifiedOrderV3(reqDTO);
                    // 将 V3 的结果，统一转换成 V2。返回的字段是一致的
                    response = new WxPayMpOrderResult();
                    BeanUtil.copyProperties(responseV3, response, true);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("未知的 API 版本(%s)", config.getApiVersion()));
            }
        } catch (WxPayException e) {
            log.error("[unifiedOrder][request({}) 发起支付失败，原因({})]", toJsonString(reqDTO), e);
            return PayCommonResult.build(defaultIfNull(e.getErrCode(), e.getReturnCode()),
                    defaultIfNull(e.getErrCodeDes(), e.getReturnMsg()),null, codeMapping);
        }
        return PayCommonResult.build(CODE_SUCCESS, MESSAGE_SUCCESS, response, codeMapping);
    }

    private WxPayMpOrderResult unifiedOrderV2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderRequest request = WxPayUnifiedOrderRequest.newBuilder()
                .outTradeNo(reqDTO.getMerchantOrderId())
                // TODO 芋艿：貌似没 title？
                .body(reqDTO.getBody())
                .totalFee(reqDTO.getAmount()) // 单位分
                .timeExpire(DateUtil.format(reqDTO.getExpireTime(), "yyyyMMddHHmmss"))
                .spbillCreateIp(reqDTO.getClientIp())
                .openid("ockUAwIZ-0OeMZl9ogcZ4ILrGba0") // TODO 芋艿：先随便写死
                .notifyUrl(reqDTO.getNotifyUrl())
                .build();
        // 执行请求
        return client.createOrder(request);
    }

    private WxPayUnifiedOrderV3Result.JsapiResult unifiedOrderV3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 构建 WxPayUnifiedOrderRequest 对象
        WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
        request.setOutTradeNo(reqDTO.getMerchantOrderId());
        // TODO 芋艿：貌似没 title？
        request.setDescription(reqDTO.getBody());
        request.setAmount(new WxPayUnifiedOrderV3Request.Amount().setTotal(reqDTO.getAmount())); // 单位分
        request.setTimeExpire(DateUtil.format(reqDTO.getExpireTime(), "yyyyMMddHHmmss"));
        request.setPayer(new WxPayUnifiedOrderV3Request.Payer().setOpenid("ockUAwIZ-0OeMZl9ogcZ4ILrGba0")); // TODO 芋艿：先随便写死
        request.setSceneInfo(new WxPayUnifiedOrderV3Request.SceneInfo().setPayerClientIp(reqDTO.getClientIp()));
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        // 执行请求
        return client.createOrderV3(TradeTypeEnum.JSAPI, request);
    }

    public static void main(String[] args) throws FileNotFoundException {
        WXPayClientConfig config = new WXPayClientConfig();
        config.setAppId("wx041349c6f39b268b");
        config.setMchId("1545083881");
        config.setMchKey("0alL64UDQdlCwiKZ73ib7ypaIjMns06p");
        config.setApiVersion(WXPayClientConfig.API_VERSION_V3);
//        config.setKeyContent(IoUtil.readUtf8(new FileInputStream("/Users/yunai/Downloads/wx_pay/apiclient_cert.p12")));
        config.setPrivateKeyContent(IoUtil.readUtf8(new FileInputStream("/Users/yunai/Downloads/wx_pay/apiclient_key.pem")));
        config.setPrivateCertContent(IoUtil.readUtf8(new FileInputStream("/Users/yunai/Downloads/wx_pay/apiclient_cert.pem")));
        config.setApiV3Key("joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase");

        WXPubPayClient client = new WXPubPayClient(1L, "biu", config);
        client.init();

        PayOrderUnifiedReqDTO reqDTO = new PayOrderUnifiedReqDTO();
        reqDTO.setAmount(123);
        reqDTO.setSubject("IPhone 13");
        reqDTO.setBody("biubiubiu");
        reqDTO.setMerchantOrderId(String.valueOf(System.currentTimeMillis()));
        reqDTO.setClientIp("127.0.0.1");
        reqDTO.setNotifyUrl("http://127.0.0.1:8080");
        CommonResult<WxPayMpOrderResult> result = client.unifiedOrder(reqDTO);
        System.out.println(result);
    }

}
