package cn.iocoder.yudao.module.wms.controller.admin.inventory.result.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 库存盘点结果 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInventoryResultRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17313")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据号")
    private String no;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17719")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "创建者备注")
    @ExcelProperty("创建者备注")
    private String creatorComment;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}