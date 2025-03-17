package cn.iocoder.yudao.module.wms.controller.admin.inbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 入库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsInboundRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6889")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据号")
    private String no;

    @Schema(description = "入库单类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("入库单类型")
    private Integer type;

    @Schema(description = "仓库ID", example = "23620")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态")
    private String status;

    @Schema(description = "来源单据ID", example = "24655")
    @ExcelProperty("来源单据ID")
    private Long sourceBillId;

    @Schema(description = "来源单据号")
    @ExcelProperty("来源单据号")
    private String sourceBillNo;

    @Schema(description = "来源单据类型", example = "2")
    @ExcelProperty("来源单据类型")
    private Integer sourceBillType;

    @Schema(description = "参考号")
    @ExcelProperty("参考号")
    private String referNo;

    @Schema(description = "跟踪号")
    @ExcelProperty("跟踪号")
    private String traceNo;

    @Schema(description = "运输方式，1-海运；2-火车；3-空运；4、集卡")
    @ExcelProperty("运输方式，1-海运；2-火车；3-空运；4、集卡")
    private Integer shippingMethod;

    @Schema(description = "预计到货时间")
    @ExcelProperty("预计到货时间")
    private LocalDateTime planArrivalTime;

    @Schema(description = "实际到货时间")
    @ExcelProperty("实际到货时间")
    private LocalDateTime actualArrivalTime;

    @Schema(description = "特别说明，创建方专用")
    @ExcelProperty("特别说明，创建方专用")
    private String creatorComment;

    @Schema(description = "初始库龄")
    @ExcelProperty("初始库龄")
    private Integer initAge;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}