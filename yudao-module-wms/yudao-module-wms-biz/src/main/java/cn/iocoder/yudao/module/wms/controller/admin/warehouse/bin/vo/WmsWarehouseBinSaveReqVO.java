package cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.common.ValidStatus;

/**
 * @table-fields : zone_id,code,picking_order,name,id,status,warehouse_id
 */
@Schema(description = "管理后台 - 库位新增/修改 Request VO")
@Data
public class WmsWarehouseBinSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23735")
    private Long id;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "代码不能为空")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "归属的仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14159")
    @NotNull(message = "归属的仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "库区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29995")
    @NotNull(message = "库区ID不能为空")
    private Long zoneId;

    @Schema(description = "拣货顺序")
    private Integer pickingOrder;

    @Schema(description = "状态，WMS通用的对象有效状态 ; ValidStatus : 0-不可用 , 1-可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(ValidStatus.class)
    private Integer status;
}
