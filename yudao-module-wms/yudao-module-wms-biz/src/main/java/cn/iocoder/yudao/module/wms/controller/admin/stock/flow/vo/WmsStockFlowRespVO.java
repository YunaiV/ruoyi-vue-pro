package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @table-fields : tenant_id,reason,outbound_pending_qty,delta_qty,make_pending_qty,updater,update_time,product_id,shelving_pending_qty,id,reason_bill_id,defective_qty,direction,creator,create_time,flow_time,transit_qty,available_qty,next_flow_id,stock_id,stock_type,inbound_item_flow_id,return_transit_qty,sellable_qty,prev_flow_id,reason_item_id,warehouse_id
 */
@Schema(description = "管理后台 - 库存流水 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockFlowRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9446")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "WMS库存类型 ; WmsStockType : 1-仓库库存 , 2-仓位库存 , 3-逻辑库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("WMS库存类型")
    private Integer stockType;

    @Schema(description = "库存ID，分别指向三张库存表的ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17743")
    @ExcelProperty("库存ID")
    private Long stockId;

    @Schema(description = "仓库", example = "")
    private WmsWarehouseSimpleRespVO warehouse;

    @Schema(description = "库位", example = "")
    private WmsWarehouseBinRespVO bin;

    @Schema(description = "WMS流水发生的原因 ; WmsStockReason : 1-入库 , 2-拣货 , 3-出库 , 4-提交出库单 , 5-拒绝出库单 , 6-库位库存移动 , 7-逻辑库存移动 , 8-盘赢 , 9-盘亏", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @ExcelProperty("WMS流水发生的原因")
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

    @Schema(description = "流水发生的时间")
    @ExcelProperty("流水发生的时间")
    private Timestamp flowTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

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

    @Schema(description = "产品", example = "")
    private WmsProductRespSimpleVO product;

    @Schema(description = "仓库ID", example = "")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "可用量，在库的良品数量", example = "")
    @ExcelProperty("可用量")
    private Integer availableQty;

    @Schema(description = "不良品数量", example = "")
    @ExcelProperty("不良品数量")
    private Integer defectiveQty;

    @Schema(description = "变更量", example = "")
    @ExcelProperty("变更量")
    private Integer deltaQty;

    @Schema(description = "待出库量", example = "")
    @ExcelProperty("待出库量")
    private Integer outboundPendingQty;

    @Schema(description = "退件在途数量", example = "")
    @ExcelProperty("退件在途数量")
    private Integer returnTransitQty;

    @Schema(description = "可售量，未被单据占用的良品数量", example = "")
    @ExcelProperty("可售量")
    private Integer sellableQty;

    @Schema(description = "待上架数量", example = "")
    @ExcelProperty("待上架数量")
    private Integer shelvingPendingQty;

    @Schema(description = "WMS库存流水方向 ; WmsStockFlowDirection : -1-流出 , 1-流入", example = "")
    @ExcelProperty("WMS库存流水方向")
    private Integer direction;

    @Schema(description = "入库单", example = "")
    private WmsInboundSimpleRespVO inbound;

    @Schema(description = "出库单", example = "")
    private WmsOutboundSimpleRespVO outbound;

    @Schema(description = "拣货单", example = "")
    private WmsPickupSimpleRespVO pickup;

    @Schema(description = "盘点单", example = "")
    private WmsStockCheckRespVO stockCheck;

    @Schema(description = "仓位库存移动单", example = "")
    private WmsStockBinMoveRespVO stockBinMove;

    @Schema(description = "仓位库存移动单", example = "")
    private WmsStockLogicMoveRespVO stockLogicMove;

    @Schema(description = "部门", example = "")
    private DeptSimpleRespVO dept;

    @Schema(description = "创建人姓名", example = "张三")
    @ExcelProperty("创建人姓名")
    private String creatorName;

    @Schema(description = "更新人姓名", example = "李四")
    @ExcelProperty("更新人姓名")
    private String updaterName;

    @Schema(description = "当前仓库库存", example = "{}")
    @ExcelProperty("当前仓库库存")
    private WmsStockWarehouseSimpleVO stockWarehouse;

    @Schema(description = "批次库存流水ID", example = "")
    @ExcelProperty("批次库存流水ID")
    private Long inboundItemFlowId;

    @Schema(description = "批次库存流水", example = "")
    @ExcelProperty("批次库存流水")
    private WmsInboundItemFlowSimpleVO inboundItemFlow;

    @Schema(description = "在制数量", example = "")
    @ExcelProperty("在制数量")
    private Integer makePendingQty;

    @Schema(description = "在途量", example = "")
    @ExcelProperty("在途量")
    private Integer transitQty;

    @Schema(description = "公司", example = "")
    @ExcelProperty("公司")
    private FmsCompanySimpleRespVO company;




}
