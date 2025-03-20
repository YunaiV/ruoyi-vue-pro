package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,defective_quantity,creator,create_time,purchase_transit_quantity,return_transit_quantity,purchase_plan_quantity,updater,available_quantity,outbound_pending_quantity,update_time,product_id,shelving_pending_quantity,sellable_quantity,id,warehouse_id
 */
@Schema(description = "管理后台 - 仓库库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockWarehouseRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21341")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14491")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3153")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "采购计划量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("采购计划量")
    private Integer purchasePlanQuantity;

    @Schema(description = "采购在途量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("采购在途量")
    private Integer purchaseTransitQuantity;

    @Schema(description = "退件在途数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("退件在途数量")
    private Integer returnTransitQuantity;

    @Schema(description = "可用量，在库的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用量")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可售量")
    private Integer sellableQuantity;

    @Schema(description = "不良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("不良品数量")
    private Integer defectiveQuantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "待上架数量，上架是指从拣货区上架到货架", example = "")
    @ExcelProperty("待上架数量")
    private Integer shelvingPendingQuantity;

    @Schema(description = "待出库量", example = "")
    @ExcelProperty("待出库量")
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
