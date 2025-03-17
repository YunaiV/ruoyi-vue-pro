package cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 出库单详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOutboundItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "27153")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "入库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6602")
    @ExcelProperty("入库单ID")
    private Long outboundId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20572")
    @ExcelProperty("标准产品ID")
    private Long productId;

    @Schema(description = "标准产品SKU", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标准产品SKU")
    private String productSku;

    @Schema(description = "预期量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预期量")
    private Integer expectedQuantity;

    @Schema(description = "实际量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("实际量")
    private Integer actualQuantity;

    @Schema(description = "来源详情ID", example = "11448")
    @ExcelProperty("来源详情ID")
    private Long sourceItemId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}