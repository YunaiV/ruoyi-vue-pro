package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jisencai
 * @table-fields : tenant_id,outbound_available_qty,creator,inbound_status,company_id,create_time,plan_qty,shelve_closed_qty,upstream_id,remark,latest_flow_id,updater,inbound_id,update_time,actual_qty,product_id,id,dept_id
 */
@Schema(description = "管理后台 - 库位移动 StockBinMove Import VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class WmsStockLogicMoveImportExcelVO {

    @ExcelProperty("仓库代码")
    public String warehouseCode;

    @ExcelProperty("仓库ID")
    public Long warehouseId;

    @ExcelProperty("产品代码")
    private String productCode;
    /**
     * 产品ID
     **/
    @ExcelIgnore
    private Long productId;

    @ExcelProperty("调出公司")
    private String fromCompanyName;

    @ExcelProperty("调出部门")
    private String fromDeptName;

    @ExcelProperty("调入公司")
    private String toCompanyName;

    @ExcelProperty("调入部门")
    private String toDeptName;

    @ExcelProperty("移动数量")
    private Integer qty;

    /**
     * 调出公司ID
     **/
    @ExcelIgnore
    private Long fromCompanyId;

    /**
     * 调出部门ID
     **/
    @ExcelIgnore
    private Long fromDeptId;

    /**
     * 调入公司ID
     **/
    @ExcelIgnore
    private Long toCompanyId;

    /**
     * 调入部门ID
     **/
    @ExcelIgnore
    private Long toDeptId;

    @ExcelProperty("备注")
    private String remark;

}
