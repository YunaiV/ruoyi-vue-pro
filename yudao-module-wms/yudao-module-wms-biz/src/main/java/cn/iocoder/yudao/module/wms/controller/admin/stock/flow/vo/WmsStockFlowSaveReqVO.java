package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @table-fields : reason,outbound_pending_qty,delta_qty,flow_time,transit_qty,available_qty,next_flow_id,make_pending_qty,stock_id,stock_type,inbound_item_flow_id,product_id,shelving_pending_qty,id,reason_bill_id,defective_qty,return_transit_qty,sellable_qty,direction,prev_flow_id,reason_item_id,warehouse_id
 */
@Schema(description = "管理后台 - 库存流水新增/修改 Request VO")
@Data
public class WmsStockFlowSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9446")
    private Long id;

    @Schema(description = "WMS库存类型 ; WmsStockType : 1-仓库库存 , 2-仓位库存 , 3-逻辑库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "WMS库存类型不能为空")
    @InEnum(WmsStockType.class)
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17743")
    @NotNull(message = "库存ID不能为空")
    private Long stockId;

    @Schema(description = "WMS流水发生的原因 ; WmsStockReason : 1-入库 , 2-拣货 , 3-出库 , 4-提交出库单 , 5-拒绝出库单 , 6-库位库存移动 , 7-逻辑库存移动 , 8-盘赢 , 9-盘亏", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @NotNull(message = "WMS流水发生的原因不能为空")
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

    @Schema(description = "退件在途数量", example = "")
    private Integer returnTransitQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    private Integer sellableQty;

    @Schema(description = "待上架数量", example = "")
    private Integer shelvingPendingQty;

    @Schema(description = "WMS库存流水方向 ; WmsStockFlowDirection : -1-流出 , 1-流入", example = "")
    @InEnum(WmsStockFlowDirection.class)
    private Integer direction;

    @Schema(description = "批次库存流水ID", example = "")
    private Long inboundItemFlowId;

    @Schema(description = "在制数量", example = "")
    private Integer makePendingQty;

    @Schema(description = "在途量", example = "")
    private Integer transitQty;
}
