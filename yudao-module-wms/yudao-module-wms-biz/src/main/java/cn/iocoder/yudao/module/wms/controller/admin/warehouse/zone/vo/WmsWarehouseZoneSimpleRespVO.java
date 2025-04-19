package cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : tenant_id,creator,code,create_time,priority,partition_type,updater,stock_type,update_time,name,id,status,warehouse_id
 */
@Schema(description = "管理后台 - 库区 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsWarehouseZoneSimpleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5926")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("代码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "归属的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17379")
    @ExcelProperty("归属的仓库ID")
    private Long warehouseId;

    @Schema(description = "存货类型 ; WarehouseAreaStockType : 1-拣货 , 2-存储", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("存货类型")
    private Integer stockType;

    @Schema(description = "分区类型 ; WarehouseAreaPartitionType : 1-标准品 , 2-不良品", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("分区类型")
    private Integer partitionType;

}
