package cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 供应商退货单新增/修改 Request VO")
@Data
public class MesWmReturnVendorSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "RV20250226001")
    @NotBlank(message = "退货单编号不能为空")
    private String code;

    @Schema(description = "退货单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "供应商退货")
    @NotBlank(message = "退货单名称不能为空")
    private String name;

    @Schema(description = "采购订单编号", example = "PO20250226001")
    private String purchaseOrderCode;

    @Schema(description = "供应商 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商 ID 不能为空")
    private Long vendorId;

    @Schema(description = "退货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "退货日期不能为空")
    private LocalDateTime returnDate;

    @Schema(description = "退货原因", example = "质量不合格")
    private String returnReason;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String transportCode;

    @Schema(description = "物流联系电话", example = "13800138000")
    private String transportTelephone;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
