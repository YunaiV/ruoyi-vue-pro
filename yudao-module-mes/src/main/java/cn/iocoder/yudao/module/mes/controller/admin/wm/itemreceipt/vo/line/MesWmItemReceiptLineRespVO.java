package cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 采购入库单行 Response VO")
@Data
public class MesWmItemReceiptLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "入库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long receiptId;

    @Schema(description = "到货通知单行编号", example = "1")
    private Long arrivalNoticeLineId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    private String unitMeasureName;

    @Schema(description = "入库数量", example = "500.00")
    private BigDecimal receivedQuantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次编码", example = "BATCH20260110")
    private String batchCode;

    @Schema(description = "生产日期")
    private LocalDateTime productionDate;

    @Schema(description = "有效期")
    private LocalDateTime expireDate;

    @Schema(description = "生产批号", example = "PB20260110")
    private String lotNumber;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
