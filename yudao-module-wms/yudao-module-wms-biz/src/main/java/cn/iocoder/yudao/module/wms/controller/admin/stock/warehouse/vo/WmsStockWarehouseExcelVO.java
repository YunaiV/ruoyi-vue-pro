package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @table-fields : tenant_id,creator,create_time,outbound_pending_qty,available_qty,purchase_transit_qty,updater,update_time,product_id,shelving_pending_qty,id,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,warehouse_id
 */
@Schema(description = "管理后台 - 仓库库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockWarehouseExcelVO {

    @ExcelIgnore
    private Long id;

    @ExcelProperty("仓库")
    private String warehouseName;

    @ExcelProperty("产品SKU")
    private String productCode;

    @ExcelProperty("可用量")
    private Integer availableQty;

    @ExcelProperty("不良品数量")
    private Integer defectiveQty;

    @ExcelProperty("待出库量")
    private Integer outboundPendingQty;

    @ExcelProperty("在制量")
    private Integer makePendingQty;

    @ExcelProperty("在途量")
    private Integer transitQty;

    @ExcelProperty("退件在途量")
    private Integer returnTransitQty;

    @ExcelProperty("可售量")
    private Integer sellableQty;

    @ExcelProperty("待上架数量")
    private Integer shelvingPendingQty;

}
