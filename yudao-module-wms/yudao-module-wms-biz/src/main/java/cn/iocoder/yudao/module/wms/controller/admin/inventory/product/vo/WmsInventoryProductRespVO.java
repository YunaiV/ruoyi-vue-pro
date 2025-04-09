package cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo;

import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,creator,update_time,notes,actual_qty,create_time,expected_qty,inventory_id,product_id,id,updater
 */
@Schema(description = "管理后台 - 库存盘点产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInventoryProductRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25474")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点结果单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7233")
    @ExcelProperty("盘点结果单ID")
    private Long inventoryId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25033")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "预期库存，产品总可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预期库存")
    private Integer expectedQty;

    @Schema(description = "实际库存，实盘数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("实际库存")
    private Integer actualQty;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String notes;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

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

    @Schema(description = "产品", example = "")
    @ExcelProperty("产品")
    private WmsProductRespSimpleVO product;
}
