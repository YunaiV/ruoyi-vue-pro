package cn.iocoder.dashboard.modules.infra.controller.job.vo.job;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

import static cn.iocoder.dashboard.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 定时任务 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class InfJobBaseVO {

    @ApiModelProperty(value = "任务名称", required = true, example = "测试任务")
    @NotNull(message = "任务名称不能为空")
    private String name;

    @ApiModelProperty(value = "任务状态", required = true, example = "1", notes = "参见 InfJobStatusEnum 枚举")
    @NotNull(message = "任务状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "处理器的名字", required = true, example = "sysUserSessionTimeoutJob")
    @NotNull(message = "处理器的名字不能为空")
    private String handlerName;

    @ApiModelProperty(value = "处理器的参数", example = "yudao")
    private String handlerParam;

    @ApiModelProperty(value = "CRON 表达式", required = true, example = "0/10 * * * * ? *")
    @NotNull(message = "CRON 表达式不能为空")
    private String cronExpression;

    @ApiModelProperty(value = "最后一次执行的开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date executeBeginTime;

    @ApiModelProperty(value = "最后一次执行的结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date executeEndTime;

    @ApiModelProperty(value = "上一次触发时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date firePrevTime;

    @ApiModelProperty(value = "下一次触发时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date fireNextTime;

    @ApiModelProperty(value = "监控超时时间", example = "1000")
    private Integer monitorTimeout;

}
