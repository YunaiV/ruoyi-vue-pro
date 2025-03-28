package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : create_time,outbound_pending_qty,product_id,shelving_pending_qty,available_qty,purchase_transit_qty,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,warehouse_id
 */
@Schema(description = "管理后台 - 仓库库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "14491")
    private Long warehouseId;

    @Schema(description = "产品ID", example = "3153")
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

    @Schema(description = "不良品数量", example = "")
    private Integer[] defectiveQty;

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

    @Schema(description = "待上架数量，上架是指从拣货区上架到货架", example = "")
    private Integer[] shelvingPendingQty;


}
