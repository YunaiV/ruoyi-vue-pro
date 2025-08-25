package cn.iocoder.yudao.module.coal.controller.admin.productionplan.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 生产计划列表 Request VO")
@Data
public class ProductionPlanListReqVO {

    @Schema(description = "计划名称", example = "2025年")
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
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] planDate;

    @Schema(description = "计划状态", example = "2")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}