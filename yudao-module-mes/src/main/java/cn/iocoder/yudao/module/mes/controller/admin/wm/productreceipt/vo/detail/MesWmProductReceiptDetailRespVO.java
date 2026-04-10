package cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 产品收货单明细 Response VO")
@Data
public class MesWmProductReceiptDetailRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "收货单行编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long lineId;

    @Schema(description = "收货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long receiptId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "P001")
    private String itemCode;

    @Schema(description = "物料名称", example = "成品A")
    private String itemName;

    @Schema(description = "规格型号", example = "标准型")
    private String specification;

    @Schema(description = "计量单位名称", example = "件")
    private String unitMeasureName;

    @Schema(description = "上架数量", example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "成品仓")
    private String warehouseName;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库区名称", example = "A区")
    private String locationName;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "库位名称", example = "A-01")
    private String areaName;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
