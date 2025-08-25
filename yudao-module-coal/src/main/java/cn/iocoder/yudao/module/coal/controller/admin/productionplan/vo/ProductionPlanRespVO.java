package cn.iocoder.yudao.module.coal.controller.admin.productionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 生产计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductionPlanRespVO {

    @Schema(description = "计划ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20306")
    @ExcelProperty("计划ID")
    private Long id;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025年")
    @ExcelProperty("计划名称")
    private String name;

    @Schema(description = "父计划ID", example = "19375")
    @ExcelProperty("父计划ID")
    private Long parentId;

    @Schema(description = "计划类型", example = "1")
    @ExcelProperty(value = "计划类型", converter = DictConvert.class)
    @DictFormat("plan_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer planType;

    @Schema(description = "计划年度")
    @ExcelProperty("计划年度")
    private Integer planYear;

    @Schema(description = "计划月份")
    @ExcelProperty("计划月份")
    private Integer planMonth;

    @Schema(description = "计划日期")
    @ExcelProperty("计划日期")
    private LocalDate planDate;

    @Schema(description = "计划状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "计划状态", converter = DictConvert.class)
    @DictFormat("plan_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "计划入洗原煤量(吨)")
    @ExcelProperty("计划入洗原煤量(吨)")
    private BigDecimal rawCoalPlan;

    @Schema(description = "计划末煤产量(吨)")
    @ExcelProperty("计划末煤产量(吨)")
    private BigDecimal fineCoalPlan;

    @Schema(description = "计划粒煤产量(吨)")
    @ExcelProperty("计划粒煤产量(吨)")
    private BigDecimal granularCoalPlan;

    @Schema(description = "计划大块煤产量(吨)")
    @ExcelProperty("计划大块煤产量(吨)")
    private BigDecimal largeBlockCoalPlan;

    @Schema(description = "计划中块煤产量(吨)")
    @ExcelProperty("计划中块煤产量(吨)")
    private BigDecimal mediumBlockCoalPlan;

    @Schema(description = "计划小块煤产量(吨)")
    @ExcelProperty("计划小块煤产量(吨)")
    private BigDecimal smallBlockCoalPlan;

    @Schema(description = "计划中煤产量(吨)")
    @ExcelProperty("计划中煤产量(吨)")
    private BigDecimal middlingCoalPlan;

    @Schema(description = "计划煤泥产量(吨)")
    @ExcelProperty("计划煤泥产量(吨)")
    private BigDecimal slimePlan;

    @Schema(description = "计划矸石产量(吨)")
    @ExcelProperty("计划矸石产量(吨)")
    private BigDecimal ganguePlan;

    @Schema(description = "末煤灰分(%)")
    @ExcelProperty("末煤灰分(%)")
    private BigDecimal fineCoalAsh;

    @Schema(description = "粒煤灰分(%)")
    @ExcelProperty("粒煤灰分(%)")
    private BigDecimal granularCoalAsh;

    @Schema(description = "大块煤灰分(%)")
    @ExcelProperty("大块煤灰分(%)")
    private BigDecimal largeBlockCoalAsh;

    @Schema(description = "中块煤灰分(%)")
    @ExcelProperty("中块煤灰分(%)")
    private BigDecimal mediumBlockCoalAsh;

    @Schema(description = "小块煤灰分(%)")
    @ExcelProperty("小块煤灰分(%)")
    private BigDecimal smallBlockCoalAsh;

    @Schema(description = "中煤灰分(%)")
    @ExcelProperty("中煤灰分(%)")
    private BigDecimal middlingCoalAsh;

    @Schema(description = "制定人ID", example = "10216")
    @ExcelProperty("制定人ID")
    private Long creatorId;

    @Schema(description = "审批人ID", example = "9760")
    @ExcelProperty("审批人ID")
    private Long approverId;

    @Schema(description = "审批时间")
    @ExcelProperty("审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
