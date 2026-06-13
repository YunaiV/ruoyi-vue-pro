package cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 供应商退货明细新增/修改 Request VO")
@Data
public class MesWmReturnVendorDetailSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "退货单编号不能为空")
    private Long returnId;
    @Schema(description = "退货单行编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "退货单行编号不能为空")
    private Long lineId;

    @Schema(description = "库存记录编号", example = "1")
    private Long materialStockId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long itemId;

    @Schema(description = "退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "300.00")
    @NotNull(message = "退货数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "退货数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH20260101001")
    private String batchCode;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "备注", example = "备注")
    private String remark;

}

