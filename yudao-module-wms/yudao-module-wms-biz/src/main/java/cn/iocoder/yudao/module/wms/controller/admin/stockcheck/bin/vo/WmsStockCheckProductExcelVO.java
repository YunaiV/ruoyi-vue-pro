package cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @table-fields : tenant_id,creator,update_time,actual_qty,create_time,bin_id,expected_qty,stockCheck_id,product_id,remark,id,updater
 */
@Schema(description = "管理后台 - 库位盘点 Response VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class WmsStockCheckProductExcelVO {

    @ExcelIgnore
    private Long productId;

    @ExcelProperty("产品代码")
    private String productCode;

    @Schema(description = "备注", example = "")
    @ExcelProperty("备注")
    private String remark;
}
