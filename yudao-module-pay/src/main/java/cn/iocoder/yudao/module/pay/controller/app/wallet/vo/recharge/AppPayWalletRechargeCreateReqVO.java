package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import java.util.Objects;

@Schema(description = "用户 APP - 创建钱包充值 Request VO")
@Data
public class AppPayWalletRechargeCreateReqVO {

    @Schema(description = "支付金额",  example = "1000")
    @Min(value = 1,  message = "支付金额必须大于零")
    private Integer payPrice;

    @Schema(description = "充值套餐编号", example = "1024")
    private Long packageId;

    @AssertTrue(message = "充值金额和充钱套餐不能同时为空")
    public boolean isValidPayPriceAndPackageId() {
        return Objects.nonNull(payPrice) || Objects.nonNull(packageId);
    }

}
