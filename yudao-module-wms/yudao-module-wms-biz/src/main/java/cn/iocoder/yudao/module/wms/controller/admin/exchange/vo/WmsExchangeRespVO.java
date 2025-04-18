package cn.iocoder.yudao.module.wms.controller.admin.exchange.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 换货单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsExchangeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20006")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据号")
    private String code;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("类型")
    private Integer type;

    @Schema(description = "调出仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27440")
    @ExcelProperty("调出仓库ID")
    private Long warehouseId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private String auditStatus;

    @Schema(description = "特别说明", example = "你猜")
    @ExcelProperty("特别说明")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}