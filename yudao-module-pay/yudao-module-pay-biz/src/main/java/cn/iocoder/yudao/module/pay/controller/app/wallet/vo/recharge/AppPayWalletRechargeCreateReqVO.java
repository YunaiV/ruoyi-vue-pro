package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;

@Schema(description = "用户 APP - 创建钱包充值 Request VO")
@Data
public class AppPayWalletRechargeCreateReqVO {

    @Schema(description = "支付金额",  example = "1000")
    @Min(value = 1,  message = "支付金额必须大于零")
    private Integer payPrice;

    @Schema(description = "充值套餐编号", example = "1024")
    private Long packageId;

    // TODO @jaosn：写个 AssertTrue 的校验方法，payPrice 和 packageId 必须二选一

}
