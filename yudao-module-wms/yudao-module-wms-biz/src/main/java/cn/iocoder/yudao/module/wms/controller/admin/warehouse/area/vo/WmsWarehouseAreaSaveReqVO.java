package cn.iocoder.yudao.module.wms.controller.admin.warehouse.area.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.ValidStatus;
import cn.iocoder.yudao.module.wms.enums.WarehouseAreaPartitionType;
import cn.iocoder.yudao.module.wms.enums.WarehouseAreaStockType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

/**
 * @table-fields : stock_type,code,name,id,priority,partition_type,warehouse_id,status
 */
@Schema(description = "管理后台 - 库区新增/修改 Request VO")
@Data
public class WmsWarehouseAreaSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20864")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "代码不能为空")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "归属的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11263")
    @NotNull(message = "归属的仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "分区类型 ; WarehouseAreaPartitionType : 1-标准品 , 2-不良品", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分区类型不能为空")
    @InEnum(WarehouseAreaPartitionType.class)
    private Integer partitionType;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    @InEnum(ValidStatus.class)
    private Integer status;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "存货类型 ; WarehouseAreaStockType : 1-拣货 , 2-存储", example = "")
    @InEnum(WarehouseAreaStockType.class)
    private Integer stockType;
}
