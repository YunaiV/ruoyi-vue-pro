package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

/**
* 支付渠道 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayChannelBaseVO {

    @Schema(description = "渠道编码", required = true)
    @NotNull(message = "渠道编码不能为空")
    private String code;

    @Schema(description = "开启状态", required = true)
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "渠道费率，单位：百分比", required = true)
    @NotNull(message = "渠道费率，单位：百分比不能为空")
    private Double feeRate;

    @Schema(description = "商户编号", required = true)
    @NotNull(message = "商户编号不能为空")
    private Long merchantId;

    @Schema(description = "应用编号", required = true)
    @NotNull(message = "应用编号不能为空")
    private Long appId;

}
