package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.pay.core.client.AbstractPayCodeMapping;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.*;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayNotifyRefundStatusEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

/**
 * 支付宝抽象类， 实现支付宝统一的接口。如退款
 *
 * @author  jason
 */
@Slf4j
public abstract class AbstractAlipayClient extends AbstractPayClient<AlipayPayClientConfig> {

    protected DefaultAlipayClient client;

    public AbstractAlipayClient(Long channelId, String channelCode,
                                AlipayPayClientConfig config, AbstractPayCodeMapping codeMapping) {
        super(channelId, channelCode, config, codeMapping);
    }

    @Override
    @SneakyThrows
    protected void doInit() {
        AlipayConfig alipayConfig = new AlipayConfig();
        BeanUtil.copyProperties(config, alipayConfig, false);
        this.client = new DefaultAlipayClient(alipayConfig);
    }

    /**
     * 从支付宝通知返回参数中解析 PayOrderNotifyRespDTO, 通知具体参数参考
     *  //https://opendocs.alipay.com/open/203/105286
     * @param data 通知结果
     * @return 解析结果 PayOrderNotifyRespDTO
     * @throws Exception  解析失败，抛出异常
     */
    @Override
    public  PayOrderNotifyRespDTO parseOrderNotify(PayNotifyDataDTO data) throws Exception {
        Map<String, String> params = strToMap(data.getBody());

        return  PayOrderNotifyRespDTO.builder().orderExtensionNo(params.get("out_trade_no"))
                .channelOrderNo(params.get("trade_no")).channelUserId(params.get("seller_id"))
                .tradeStatus(params.get("trade_status"))
                .successTime(DateUtil.parse(params.get("notify_time"), "yyyy-MM-dd HH:mm:ss"))
                .data(data.getBody()).build();
    }

    @Override
    public PayRefundNotifyDTO parseRefundNotify(PayNotifyDataDTO notifyData) {
        Map<String, String> params = strToMap(notifyData.getBody());
        PayRefundNotifyDTO notifyDTO = PayRefundNotifyDTO.builder().channelOrderNo(params.get("trade_no"))
                .tradeNo(params.get("out_trade_no"))
                .reqNo(params.get("out_biz_no"))
                .status(PayNotifyRefundStatusEnum.SUCCESS)
                .refundSuccessTime(DateUtil.parse(params.get("gmt_refund"), "yyyy-MM-dd HH:mm:ss"))
                .build();
        return notifyDTO;
    }

    @Override
    public boolean isRefundNotify(PayNotifyDataDTO notifyData) {
        if (notifyData.getParams().containsKey("refund_fee")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean verifyNotifyData(PayNotifyDataDTO notifyData) {
        boolean verifyResult = false;
        try {
            verifyResult =  AlipaySignature.rsaCheckV1(notifyData.getParams(), config.getAlipayPublicKey(), StandardCharsets.UTF_8.name(), "RSA2");
        } catch (AlipayApiException e) {
            log.error("[AlipayClient verifyNotifyData][(notify param is :{}) 验证失败]", toJsonString(notifyData.getParams()), e);
        }
        return verifyResult;
    }

    /**
     * 支付宝统一的退款接口 alipay.trade.refund
     * @param reqDTO 退款请求 request DTO
     * @return 退款请求 Response
     */
    @Override
    protected PayCommonResult<PayRefundUnifiedRespDTO> doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO)  {
        AlipayTradeRefundModel model=new AlipayTradeRefundModel();
        model.setTradeNo(reqDTO.getChannelOrderNo());
        model.setOutTradeNo(reqDTO.getPayTradeNo());
        model.setOutRequestNo(reqDTO.getMerchantRefundId());
        model.setRefundAmount(calculateAmount(reqDTO.getAmount()).toString());
        model.setRefundReason(reqDTO.getReason());
        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        refundRequest.setBizModel(model);
        try {
            AlipayTradeRefundResponse response =  client.execute(refundRequest);
            log.info("[doUnifiedRefund][response({}) 发起退款 渠道返回", toJsonString(response));
            if (response.isSuccess()) {
                //退款导致触发的异步通知是发送到支付接口中设置的notify_url
                //支付宝不返回退款单号，设置为空
                PayRefundUnifiedRespDTO respDTO = new PayRefundUnifiedRespDTO();
                respDTO.setChannelRefundId("");
                return PayCommonResult.build(response.getCode(), response.getMsg(), respDTO, codeMapping);
            }
            // 失败。需要抛出异常
            return PayCommonResult.build(response.getCode(), response.getMsg(), null, codeMapping);
        } catch (AlipayApiException e) {
            // TODO 记录异常日志
            log.error("[doUnifiedRefund][request({}) 发起退款失败,网络读超时，退款状态未知]", toJsonString(reqDTO), e);
            return PayCommonResult.build(e.getErrCode(), e.getErrMsg(), null, codeMapping);
        }
    }



    /**
     * 支付宝统一回调参数  str 转 map
     *
     * @param s 支付宝支付通知回调参数
     * @return map 支付宝集合
     */
    public static Map<String, String> strToMap(String s) {
        // TODO @zxy：这个可以使用 hutool 的 HttpUtil decodeParams 方法么？
        Map<String, String> stringStringMap = new HashMap<>();
        // 调整时间格式
        String s3 = s.replaceAll("%3A", ":");
        // 获取 map
        String s4 = s3.replace("+", " ");
        String[] split = s4.split("&");
        for (String s1 : split) {
            String[] split1 = s1.split("=");
            stringStringMap.put(split1[0], split1[1]);
        }
        return stringStringMap;
    }

}
