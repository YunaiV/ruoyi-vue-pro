package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import com.github.binarywang.wxpay.bean.request.WxPayMicropayRequest;
import com.github.binarywang.wxpay.bean.result.WxPayMicropayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

/**
 * 微信支付【付款码支付】的 PayClient 实现类
 *
 * 文档：<a href="https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=9_10&index=1">付款码支付</a>
 *
 * @author 芋道源码
 */
@Slf4j
public class WxBarPayClient extends AbstractWxPayClient {

    /**
     * 微信付款码的过期时间
     */
    private static final Duration AUTH_CODE_EXPIRE = Duration.ofMinutes(3);

    public WxBarPayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_BAR.getCode(), config);
    }

    @Override
    protected void doInit() {
        super.doInit(WxPayConstants.TradeType.MICROPAY);
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrderV2(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        // 由于付款码需要不断轮询，所以需要在较短的时间完成支付
        LocalDateTime expireTime = LocalDateTimeUtils.addTime(AUTH_CODE_EXPIRE);
        if (expireTime.isAfter(reqDTO.getExpireTime())) {
            expireTime = reqDTO.getExpireTime();
        }
        // 构建 WxPayMicropayRequest 对象
        WxPayMicropayRequest request = WxPayMicropayRequest.newBuilder()
                .outTradeNo(reqDTO.getOutTradeNo())
                .body(reqDTO.getSubject())
                .detail(reqDTO.getBody())
                .totalFee(reqDTO.getPrice()) // 单位分
                .timeExpire(formatDateV2(expireTime))
                .spbillCreateIp(reqDTO.getUserIp())
                .authCode(getAuthCode(reqDTO))
                .build();
        // 执行请求，重试直到失败（过期），或者成功
        WxPayException lastWxPayException = null;
        for (int i = 1; i < Byte.MAX_VALUE; i++) {
            try {
                WxPayMicropayResult response = client.micropay(request);
                // 支付成功，例如说：1）用户输入了密码；2）用户免密支付
                return PayOrderRespDTO.successOf(response.getTransactionId(), response.getOpenid(), parseDateV2(response.getTimeEnd()),
                        response.getOutTradeNo(), response)
                        .setDisplayMode(PayOrderDisplayModeEnum.BAR_CODE.getMode());
            } catch (WxPayException ex) {
                lastWxPayException = ex;
                // 如果不满足这 3 种任一的，则直接抛出 WxPayException 异常，不仅需处理
                // 1. SYSTEMERROR：接口返回错误：请立即调用被扫订单结果查询API，查询当前订单状态，并根据订单的状态决定下一步的操作。
                // 2. USERPAYING：用户支付中，需要输入密码：等待 5 秒，然后调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作。
                // 3. BANKERROR：银行系统异常：请立即调用被扫订单结果查询 API，查询当前订单的不同状态，决定下一步的操作。
                if (!StrUtil.equalsAny(ex.getErrCode(), "SYSTEMERROR", "USERPAYING", "BANKERROR")) {
                    throw ex;
                }
                // 等待 5 秒，继续下一轮重新发起支付
                log.info("[doUnifiedOrderV2][发起微信 Bar 支付第({})失败，等待下一轮重试，请求({})，响应({})]", i,
                        toJsonString(request), ex.getMessage());
                ThreadUtil.sleep(5, TimeUnit.SECONDS);
            }
        }
        throw lastWxPayException;
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrderV3(PayOrderUnifiedReqDTO reqDTO) throws WxPayException {
        return doUnifiedOrderV2(reqDTO);
    }

    // ========== 各种工具方法 ==========

    static String getAuthCode(PayOrderUnifiedReqDTO reqDTO) {
        String authCode = MapUtil.getStr(reqDTO.getChannelExtras(), "authCode");
        if (StrUtil.isEmpty(authCode)) {
            throw invalidParamException("支付请求的 authCode 不能为空！");
        }
        return authCode;
    }

}
