package cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 生产退料单行 Save Request VO")
@Data
public class MesWmReturnIssueLineSaveReqVO {

    @Schema(description = "行 ID", example = "1")
    private Long id;

    @Schema(description = "退料单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "退料单 ID 不能为空")
    private Long issueId;

    @Schema(description = "物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料 ID 不能为空")
    private Long itemId;

    @Schema(description = "退料数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "退料数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "退料数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "库存记录 ID", example = "1")
    private Long materialStockId;

    @Schema(description = "批次 ID", example = "1")
    private Long batchId;

    @Schema(description = "批次编码", example = "BCH20260330001")
    private String batchCode;

    @Schema(description = "是否需要质检", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否需要质检不能为空")
    private Boolean rqcCheckFlag;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
