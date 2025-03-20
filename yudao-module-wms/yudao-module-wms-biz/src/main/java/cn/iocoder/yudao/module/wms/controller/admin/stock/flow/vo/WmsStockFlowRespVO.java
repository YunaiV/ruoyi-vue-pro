package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import java.sql.Timestamp;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,reason,purchase_plan_quantity,updater,available_quantity,outbound_pending_quantity,update_time,product_id,shelving_pending_quantity,sellable_quantity,id,reason_bill_id,defective_quantity,creator,create_time,purchase_transit_quantity,flow_time,next_flow_id,return_transit_quantity,stock_id,stock_type,delta_quantity,prev_flow_id,reason_item_id,warehouse_id
 */
@Schema(description = "管理后台 - 库存流水 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockFlowRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9446")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "库存类型 ; StockType : 1-仓库库存 , 2-仓位库存 , 3-所有者库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("库存类型")
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17743")
    @ExcelProperty("库存ID")
    private Long stockId;

    @Schema(description = "流水发生的原因 ; StockReason : 1-入库", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @ExcelProperty("流水发生的原因")
    private Integer reason;

    @Schema(description = "流水触发的单据ID", example = "21958")
    @ExcelProperty("流水触发的单据ID")
    private Long reasonBillId;

    @Schema(description = "流水触发的单据下对应的明细ID", example = "30829")
    @ExcelProperty("流水触发的单据下对应的明细ID")
    private Long reasonItemId;

    @Schema(description = "前一个流水ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31237")
    @ExcelProperty("前一个流水ID")
    private Long prevFlowId;

    @Schema(description = "变更量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变更量")
    private Integer deltaQuantity;

    @Schema(description = "采购计划量")
    @ExcelProperty("采购计划量")
    private Integer purchasePlanQuantity;

    @Schema(description = "采购在途量")
    @ExcelProperty("采购在途量")
    private Integer purchaseTransitQuantity;

    @Schema(description = "退件在途数量")
    @ExcelProperty("退件在途数量")
    private Integer returnTransitQuantity;

    @Schema(description = "可用量，在库的良品数量")
    @ExcelProperty("可用量")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量")
    @ExcelProperty("可售量")
    private Integer sellableQuantity;

    @Schema(description = "不良品数量")
    @ExcelProperty("不良品数量")
    private Integer defectiveQuantity;

    @Schema(description = "流水发生的时间")
    @ExcelProperty("流水发生的时间")
    private Timestamp flowTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "待上架数量", example = "")
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

    @Schema(description = "上一个流水ID", example = "")
    @ExcelProperty("上一个流水ID")
    private Long nextFlowId;

    @Schema(description = "产品ID", example = "")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "仓库ID", example = "")
    @ExcelProperty("仓库ID")
    private Long warehouseId;
}
