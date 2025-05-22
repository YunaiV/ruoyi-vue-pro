package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw;

import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "用户 App - 分销提现创建 Request VO")
@Data
public class AppBrokerageWithdrawCreateReqVO {

    @Schema(description = "提现方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(value = BrokerageWithdrawTypeEnum.class, message = "提现方式必须是 {value}")
    private Integer type;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @PositiveOrZero(message = "提现金额不能小于 0")
    @Min(value = 30, message = "微信提现金额不能小于 0.3", groups = {WechatApi.class})
    @NotNull(message = "提现金额不能为空")
    private Integer price;

    @Schema(description = "提现账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456789")
    @NotBlank(message = "提现账号不能为空", groups = {Bank.class, WechatApi.class, AlipayApi.class})
    private String userAccount;

    @Schema(description = "提现姓名", example = "张三")
    @NotBlank(message = "提现姓名不能为空", groups = {Bank.class, WechatApi.class, AlipayApi.class})
    private String userName;

    @Schema(description = "收款码的图片", example = "https://www.iocoder.cn/1.png")
    @URL(message = "收款码的图片，必须是一个 URL", groups = {WechatQR.class, AlipayQR.class})
    private String qrCodeUrl;

    @Schema(description = "提现银行", example = "1")
    @NotNull(message = "提现银行不能为空", groups = {Bank.class})
    private String bankName;
    @Schema(description = "开户地址", example = "海淀支行")
    private String bankAddress;

    @Schema(description = "转账渠道", example = "wx_lite")
    @NotNull(message = "转账渠道不能为空", groups = {WechatApi.class})
    @InEnum(PayChannelEnum.class)
    private String transferChannelCode;

    public interface Wallet {
    }

    public interface Bank {
    }

    public interface WechatQR {
    }

    public interface WechatApi {
    }

    public interface AlipayQR {
    }

    public interface AlipayApi {
    }

    public void validate(Validator validator) {
        if (BrokerageWithdrawTypeEnum.WALLET.getType().equals(type)) {
            ValidationUtils.validate(validator, this, Wallet.class);
        } else if (BrokerageWithdrawTypeEnum.BANK.getType().equals(type)) {
            ValidationUtils.validate(validator, this, Bank.class);
        } else if (BrokerageWithdrawTypeEnum.WECHAT_QR.getType().equals(type)) {
            ValidationUtils.validate(validator, this, WechatQR.class);
        } else if (BrokerageWithdrawTypeEnum.WECHAT_API.getType().equals(type)) {
            ValidationUtils.validate(validator, this, WechatApi.class);
        } else if (BrokerageWithdrawTypeEnum.ALIPAY_QR.getType().equals(type)) {
            ValidationUtils.validate(validator, this, AlipayQR.class);
        } else if (BrokerageWithdrawTypeEnum.ALIPAY_API.getType().equals(type)) {
            ValidationUtils.validate(validator, this, AlipayApi.class);
        }
    }

}
