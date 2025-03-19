package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : available_quantity,tenant_id,outbound_pending_quantity,creator,update_time,company_id,create_time,product_id,id,dept_id,warehouse_id,updater
 */
@Schema(description = "管理后台 - 所有者库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockOwnershipRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17082")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14322")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "产品ID", example = "1919")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "产品SKU")
    @ExcelProperty("产品SKU")
    private String productSku;

    @Schema(description = "库存主体ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28887")
    @ExcelProperty("库存主体ID")
    private Long inventorySubjectId;

    @Schema(description = "库存归属ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15579")
    @ExcelProperty("库存归属ID")
    private Long inventoryOwnerId;

    @Schema(description = "可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用库存")
    private Integer availableQuantity;

    @Schema(description = "待出库库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("待出库库存")
    private Integer pendingOutboundQuantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "库存财务主体公司ID", example = "")
    @ExcelProperty("库存财务主体公司ID")
    private Long companyId;

    @Schema(description = "库存归属部门ID", example = "")
    @ExcelProperty("库存归属部门ID")
    private Long deptId;

    @Schema(description = "待出库库存", example = "")
    @ExcelProperty("待出库库存")
    private Integer outboundPendingQuantity;

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
