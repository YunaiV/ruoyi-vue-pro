package cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 生产任务投料新增/修改 Request VO")
@Data
public class MesProTaskIssueSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "生产任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产任务编号不能为空")
    private Long taskId;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "工作站编号", example = "1")
    private Long workstationId;

    @Schema(description = "来源单据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源单据编号不能为空")
    private Long sourceDocId;

    @Schema(description = "来源单据编码", example = "DOC-001")
    private String sourceDocCode;

    @Schema(description = "来源单据类型", example = "MATERIAL_ISSUE")
    private String sourceDocType;

    @Schema(description = "投料批次", example = "BATCH-001")
    private String batchCode;

    @Schema(description = "来源单据行编号", example = "1")
    private Long sourceLineId;

    @Schema(description = "产品物料编号", example = "100")
    private Long itemId;

    @Schema(description = "单位编号", example = "1")
    private Long unitMeasureId;

    @Schema(description = "总投料数量", example = "100.00")
    private BigDecimal issuedQuantity;

    @Schema(description = "当前可用数量", example = "80.00")
    private BigDecimal availableQuantity;

    @Schema(description = "当前使用数量", example = "20.00")
    private BigDecimal usedQuantity;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
