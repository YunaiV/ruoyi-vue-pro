package cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,update_time,notes,actual_quantity,create_time,bin_id,expected_qty,inventory_id,product_id,id,updater
 */
@Schema(description = "管理后台 - 库位盘点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInventoryBinRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11537")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点结果单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5995")
    @ExcelProperty("盘点结果单ID")
    private Long inventoryId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30522")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "预期库存，仓位可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预期库存")
    private Integer expectedQty;

    @Schema(description = "实际库存，实盘数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("实际库存")
    private Integer actualQuantity;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String notes;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "仓位ID", example = "")
    @ExcelProperty("仓位ID")
    private Long binId;

    @Schema(description = "创建者", example = "")
    @ExcelProperty("创建者")
    private String creator;

    @Schema(description = "更新者", example = "")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", example = "")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户编号", example = "")
    @ExcelProperty("租户编号")
    private Long tenantId;
}
