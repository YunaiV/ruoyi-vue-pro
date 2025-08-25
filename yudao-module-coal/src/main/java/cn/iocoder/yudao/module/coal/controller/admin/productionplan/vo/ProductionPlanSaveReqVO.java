package cn.iocoder.yudao.module.coal.controller.admin.productionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 生产计划新增/修改 Request VO")
@Data
public class ProductionPlanSaveReqVO {

    @Schema(description = "计划ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20306")
    private Long id;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025年")
    @NotEmpty(message = "计划名称不能为空")
    private String name;

    @Schema(description = "父计划ID", example = "19375")
    private Long parentId;

    @Schema(description = "计划类型", example = "1")
    private Integer planType;

    @Schema(description = "计划年度")
    private Integer planYear;

    @Schema(description = "计划月份")
    private Integer planMonth;

    @Schema(description = "计划日期")
    private LocalDate planDate;

    @Schema(description = "计划状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "计划状态不能为空")
    private Integer status;

    @Schema(description = "计划入洗原煤量(吨)")
    private BigDecimal rawCoalPlan;

    @Schema(description = "计划末煤产量(吨)")
    private BigDecimal fineCoalPlan;

    @Schema(description = "计划粒煤产量(吨)")
    private BigDecimal granularCoalPlan;

    @Schema(description = "计划大块煤产量(吨)")
    private BigDecimal largeBlockCoalPlan;

    @Schema(description = "计划中块煤产量(吨)")
    private BigDecimal mediumBlockCoalPlan;

    @Schema(description = "计划小块煤产量(吨)")
    private BigDecimal smallBlockCoalPlan;

    @Schema(description = "计划中煤产量(吨)")
    private BigDecimal middlingCoalPlan;

    @Schema(description = "计划煤泥产量(吨)")
    private BigDecimal slimePlan;

    @Schema(description = "计划矸石产量(吨)")
    private BigDecimal ganguePlan;

    @Schema(description = "末煤灰分(%)")
    private BigDecimal fineCoalAsh;

    @Schema(description = "粒煤灰分(%)")
    private BigDecimal granularCoalAsh;

    @Schema(description = "大块煤灰分(%)")
    private BigDecimal largeBlockCoalAsh;

    @Schema(description = "中块煤灰分(%)")
    private BigDecimal mediumBlockCoalAsh;

    @Schema(description = "小块煤灰分(%)")
    private BigDecimal smallBlockCoalAsh;

    @Schema(description = "中煤灰分(%)")
    private BigDecimal middlingCoalAsh;

    @Schema(description = "制定人ID", example = "10216")
    private Long creatorId;

    @Schema(description = "审批人ID", example = "9760")
    private Long approverId;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

}