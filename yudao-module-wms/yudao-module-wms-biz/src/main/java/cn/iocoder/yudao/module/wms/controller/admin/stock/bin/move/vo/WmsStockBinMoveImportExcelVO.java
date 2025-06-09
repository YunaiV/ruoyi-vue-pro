package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,latest_flow_id,updater,inbound_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 库位移动 StockBinMove Import VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class WmsStockBinMoveImportExcelVO {

    @ExcelProperty("仓库代码")
    public String warehouseCode;
    /**
     * 仓库ID
     **/
    @ExcelIgnore
    public Long warehouseId;

    @ExcelProperty("产品代码")
    private String productCode;

    /**
     * 产品ID
     **/
    @ExcelIgnore
    private Long productId;

    @ExcelProperty("调出库位代码")
    private String fromBinCode;

    @ExcelProperty("调入库位代码")
    private String toBinCode;

    @ExcelProperty("移动数量")
    private Integer qty;

    /**
     * 调出库位ID
     **/
    @ExcelIgnore
    private Long fromBinId;



    /**
     * 调入库ID
     **/
    @ExcelIgnore
    private Long toBinId;

    @ExcelProperty("备注")
    private String remark;

}
