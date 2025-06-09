package cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo;

import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @table-fields : tenant_id,creator,create_time,outbound_pending_qty,available_qty,purchase_transit_qty,updater,update_time,product_id,shelving_pending_qty,id,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,warehouse_id
 */
@Schema(description = "管理后台 - 仓库库存按产品分组 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockWarehouseProductRespVO {

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3153")
    @ExcelProperty("产品ID")
    private Long id;

    @Schema(description = "产品", example = "")
    private WmsProductRespSimpleVO product;

    @Schema(description = "仓库库存", example = "")
    private List<WmsStockWarehouseRespVO> stockWarehouseList;



}
