package cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 供应商退货单行 Save Request VO")
@Data
public class MesWmReturnVendorLineSaveReqVO {

    @Schema(description = "行ID", example = "1")
    private Long id;

    @Schema(description = "退货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "退货单ID不能为空")
    private Long returnId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料ID不能为空")
    private Long itemId;

    @Schema(description = "退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "退货数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "退货数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH20260101001")
    private String batchCode;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
