package cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 产品收货单行 Response VO")
@Data
public class MesWmProductReceiptLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "收货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long receiptId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "库存物资记录编号", example = "1")
    private Long materialStockId;

    @Schema(description = "物料编码", example = "P001")
    private String itemCode;

    @Schema(description = "物料名称", example = "成品A")
    private String itemName;

    @Schema(description = "规格型号", example = "标准型")
    private String specification;

    @Schema(description = "计量单位名称", example = "件")
    private String unitMeasureName;

    @Schema(description = "收货数量", example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH20260301001")
    private String batchCode;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
