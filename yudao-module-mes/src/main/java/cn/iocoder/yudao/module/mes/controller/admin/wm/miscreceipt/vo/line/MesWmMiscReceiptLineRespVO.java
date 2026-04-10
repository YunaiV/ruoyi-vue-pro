package cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 杂项入库单行 Response VO")
@Data
public class MesWmMiscReceiptLineRespVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "入库单编号", example = "1")
    private Long receiptId;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;

    @Schema(description = "入库数量", example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次号", example = "BATCH20260301")
    private String batchCode;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    private String unitMeasureName;

    @Schema(description = "仓库名称", example = "原料仓")
    private String warehouseName;

    @Schema(description = "库区名称", example = "A区")
    private String locationName;

    @Schema(description = "库位名称", example = "1排1架")
    private String areaName;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
