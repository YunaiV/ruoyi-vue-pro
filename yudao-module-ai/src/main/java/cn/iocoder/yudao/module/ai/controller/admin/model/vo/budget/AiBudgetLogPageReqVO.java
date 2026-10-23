package cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 预算事件日志分页 Request VO")
@Data
public class AiBudgetLogPageReqVO extends PageParam {

    @Schema(description = "用户编号，0 表示租户级", example = "0")
    private Long userId;

    @Schema(description = "事件类型", example = "THRESHOLD_ALERT")
    private String eventType;

    @Schema(description = "周期开始时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] periodStartTime;

}
