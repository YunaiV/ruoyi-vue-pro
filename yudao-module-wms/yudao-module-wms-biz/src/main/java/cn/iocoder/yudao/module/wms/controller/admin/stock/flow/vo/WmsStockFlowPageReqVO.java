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
 * @author jisencai
 * @table-fields : reason,create_time,outbound_pending_qty,delta_qty,flow_time,transit_qty,available_qty,next_flow_id,make_pending_qty,stock_id,stock_type,inbound_item_flow_id,product_id,shelving_pending_qty,reason_bill_id,defective_qty,return_transit_qty,sellable_qty,direction,prev_flow_id,reason_item_id,warehouse_id
 */
@Schema(description = "管理后台 - 库存流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockFlowPageReqVO extends PageParam {

    @Schema(description = "WMS库存类型 ; WmsStockType : 1-仓库库存 , 2-仓位库存 , 3-逻辑库存", example = "2", hidden = true)
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", example = "17743")
    private Long stockId;

    @Schema(description = "WMS流水发生的原因 ; WmsStockReason : 1-入库 , 2-拣货 , 3-出库 , 4-提交出库单 , 5-拒绝出库单 , 6-库位库存移动 , 7-逻辑库存移动 , 8-盘赢 , 9-盘亏", example = "1", hidden = true)
    private Integer[] reason;

    @Schema(description = "流水触发的单据ID", example = "21958")
    private Long reasonBillId;

    @Schema(description = "流水触发的单据下对应的明细ID", example = "30829")
    private Long reasonItemId;

    @Schema(description = "前一个流水ID", example = "31237", hidden = true)
    private Long prevFlowId;

    @Schema(description = "流水发生的时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Timestamp[] flowTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "上一个流水ID", example = "", hidden = true)
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

    @Schema(description = "退件在途数量", example = "")
    private Integer[] returnTransitQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    private Integer[] sellableQty;

    @Schema(description = "待上架数量", example = "")
    private Integer[] shelvingPendingQty;

    @Schema(description = "WMS库存流水方向 ; WmsStockFlowDirection : -1-流出 , 1-流入", example = "")
    private Integer direction;

    @Schema(description = "批次库存流水ID", example = "")
    private Long inboundItemFlowId;

    @Schema(description = "在制数量", example = "")
    private Integer[] makePendingQty;

    @Schema(description = "在途量", example = "")
    private Integer[] transitQty;

    @Schema(description = "公司ID", example = "")
    private Long companyId;
}
