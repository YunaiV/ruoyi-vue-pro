package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.*;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

/**
 * 支付宝【手机网站】的 PayClient 实现类
 * 文档：https://opendocs.alipay.com/apis/api_1/alipay.trade.wap.pay
 *
 * @author 芋道源码
 */
@Slf4j
public class AlipayWapPayClient extends AbstractPayClient<AlipayPayClientConfig> {

    private DefaultAlipayClient client;

    public AlipayWapPayClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_WAP.getCode(), config, new AlipayPayCodeMapping());
    }

    @Override
    @SneakyThrows
    protected void doInit() {
        AlipayConfig alipayConfig = new AlipayConfig();
        BeanUtil.copyProperties(config, alipayConfig, false);
        this.client = new DefaultAlipayClient(alipayConfig);
    }

    @Override
    public PayCommonResult<AlipayTradeWapPayResponse> doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        // 构建 AlipayTradeWapPayModel 请求
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(reqDTO.getMerchantOrderId());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(calculateAmount(reqDTO.getAmount()).toString());
        model.setProductCode("QUICK_WAP_PAY"); // TODO 芋艿：这里咋整
        //TODO 芋艿：这里咋整  jason @芋艿 可以去掉吧,
        // TODO 芋艿 似乎这里不用传sellerId
        // https://opendocs.alipay.com/apis/api_1/alipay.trade.wap.pay
        //model.setSellerId("2088102147948060");
        model.setTimeExpire(DateUtil.format(reqDTO.getExpireTime(),"yyyy-MM-dd HH:mm:ss"));
        // TODO 芋艿：userIp
        // 构建 AlipayTradeWapPayRequest
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(reqDTO.getNotifyUrl());
        request.setReturnUrl(reqDTO.getReturnUrl());
        // 执行请求
        AlipayTradeWapPayResponse response;
        try {
            response = client.pageExecute(request);
        } catch (AlipayApiException e) {
            return PayCommonResult.build(e.getErrCode(), e.getErrMsg(), null, codeMapping);
        }

        // TODO 芋艿：sub Code
        if(response.isSuccess() && Objects.isNull(response.getCode()) && Objects.nonNull(response.getBody())){
            //成功alipay wap 成功 code 为 null , body 为form 表单
            return PayCommonResult.build("-9999", "Success", response, codeMapping);
        }else {
            return PayCommonResult.build(response.getCode(), response.getMsg(), response, codeMapping);
        }
    }


    /**
     * 从支付宝通知返回参数中解析 PayOrderNotifyRespDTO, 通知具体参数参考
     *  //https://opendocs.alipay.com/open/203/105286
     * @param data 通知结果
     * @return 解析结果 PayOrderNotifyRespDTO
     * @throws Exception  解析失败，抛出异常
     */
    @Override
    public PayOrderNotifyRespDTO parseOrderNotify(PayNotifyDataDTO data) throws Exception {
        Map<String, String> params = data.getParams();
        return  PayOrderNotifyRespDTO.builder().orderExtensionNo(params.get("out_trade_no"))
                .channelOrderNo(params.get("trade_no")).channelUserId(params.get("seller_id"))
                .tradeStatus(params.get("trade_status"))
                .successTime(DateUtil.parse(params.get("notify_time"), "yyyy-MM-dd HH:mm:ss"))
                .data(data.getBody()).build();
    }


    @Override
    protected PayRefundUnifiedRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO)  {
        AlipayTradeRefundModel model=new AlipayTradeRefundModel();
        model.setTradeNo(reqDTO.getChannelOrderNo());
        model.setOutTradeNo(reqDTO.getPayTradeNo());
        model.setOutRequestNo(reqDTO.getRefundReqNo());
        model.setRefundAmount(calculateAmount(reqDTO.getAmount()).toString());
        model.setRefundReason(reqDTO.getReason());
        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        refundRequest.setBizModel(model);
        PayRefundUnifiedRespDTO respDTO = new PayRefundUnifiedRespDTO();
        try {
            AlipayTradeRefundResponse response =  client.execute(refundRequest);
            log.info("[doUnifiedRefund][response({}) 发起退款 渠道返回", toJsonString(response));
            if (response.isSuccess()) {
                //退款成功
                //TODO 沙箱环境 返回 的tradeNo(渠道退款单号） 和 订单的tradNo 是一个值，是不是理解不对?
                respDTO.setRespEnum(PayChannelRespEnum.SYNC_SUCCESS)
                        .setChannelRefundNo(response.getTradeNo())
                        .setPayTradeNo(response.getOutTradeNo());
            }else{
                //特殊处理 sub_code  ACQ.SYSTEM_ERROR（系统错误）， 需要调用重试任务
                //沙箱环境返回的貌似是”aop.ACQ.SYSTEM_ERROR“， 用contain
                if (response.getSubCode().contains("ACQ.SYSTEM_ERROR")) {
                    respDTO.setRespEnum(PayChannelRespEnum.RETRY_FAILURE)
                            .setChannelErrMsg(response.getSubMsg())
                            .setChannelErrCode(response.getSubCode());
                }else{
                    //其他当做不可以重试的错误
                    respDTO.setRespEnum(PayChannelRespEnum.CAN_NOT_RETRY_FAILURE)
                            .setChannelErrCode(response.getSubCode())
                            .setChannelErrMsg(response.getSubMsg());
                }
            }
            return respDTO;
        } catch (AlipayApiException e) {
            //TODO 记录异常日志
            log.error("[doUnifiedRefund][request({}) 发起退款失败,网络读超时，退款状态未知]", toJsonString(reqDTO), e);
            Throwable cause = e.getCause();
            //网络 read time out 异常, 退款状态未知
            if (cause instanceof SocketTimeoutException) {
                respDTO.setExceptionMsg(e.getMessage())
                        .setRespEnum(PayChannelRespEnum.READ_TIME_OUT_EXCEPTION);
            }else{
                respDTO.setExceptionMsg(e.getMessage())
                        .setChannelErrCode(e.getErrCode())
                        .setChannelErrMsg(e.getErrMsg())
                        .setRespEnum(PayChannelRespEnum.CALL_EXCEPTION);
            }

            return respDTO;

        }


    }

}
