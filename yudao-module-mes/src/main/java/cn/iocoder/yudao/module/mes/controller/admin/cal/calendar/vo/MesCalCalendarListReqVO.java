package cn.iocoder.yudao.module.mes.controller.admin.cal.calendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 排班日历查询 Request VO")
@Data
public class MesCalCalendarListReqVO {

    public static final String QUERY_TYPE_TYPE = "TYPE";
    public static final String QUERY_TYPE_TEAM = "TEAM";
    public static final String QUERY_TYPE_USER = "USER";

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startDay;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endDay;

    @Schema(description = "查询类型：TYPE-按分类，TEAM-按班组，USER-按个人", requiredMode = Schema.RequiredMode.REQUIRED, example = "TYPE")
    @NotEmpty(message = "查询类型不能为空")
    private String queryType;

    @Schema(description = "班组类型（queryType=TYPE 时使用）", example = "1")
    private Integer calendarType;

    @Schema(description = "班组编号（queryType=TEAM 时使用）", example = "201")
    private Long teamId;

    @Schema(description = "用户编号（queryType=USER 时使用）", example = "1")
    private Long userId;

}
