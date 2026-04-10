package cn.iocoder.yudao.module.mes.controller.admin.wm.returnissue.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产退料明细 Response VO")
@Data
public class MesWmReturnIssueDetailRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "退料单编号", example = "1")
    private Long issueId;

    @Schema(description = "行编号", example = "1")
    private Long lineId;

    @Schema(description = "库存记录编号", example = "1")
    private Long materialStockId;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "ITEM001")
    private String itemCode;

    @Schema(description = "物料名称", example = "螺丝")
    private String itemName;

    @Schema(description = "规格型号", example = "M8*20")
    private String specification;

    @Schema(description = "计量单位名称", example = "个")
    private String unitMeasureName;

    @Schema(description = "退料数量", example = "300.00")
    private BigDecimal quantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH20260101001")
    private String batchCode;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "原材料仓库")
    private String warehouseName;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库区名称", example = "A区")
    private String locationName;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "库位名称", example = "A01")
    private String areaName;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
