package cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 库存盘点结果详情 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInventoryResultItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1580")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点结果单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2645")
    @ExcelProperty("盘点结果单ID")
    private Long resultId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20359")
    @ExcelProperty("产品ID")
    private String productId;

    @Schema(description = "预期库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预期库存")
    private Integer expectedQty;

    @Schema(description = "实际库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("实际库存")
    private Integer actualQty;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}