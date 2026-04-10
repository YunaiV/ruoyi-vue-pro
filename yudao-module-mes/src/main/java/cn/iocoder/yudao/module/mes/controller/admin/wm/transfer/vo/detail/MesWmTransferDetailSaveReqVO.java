package cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 调拨明细 Save Request VO")
@Data
public class MesWmTransferDetailSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "转移单行编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转移单行编号不能为空")
    private Long lineId;

    @Schema(description = "转移单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转移单编号不能为空")
    private Long transferId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long itemId;

    @Schema(description = "上架数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "上架数量不能为空")
    @DecimalMin(value = "0.01", message = "上架数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "移入仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "移入仓库编号不能为空")
    private Long toWarehouseId;

    @Schema(description = "移入库区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "移入库区编号不能为空")
    private Long toLocationId;

    @Schema(description = "移入库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "移入库位编号不能为空")
    private Long toAreaId;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

}
