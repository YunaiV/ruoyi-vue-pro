package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : reason,outbound_pending_qty,create_time,delta_qty,flow_time,next_flow_id,available_qty,purchase_transit_qty,stock_id,stock_type,product_id,shelving_pending_qty,reason_bill_id,return_transit_qty,sellable_qty,defective_qty,purchase_plan_qty,warehouse_id,direction,reason_item_id,prev_flow_id
 */
@Schema(description = "管理后台 - 库存流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockFlowPageReqVO extends PageParam {

    @Schema(description = "库存类型 ; WmsStockType : 1-仓库库存 , 2-仓位库存 , 3-所有者库存", example = "2" , hidden = true)
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", example = "17743")
    private Long stockId;

    @Schema(description = "流水发生的原因 ; WmsStockReason : 1-入库 , 2-拣货 , 3-出库 , 4-提交出库单 , 5-拒绝出库单", example = "1",hidden = true)
    private Integer[] reason;

    @Schema(description = "流水触发的单据ID", example = "21958")
    private Long reasonBillId;

    @Schema(description = "流水触发的单据下对应的明细ID", example = "30829")
    private Long reasonItemId;

    @Schema(description = "前一个流水ID", example = "31237",hidden = true)
    private Long prevFlowId;

    @Schema(description = "流水发生的时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Timestamp[] flowTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "上一个流水ID", example = "" ,hidden = true)
    private Long nextFlowId;

    @Schema(description = "产品ID", example = "")
    private Long productId;

    @Schema(description = "产品代码", example = "")
    private String productCode;

    @Schema(description = "仓库ID", example = "")
    private Long warehouseId;

    @Schema(description = "可用量，在库的良品数量", example = "")
    private Integer[] availableQty;

    @Schema(description = "不良品数量", example = "")
    private Integer[] defectiveQty;

    @Schema(description = "变更量", example = "")
    private Integer[] deltaQty;

    @Schema(description = "待出库量", example = "")
    private Integer[] outboundPendingQty;

    @Schema(description = "采购计划量", example = "")
    private Integer[] purchasePlanQty;

    @Schema(description = "采购在途量", example = "")
    private Integer[] purchaseTransitQty;

    @Schema(description = "退件在途数量", example = "")
    private Integer[] returnTransitQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    private Integer[] sellableQty;

    @Schema(description = "待上架数量", example = "")
    private Integer[] shelvingPendingQty;

    @Schema(description = "库存流水方向 ; WmsStockFlowDirection : -1-流出 , 1-流入", example = "")
    private Integer direction;
}
