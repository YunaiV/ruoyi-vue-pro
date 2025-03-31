package cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.warehouse.WmsWarehouseAreaStockType;
import cn.iocoder.yudao.module.wms.enums.warehouse.WmsWarehouseAreaPartitionType;
import cn.iocoder.yudao.module.wms.enums.common.WmsValidStatus;

/**
 * @table-fields : stock_type,code,name,id,priority,partition_type,status,warehouse_id
 */
@Schema(description = "管理后台 - 库区新增/修改 Request VO")
@Data
public class WmsWarehouseZoneSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5926")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "代码不能为空")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "归属的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17379")
    @NotNull(message = "归属的仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "存货类型 ; WarehouseAreaStockType : 1-拣货 , 2-存储", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "存货类型不能为空")
    @InEnum(WmsWarehouseAreaStockType.class)
    private Integer stockType;

    @Schema(description = "分区类型 ; WarehouseAreaPartitionType : 1-标准品 , 2-不良品", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分区类型不能为空")
    @InEnum(WmsWarehouseAreaPartitionType.class)
    private Integer partitionType;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    @InEnum(WmsValidStatus.class)
    private Integer status;

    @Schema(description = "优先级")
    private Integer priority;
}
