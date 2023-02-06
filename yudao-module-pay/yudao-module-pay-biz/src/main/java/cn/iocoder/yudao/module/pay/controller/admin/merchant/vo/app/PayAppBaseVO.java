package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

/**
* 支付应用信息 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayAppBaseVO {

    @Schema(description = "应用名", required = true)
    @NotNull(message = "应用名不能为空")
    private String name;

    @Schema(description = "开启状态", required = true)
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "支付结果的回调地址", required = true)
    @NotNull(message = "支付结果的回调地址不能为空")
    private String payNotifyUrl;

    @Schema(description = "退款结果的回调地址", required = true)
    @NotNull(message = "退款结果的回调地址不能为空")
    private String refundNotifyUrl;

    @Schema(description = "商户编号", required = true)
    @NotNull(message = "商户编号不能为空")
    private Long merchantId;

}
