package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 盘点新增/修改 Request VO")
@Data
public class WmsInventorySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10689")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "单据号不能为空")
    private String no;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26854")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "出库单审批状态 ; WmsInventoryAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "出库单审批状态 ; WmsInventoryAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过不能为空")
    private Integer auditStatus;

    @Schema(description = "创建者备注")
    private String creatorNotes;

}