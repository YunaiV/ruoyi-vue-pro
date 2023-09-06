package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;

@Schema(description = "用户 App - 分销提现创建 Request VO")
@Data
public class AppBrokerageWithdrawCreateReqVO {

    // TODO @疯狂：参数校验逻辑，需要根据 type 进行不同的校验；感觉可以通过分组？

    @Schema(description = "提现方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "提现账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456789") // 银行卡号/微信账号/支付宝账号
    private String accountNo;

    @Schema(description = "收款码的图片", example = "https://www.iocoder.cn/1.png")
    @URL(message = "收款码的图片，必须是一个 URL")
    private String accountQrCodeUrl;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @Min(value = 1, message = "提现金额必须大于 1")
    private Integer price;

}
