package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

import java.sql.Timestamp;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockType;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;

/**
 * @table-fields : reason,outbound_pending_qty,delta_qty,flow_time,available_qty,next_flow_id,purchase_transit_qty,stock_id,stock_type,product_id,shelving_pending_qty,id,reason_bill_id,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,prev_flow_id,reason_item_id,warehouse_id
 */
@Schema(description = "管理后台 - 库存流水新增/修改 Request VO")
@Data
public class WmsStockFlowSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9446")
    private Long id;

    @Schema(description = "库存类型 ; StockType : 1-仓库库存 , 2-仓位库存 , 3-所有者库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "库存类型不能为空")
    @InEnum(WmsStockType.class)
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17743")
    @NotNull(message = "库存ID不能为空")
    private Long stockId;

    @Schema(description = "流水发生的原因 ; StockReason : 1-入库", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @NotNull(message = "流水发生的原因不能为空")
    @InEnum(WmsStockReason.class)
    private Integer reason;

    @Schema(description = "流水触发的单据ID", example = "21958")
    private Long reasonBillId;

    @Schema(description = "流水触发的单据下对应的明细ID", example = "30829")
    private Long reasonItemId;

    @Schema(description = "前一个流水ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31237")
    @NotNull(message = "前一个流水ID不能为空")
    private Long prevFlowId;

    @Schema(description = "流水发生的时间")
    private Timestamp flowTime;

    @Schema(description = "上一个流水ID", example = "")
    private Long nextFlowId;

    @Schema(description = "产品ID", example = "")
    private Long productId;

    @Schema(description = "仓库ID", example = "")
    private Long warehouseId;

    @Schema(description = "可用量，在库的良品数量", example = "")
    private Integer availableQty;

    @Schema(description = "不良品数量", example = "")
    private Integer defectiveQty;

    @Schema(description = "变更量", example = "")
    private Integer deltaQty;

    @Schema(description = "待出库量", example = "")
    private Integer outboundPendingQty;

    @Schema(description = "采购计划量", example = "")
    private Integer purchasePlanQty;

    @Schema(description = "采购在途量", example = "")
    private Integer purchaseTransitQty;

    @Schema(description = "退件在途数量", example = "")
    private Integer returnTransitQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    private Integer sellableQty;

    @Schema(description = "待上架数量", example = "")
    private Integer shelvingPendingQty;
}
