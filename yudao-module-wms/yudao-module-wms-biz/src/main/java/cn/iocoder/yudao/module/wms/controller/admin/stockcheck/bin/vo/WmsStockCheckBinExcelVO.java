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
public class WmsStockCheckBinExcelVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11537")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点单", requiredMode = Schema.RequiredMode.REQUIRED, example = "5995")
    @ExcelProperty("盘点单")
    private String stockCheckCode;

    @ExcelIgnore
    private Long binId;

    @Schema(description = "仓位代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    @ExcelProperty("库位")
    private String binCode;

    @Schema(description = "仓位名称", example = "")
    @ExcelProperty("仓位名称")
    private String binName;

    @ExcelIgnore
    private Long productId;

    @Schema(description = "产品代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "30522")
    @ExcelProperty("产品代码")
    private String productCode;

    @Schema(description = "预期库存，仓位可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预期库存")
    private Integer expectedQty;


    @Schema(description = "实际库存，实盘数量", example = "100")
    @ExcelProperty("实盘数量")
    private Integer actualQty;

    @Schema(description = "备注", example = "")
    @ExcelProperty("备注")
    private String remark;
}
