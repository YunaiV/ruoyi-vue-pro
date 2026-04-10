package cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 排班计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesCalPlanPageReqVO extends PageParam {

    @Schema(description = "计划编码", example = "PLAN001")
    private String code;

    @Schema(description = "计划名称", example = "2024年排班")
    private String name;

    @Schema(description = "轮班方式", example = "1")
    private Integer shiftType;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "班组类型", example = "1")
    private Integer calendarType;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startDate;

    @Schema(description = "结束日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endDate;

}
