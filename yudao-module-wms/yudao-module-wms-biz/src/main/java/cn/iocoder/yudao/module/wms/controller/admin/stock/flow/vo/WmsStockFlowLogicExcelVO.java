package cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author jisencai
 * @table-fields : tenant_id,reason,outbound_pending_qty,delta_qty,make_pending_qty,updater,update_time,product_id,shelving_pending_qty,id,reason_bill_id,defective_qty,direction,creator,create_time,flow_time,transit_qty,available_qty,next_flow_id,stock_id,stock_type,inbound_item_flow_id,return_transit_qty,sellable_qty,prev_flow_id,reason_item_id,warehouse_id
 */
@Schema(description = "管理后台 - 库存流水 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockFlowLogicExcelVO {


    @ExcelIgnore
    private Long id;

    @ExcelProperty("仓库")
    private String warehouseName;

    @ExcelProperty("公司")
    private String companyName;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("流水方向")
    private String directionLabel;

    @ExcelProperty("变更量")
    private Integer deltaQty;

    @ExcelProperty("发生时间")
    private String flowTime;

    @ExcelProperty("SKU")
    private String productCode;

    @ExcelProperty("业务单据")
    private String reasonBillCode;

    @ExcelProperty("待上架数量")
    private Integer shelvingPendingQty;

    @ExcelProperty("可用量")
    private Integer availableQty;

    @ExcelProperty("待出库量")
    private Integer outboundPendingQty;



}
