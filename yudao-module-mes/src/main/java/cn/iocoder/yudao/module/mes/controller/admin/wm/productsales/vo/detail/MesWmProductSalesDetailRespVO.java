package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 销售出库明细 Response VO")
@Data
public class MesWmProductSalesDetailRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "出库单行ID", example = "1")
    private Long lineId;

    @Schema(description = "出库单ID", example = "1")
    private Long salesId;

    @Schema(description = "物料ID", example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "产品A")
    private String itemName;

    @Schema(description = "规格型号", example = "100g")
    private String specification;

    @Schema(description = "单位名称", example = "个")
    private String unitMeasureName;

    @Schema(description = "拣货数量", example = "50")
    private BigDecimal quantity;

    @Schema(description = "库存记录ID", example = "1")
    private Long materialStockId;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH001")
    private String batchCode;

    @Schema(description = "仓库ID", example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "成品仓")
    private String warehouseName;

    @Schema(description = "库区ID", example = "1")
    private Long locationId;

    @Schema(description = "库区名称", example = "A区")
    private String locationName;

    @Schema(description = "库位ID", example = "1")
    private Long areaId;

    @Schema(description = "库位名称", example = "A-01")
    private String areaName;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
