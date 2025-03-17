package cn.iocoder.yudao.module.wms.controller.admin.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 出库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOutboundRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7690")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String no;

    @Schema(description = "仓库ID", example = "16056")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "类型", example = "1")
    @ExcelProperty("类型")
    private Integer type;

    @Schema(description = "状态", example = "1")
    @ExcelProperty("状态")
    private String status;

    @Schema(description = "审核状态", example = "2")
    @ExcelProperty("审核状态")
    private Integer auditStatus;

    @Schema(description = "来源单据ID", example = "32195")
    @ExcelProperty("来源单据ID")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    @ExcelProperty("来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型", example = "2")
    @ExcelProperty("来源单据类型")
    private Integer sourceBillType;

    @Schema(description = "特别说明，创建方专用")
    @ExcelProperty("特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}