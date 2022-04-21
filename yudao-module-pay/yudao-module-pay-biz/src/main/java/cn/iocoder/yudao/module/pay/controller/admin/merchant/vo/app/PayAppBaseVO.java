package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 支付应用信息 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayAppBaseVO {

    @ApiModelProperty(value = "应用名", required = true)
    @NotNull(message = "应用名不能为空")
    private String name;

    @ApiModelProperty(value = "开启状态", required = true)
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "支付结果的回调地址", required = true)
    @NotNull(message = "支付结果的回调地址不能为空")
    private String payNotifyUrl;

    @ApiModelProperty(value = "退款结果的回调地址", required = true)
    @NotNull(message = "退款结果的回调地址不能为空")
    private String refundNotifyUrl;

    @ApiModelProperty(value = "商户编号", required = true)
    @NotNull(message = "商户编号不能为空")
    private Long merchantId;

}
