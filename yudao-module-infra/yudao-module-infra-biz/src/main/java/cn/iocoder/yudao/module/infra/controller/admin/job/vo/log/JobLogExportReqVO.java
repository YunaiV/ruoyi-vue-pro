package cn.iocoder.yudao.module.infra.controller.admin.job.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 定时任务 Excel 导出 Request VO,参数和 JobLogPageReqVO 是一致的")
@Data
public class JobLogExportReqVO {

    @Schema(description = "任务编号", example = "10")
    private Long jobId;

    @Schema(description = "处理器的名字,模糊匹配")
    private String handlerName;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "开始执行时间")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "结束执行时间")
    private LocalDateTime endTime;

    @Schema(description = "任务状态,参见 JobLogStatusEnum 枚举")
    private Integer status;

}
