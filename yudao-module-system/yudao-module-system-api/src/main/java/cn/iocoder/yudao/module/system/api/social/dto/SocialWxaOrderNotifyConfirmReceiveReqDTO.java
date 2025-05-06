package cn.iocoder.yudao.module.system.api.social.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小程序订单上传购物详情
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/shopping-order/normal-shopping-detail/uploadShoppingInfo.html">上传购物详情</a>
 * @author 芋道源码
 */
@Data
public class SocialWxaOrderNotifyConfirmReceiveReqDTO {

    /**
     * 原支付交易对应的微信订单号
     */
    @NotEmpty(message = "原支付交易对应的微信订单号不能为空")
    private String transactionId;

    /**
     * 快递签收时间
     */
    @NotNull(message = "快递签收时间不能为空")
    private LocalDateTime receivedTime;

}
