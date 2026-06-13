package cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 产品入库单行新增/修改 Request VO")
@Data
public class MesWmProductReceiptLineSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "收货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "收货单编号不能为空")
    private Long receiptId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long itemId;

    @Schema(description = "库存物资记录编号", example = "1")
    private Long materialStockId;

    @Schema(description = "收货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "收货数量不能为空")
    @Positive(message = "收货数量必须大于 0")
    private BigDecimal quantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH20260301001")
    private String batchCode;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
