package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import java.sql.Timestamp;

/**
 * @table-fields : defective_quantity,reason,create_time,purchase_transit_quantity,flow_time,next_flow_id,return_transit_quantity,purchase_plan_quantity,stock_id,available_quantity,stock_type,outbound_pending_quantity,product_id,shelving_pending_quantity,sellable_quantity,delta_quantity,reason_bill_id,prev_flow_id,reason_item_id,warehouse_id
 */
@Schema(description = "管理后台 - 库存流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockFlowPageReqVO extends PageParam {

    @Schema(description = "库存类型 ; StockType : 1-仓库库存 , 1-仓位库存 , 1-所有者库存", example = "2")
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", example = "17743")
    private Long stockId;

    @Schema(description = "流水发生的原因 ; StockReason : 1-入库", example = "不香")
    private Integer reason;

    @Schema(description = "流水触发的单据ID", example = "21958")
    private Long reasonBillId;

    @Schema(description = "流水触发的单据下对应的明细ID", example = "30829")
    private Long reasonItemId;

    @Schema(description = "前一个流水ID", example = "31237")
    private Long prevFlowId;

    @Schema(description = "变更量")
    private Integer deltaQuantity;

    @Schema(description = "采购计划量")
    private Integer purchasePlanQuantity;

    @Schema(description = "采购在途量")
    private Integer purchaseTransitQuantity;

    @Schema(description = "退件在途数量")
    private Integer returnTransitQuantity;

    @Schema(description = "可用量，在库的良品数量")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量")
    private Integer sellableQuantity;

    @Schema(description = "不良品数量")
    private Integer defectiveQuantity;

    @Schema(description = "流水发生的时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Timestamp[] flowTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "待上架数量", example = "")
    private Integer shelvingPendingQuantity;

    @Schema(description = "待出库量", example = "")
    private Integer outboundPendingQuantity;

    @Schema(description = "上一个流水ID", example = "")
    private Long nextFlowId;

    @Schema(description = "产品ID", example = "")
    private Long productId;

    @Schema(description = "仓库ID", example = "")
    private Long warehouseId;
}
