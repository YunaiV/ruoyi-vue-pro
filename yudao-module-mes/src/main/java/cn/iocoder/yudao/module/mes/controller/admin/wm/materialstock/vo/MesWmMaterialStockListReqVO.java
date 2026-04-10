package cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料库存查询 Request VO")
@Data
public class MesWmMaterialStockListReqVO {

    @Schema(description = "仓库编号")
    private Long warehouseId;

    @Schema(description = "库区编号")
    private Long locationId;

    @Schema(description = "库位编号")
    private Long areaId;

    @Schema(description = "物料编号")
    private Long itemId;

    @Schema(description = "批次编号")
    private Long batchId;

    @Schema(description = "批次号")
    private String batchCode;

    @Schema(description = "开始时间（动态盘点用）")
    private LocalDateTime startTime;

    @Schema(description = "结束时间（动态盘点用）")
    private LocalDateTime endTime;

}
