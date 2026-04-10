package cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.shift;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Collection;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 班组排班列表 Request VO")
@Data
public class MesCalTeamShiftListReqVO {

    @Schema(description = "班组编号", example = "201")
    private Long teamId;

    @Schema(description = "班组编号集合")
    private Collection<Long> teamIds;

    @Schema(description = "排班计划编号", example = "1")
    private Long planId;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startDay;

    @Schema(description = "结束日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endDay;

}
