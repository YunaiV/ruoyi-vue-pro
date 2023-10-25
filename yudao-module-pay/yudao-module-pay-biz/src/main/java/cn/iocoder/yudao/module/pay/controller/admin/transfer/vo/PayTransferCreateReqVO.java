package cn.iocoder.yudao.module.pay.controller.admin.transfer.vo;

import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer.PayDemoTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - 发起转账 Request VO")
@Data
public class PayTransferCreateReqVO {

    @Schema(description = "应用编号不能为空", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    @Schema(description = "商户转账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商户转账单编号不能为空")
    private String merchantTransferId;

    @Schema(description = "转账类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转账类型不能为空")
    @InEnum(PayTransferTypeEnum.class)
    private Integer type;

    @Schema(description = "转账渠道", requiredMode = Schema.RequiredMode.REQUIRED, example = "alipay_pc")
    @NotEmpty(message = "转账渠道不能为空")
    private String channelCode;

    @Min(value = 1, message = "转账金额必须大于零")
    @NotNull(message = "转账金额不能为空")
    private Integer price;

    @Schema(description = "转账标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "示例转账")
    @NotEmpty(message = "转账标题不能为空")
    private String subject;

    @Schema(description = "支付宝登录号",  example = "test1@sandbox.com")
    @NotBlank(message = "支付宝登录号不能为空", groups = {PayTransferTypeEnum.Alipay.class})
    private String alipayLogonId;

    @Schema(description = "支付宝账号名称",  example = "test1")
    @NotBlank(message = "支付宝账号名称不能为空", groups = {PayTransferTypeEnum.Alipay.class})
    private String alipayAccountName;

    @Schema(description = "微信 openId",  example = "oLefc4g5Gxx")
    @NotBlank(message = "微信 openId 不能为空", groups = {PayTransferTypeEnum.WxPay.class})
    private String openid;

    @Schema(description = "微信账号名称", example = "test2")
    private String wxAccountName;

    @Schema(description = "转账渠道的额外参数")
    private Map<String, String> channelExtras;

    public void validate(Validator validator) {
       PayTransferTypeEnum transferType = PayTransferTypeEnum.typeOf(type);
        switch (transferType) {
            case ALIPAY_BALANCE: {
                ValidationUtils.validate(validator, this, PayDemoTransferCreateReqVO.Alipay.class);
                break;
            }
            case WX_BALANCE: {
                ValidationUtils.validate(validator, this, PayDemoTransferCreateReqVO.WxPay.class);
                break;
            }
            default: {
                throw new UnsupportedOperationException("待实现");
            }
        }
    }

}
