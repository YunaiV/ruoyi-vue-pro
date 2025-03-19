package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 仓位库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockBinRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30764")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "748")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "库位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10839")
    @ExcelProperty("库位ID")
    private Long binId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11713")
    @ExcelProperty("产品ID")
    private String productId;

    @Schema(description = "可用量，在库的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用量，在库的良品数量")
    private Integer availableQuantity;

    @Schema(description = "可售量，未被单据占用的良品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可售量，未被单据占用的良品数量")
    private Integer sellableQuantity;

    @Schema(description = "待出库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("待出库量")
    private Integer outboundPendingQuantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}