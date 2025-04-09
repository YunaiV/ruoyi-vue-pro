package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 盘点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInventoryRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10689")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据号")
    private String no;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26854")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "出库单审批状态 ; WmsInventoryAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("出库单审批状态 ; WmsInventoryAuditStatus : 0-起草中 , 1-待审批 , 2-已驳回 , 3-已通过")
    private Integer auditStatus;

    @Schema(description = "创建者备注")
    @ExcelProperty("创建者备注")
    private String creatorNotes;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}