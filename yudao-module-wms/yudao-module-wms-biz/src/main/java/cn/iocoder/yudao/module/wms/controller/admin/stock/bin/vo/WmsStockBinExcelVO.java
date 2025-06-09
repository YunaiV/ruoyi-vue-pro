package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo;

import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - 仓位库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockBinExcelVO {

    @ExcelIgnore
    private Long id;

    @ExcelProperty("仓库")
    private String warehouseName;

    @ExcelProperty("库位")
    private String binName;

    @Schema(description = "库位", example = "")
    private WmsWarehouseBinRespVO bin;

    @ExcelProperty("SKU")
    private String productCode;

    @ExcelProperty("可用量")
    private Integer availableQty;

    @ExcelProperty("待出库量")
    private Integer outboundPendingQty;

    @ExcelProperty("可售量")
    private Integer sellableQty;


}
