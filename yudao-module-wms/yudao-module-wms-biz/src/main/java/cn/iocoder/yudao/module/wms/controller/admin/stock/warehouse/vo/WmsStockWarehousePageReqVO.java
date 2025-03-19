package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : available_quantity,defective_quantity,outbound_pending_quantity,create_time,purchase_transit_quantity,product_id,shelving_pending_quantity,sellable_quantity,return_transit_quantity,purchase_plan_quantity,warehouse_id
 */
@Schema(description = "管理后台 - 仓库库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "14491")
    private Long warehouseId;

    @Schema(description = "产品ID", example = "3153")
    private String productId;

    @Schema(description = "产品SKU")
    private String productSku;

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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "待上架数量", example = "")
    private Integer shelvingPendingQuantity;

    @Schema(description = "待出库量", example = "")
    private Integer outboundPendingQuantity;
}
