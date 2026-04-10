package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 钱包充值记录 Resp VO")
@Data
public class AppPayWalletRechargeRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户实际到账余额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalPrice;

    @Schema(description = "实际支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer payPrice;

    @Schema(description = "钱包赠送金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer bonusPrice;

    @Schema(description = "支付成功的支付渠道", requiredMode = Schema.RequiredMode.REQUIRED)
    private String payChannelCode;

    @Schema(description = "支付渠道名", example = "微信小程序支付")
    private String payChannelName;

    @Schema(description = "支付订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long payOrderId;

    @Schema(description = "支付成功的外部订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String payOrderChannelOrderNo; // 从 PayOrderDO 的 channelOrderNo 字段

    @Schema(description = "订单支付时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime payTime;

    @Schema(description = "退款状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer refundStatus;

}
