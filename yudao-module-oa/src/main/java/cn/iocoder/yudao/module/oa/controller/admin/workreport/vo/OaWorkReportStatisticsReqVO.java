package cn.iocoder.yudao.module.oa.controller.admin.workreport.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 工作汇报统计 Request VO")
@Data
public class OaWorkReportStatisticsReqVO {

    @Schema(description = "汇报类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "汇报类型不能为空")
    @InEnum(value = OaWorkReportTypeEnum.class, message = "汇报类型必须是 {value}")
    private Integer type;

    @Schema(description = "统计开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-01 00:00:00")
    @NotNull(message = "统计开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-30 23:59:59")
    @NotNull(message = "统计结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "完整周期查询开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-01 00:00:00")
    @NotNull(message = "查询开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime queryStartTime;

    @Schema(description = "完整周期查询结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-30 23:59:59")
    @NotNull(message = "查询结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime queryEndTime;

    @Schema(description = "部门编号", example = "103")
    private Long deptId;

    @JsonIgnore
    @AssertTrue(message = "统计结束时间不能早于开始时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }

}
