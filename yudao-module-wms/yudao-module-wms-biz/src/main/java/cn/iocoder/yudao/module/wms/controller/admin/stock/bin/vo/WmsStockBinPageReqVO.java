package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : create_time,outbound_pending_qty,bin_id,product_id,available_qty,sellable_qty,warehouse_id
 */
@Schema(description = "管理后台 - 仓位库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockBinPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "748")
    private Long warehouseId;

    @Schema(description = "库区ID", example = "10839")
    private Long zoneId;

    @Schema(description = "库位ID", example = "10839")
    private Long binId;

    @Schema(description = "产品ID", example = "11713")
    private Long productId;

    @Schema(description = "产品代码", example = "3153")
    private String productCode;

    @Schema(description = "产品部门", example = "3153")
    private Long productDeptId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "可用量，在库的良品数量", example = "")
    private Integer[] availableQty;

    @Schema(description = "待出库量", example = "")
    private Integer[] outboundPendingQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    private Integer[] sellableQty;



    @Schema(description = "仓库库存-可用量，在库的良品数量", example = "")
    private Integer[] warehouseAvailableQty;

    @Schema(description = "仓库库存-不良品数量", example = "")
    private Integer[] warehouseDefectiveQty;

    @Schema(description = "仓库库存-待出库量", example = "")
    private Integer[] warehouseOutboundPendingQty;

    @Schema(description = "仓库库存-采购计划量", example = "")
    private Integer[] warehousePurchasePlanQty;

    @Schema(description = "仓库库存-采购在途量", example = "")
    private Integer[] warehousePurchaseTransitQty;

    @Schema(description = "仓库库存-退件在途数量", example = "")
    private Integer[] warehouseReturnTransitQty;

    @Schema(description = "仓库库存-可售量，未被单据占用的良品数量", example = "")
    private Integer[] warehouseSellableQty;

    @Schema(description = "仓库库存-待上架数量，上架是指从拣货区上架到货架", example = "")
    private Integer[] warehouseShelvingPendingQty;


    @Schema(description = "是否返回 suggestedOwnership ", example = "")
    private Integer withSuggestedOwnership;


}
