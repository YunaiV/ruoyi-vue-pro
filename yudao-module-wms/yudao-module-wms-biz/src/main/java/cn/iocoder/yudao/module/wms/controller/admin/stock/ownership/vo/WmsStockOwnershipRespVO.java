package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 所有者库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockOwnershipRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17082")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14322")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "产品ID", example = "1919")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "产品SKU")
    @ExcelProperty("产品SKU")
    private String productSku;

    @Schema(description = "库存主体ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28887")
    @ExcelProperty("库存主体ID")
    private Long inventorySubjectId;

    @Schema(description = "库存归属ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15579")
    @ExcelProperty("库存归属ID")
    private Long inventoryOwnerId;

    @Schema(description = "可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用库存")
    private Integer availableQuantity;

    @Schema(description = "待出库库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("待出库库存")
    private Integer pendingOutboundQuantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}