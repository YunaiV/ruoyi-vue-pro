package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.sql.Timestamp;

/**
 * @table-fields : reason,defective_quantity,purchase_transit_quantity,flow_time,return_transit_quantity,stock_id,purchase_plan_quantity,stock_type,available_quantity,outbound_pending_quantity,shelving_pending_quantity,sellable_quantity,delta_quantity,id,reason_bill_id,reason_item_id,prev_flow_id
 */
@Schema(description = "管理后台 - 库存流水新增/修改 Request VO")
@Data
public class WmsStockFlowSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9446")
    private Long id;

    @Schema(description = "库存类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "库存类型不能为空")
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17743")
    @NotNull(message = "库存ID不能为空")
    private Long stockId;

    @Schema(description = "流水发生的原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @NotNull(message = "流水发生的原因不能为空")
    private Integer reason;

    @Schema(description = "流水触发的单据ID", example = "21958")
    private Long reasonBillId;

    @Schema(description = "流水触发的单据下对应的明细ID", example = "30829")
    private Long reasonItemId;

    @Schema(description = "前一个流水ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31237")
    @NotNull(message = "前一个流水ID不能为空")
    private Long prevFlowId;

    @Schema(description = "变更量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变更量不能为空")
    private Integer deltaQuantity;

    @Schema(description = "采购计划量")
    private Integer purchasePlanQuantity;

    @Schema(description = "采购在途量")
    private Integer purchaseTransitQuantity;

    @Schema(description = "退件在途数量")
    private Integer returnTransitQuantity;

    @Schema(description = "待上架数量")
    private Integer pendingShelvingQuantity;

    @Schema(description = "可用量，在库的良品数量")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量")
    private Integer sellableQuantity;

    @Schema(description = "待出库量")
    private Integer pendingOutboundQuantity;

    @Schema(description = "不良品数量")
    private Integer defectiveQuantity;

    @Schema(description = "流水发生的时间")
    private Timestamp flowTime;

    @Schema(description = "待上架数量", example = "")
    private Integer shelvingPendingQuantity;

    @Schema(description = "待出库量", example = "")
    private Integer outboundPendingQuantity;
}
