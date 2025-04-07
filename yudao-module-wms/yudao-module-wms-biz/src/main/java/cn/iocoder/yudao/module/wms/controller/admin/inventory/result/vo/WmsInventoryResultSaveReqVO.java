package cn.iocoder.yudao.module.wms.controller.admin.inventory.result.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 库存盘点结果新增/修改 Request VO")
@Data
public class WmsInventoryResultSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17313")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "单据号不能为空")
    private String no;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17719")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "创建者备注")
    private String creatorComment;

}