package cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - 付款条款新增/修改 Request VO")
@Data
public class SrmPaymentTermSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10931")
    private Long id;

    @Schema(description = "人民币采购条款（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "人民币采购条款（中文）不能为空")
    private String paymentTermCn;

    @Schema(description = "外币采购条款（中文）")
    private String paymentTermCnForeign;

    @Schema(description = "外币采购条款（英文）")
    private String paymentTermEnForeign;

    @Schema(description = "备注", example = "随便")
    private String remark;

}