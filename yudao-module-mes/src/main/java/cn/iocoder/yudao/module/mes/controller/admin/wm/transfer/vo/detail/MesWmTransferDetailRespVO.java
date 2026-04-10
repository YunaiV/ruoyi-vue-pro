package cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 调拨明细 Response VO")
@Data
public class MesWmTransferDetailRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "转移单行编号", example = "1")
    private Long lineId;

    @Schema(description = "转移单编号", example = "1")
    private Long transferId;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;
    @Schema(description = "物料编码")
    private String itemCode;
    @Schema(description = "物料名称")
    private String itemName;
    @Schema(description = "规格型号")
    private String specification;
    @Schema(description = "单位名称")
    private String unitMeasureName;

    @Schema(description = "上架数量", example = "100")
    private BigDecimal quantity;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;
    @Schema(description = "批次号")
    private String batchCode;

    @Schema(description = "移入仓库编号", example = "1")
    private Long toWarehouseId;
    @Schema(description = "移入仓库名称")
    private String toWarehouseName;

    @Schema(description = "移入库区编号", example = "1")
    private Long toLocationId;
    @Schema(description = "移入库区名称")
    private String toLocationName;

    @Schema(description = "移入库位编号", example = "1")
    private Long toAreaId;
    @Schema(description = "移入库位名称")
    private String toAreaName;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

}
