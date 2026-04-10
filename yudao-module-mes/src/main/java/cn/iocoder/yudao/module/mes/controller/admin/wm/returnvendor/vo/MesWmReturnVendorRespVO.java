package cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 供应商退货单 Response VO")
@Data
public class MesWmReturnVendorRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "RV20250226001")
    private String code;

    @Schema(description = "退货单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "供应商退货")
    private String name;

    @Schema(description = "采购订单编号", example = "PO20250226001")
    private String purchaseOrderCode;

    @Schema(description = "供应商 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long vendorId;

    @Schema(description = "供应商编码", example = "V001")
    private String vendorCode;

    @Schema(description = "供应商名称", example = "XX供应商")
    private String vendorName;

    @Schema(description = "供应商简称", example = "XX")
    private String vendorNickname;

    @Schema(description = "退货日期")
    private LocalDateTime returnDate;

    @Schema(description = "退货原因", example = "质量不合格")
    private String returnReason;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String transportCode;

    @Schema(description = "物流联系电话", example = "13800138000")
    private String transportTelephone;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
