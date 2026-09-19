package cn.iocoder.yudao.module.oa.controller.admin.schedule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaScheduleTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 日程分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaSchedulePageReqVO extends PageParam {

    @Schema(description = "是否包含我的日程", example = "true")
    private Boolean includeMine;

    @Schema(description = "是否包含共享给我的日程", example = "true")
    private Boolean includeReceived;

    @Schema(description = "标题", example = "周会")
    private String title;

    @Schema(description = "日程类型", example = "1")
    @InEnum(value = OaScheduleTypeEnum.class, message = "日程类型必须是 {value}")
    private Integer type;

    @Schema(description = "优先级", example = "1")
    @InEnum(value = OaPriorityEnum.class, message = "优先级必须是 {value}")
    private Integer priority;

    @Schema(description = "开始时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "日历显示时间范围，查询与该范围相交的日程", example = "[\"2026-09-01 00:00:00\", \"2026-09-30 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Size(min = 2, max = 2)
    private LocalDateTime[] overlapTime;

}
