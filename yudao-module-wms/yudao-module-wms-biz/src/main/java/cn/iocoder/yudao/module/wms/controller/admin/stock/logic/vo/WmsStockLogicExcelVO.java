package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @author jisencai
 */
@Schema(description = "管理后台 - 逻辑库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockLogicExcelVO {

    @ExcelIgnore
    private Long id;

    @ExcelProperty("仓库")
    private String warehouseName;

    @ExcelProperty("SKU")
    private String productCode;

    @ExcelProperty("公司")
    private String companyName;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("可用库存")
    private Integer availableQty;

    @ExcelProperty("待出库库存")
    private Integer outboundPendingQty;

    @ExcelProperty("待上架数量")
    private Integer shelvingPendingQty;


}
