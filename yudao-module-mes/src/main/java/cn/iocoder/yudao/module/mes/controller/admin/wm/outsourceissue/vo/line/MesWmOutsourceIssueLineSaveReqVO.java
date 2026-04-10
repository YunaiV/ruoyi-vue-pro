package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 外协发料单行新增/修改 Request VO")
@Data
public class MesWmOutsourceIssueLineSaveReqVO {

    @Schema(description = "行ID", example = "1024")
    private Long id;

    @Schema(description = "发料单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发料单ID不能为空")
    private Long issueId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料ID不能为空")
    private Long itemId;

    @Schema(description = "发料数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "500.00")
    @NotNull(message = "发料数量不能为空")
    @DecimalMin(value = "0.01", message = "发料数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "库存ID", example = "1")
    private Long materialStockId;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "批次编码", example = "BATCH001")
    private String batchCode;

}
