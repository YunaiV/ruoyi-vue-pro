package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : tenant_id,no,creator,update_time,create_time,execute_status,id,warehouse_id,updater
 */
@Schema(description = "管理后台 - 库位移动 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockBinMoveSimpleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29002")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String no;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15798")
    @ExcelProperty("仓库ID")
    private Long warehouseId;



}
