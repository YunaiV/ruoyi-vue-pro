package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw;

import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Schema(description = "用户 App - 分销提现创建 Request VO")
@Data
public class AppBrokerageWithdrawCreateReqVO {

    @Schema(description = "提现方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(value = BrokerageWithdrawTypeEnum.class, message = "提现方式必须是 {value}")
    private Integer type;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @PositiveOrZero(message = "提现金额不能小于 0")
    @NotNull(message = "提现金额不能为空")
    private Integer price;

    // ========== 银行卡、微信、支付宝 提现相关字段 ==========

    @Schema(description = "提现账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456789")
    @NotBlank(message = "提现账号不能为空", groups = {Bank.class, Wechat.class, Alipay.class})
    private String accountNo;

    // ========== 微信、支付宝 提现相关字段 ==========

    @Schema(description = "收款码的图片", example = "https://www.iocoder.cn/1.png")
    @URL(message = "收款码的图片，必须是一个 URL")
    private String accountQrCodeUrl;

    // ========== 银行卡 提现相关字段 ==========

    @Schema(description = "持卡人姓名", example = "张三")
    @NotBlank(message = "持卡人姓名不能为空", groups = {Bank.class})
    private String name;
    @Schema(description = "提现银行", example = "1")
    @NotNull(message = "提现银行不能为空", groups = {Bank.class})
    private String bankName;
    @Schema(description = "开户地址", example = "海淀支行")
    private String bankAddress;

    public interface Wallet {
    }

    public interface Bank {
    }

    public interface Wechat {
    }

    public interface Alipay {
    }

    public void validate(Validator validator) {
        if (BrokerageWithdrawTypeEnum.WALLET.getType().equals(type)) {
            ValidationUtils.validate(validator, this, Wallet.class);
        } else if (BrokerageWithdrawTypeEnum.BANK.getType().equals(type)) {
            ValidationUtils.validate(validator, this, Bank.class);
        } else if (BrokerageWithdrawTypeEnum.WECHAT.getType().equals(type)) {
            ValidationUtils.validate(validator, this, Wechat.class);
        } else if (BrokerageWithdrawTypeEnum.ALIPAY.getType().equals(type)) {
            ValidationUtils.validate(validator, this, Alipay.class);
        }
    }

}
