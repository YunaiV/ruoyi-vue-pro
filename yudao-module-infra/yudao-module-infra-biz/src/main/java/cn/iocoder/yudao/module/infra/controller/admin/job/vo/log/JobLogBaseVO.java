package cn.iocoder.yudao.module.infra.controller.admin.job.vo.log;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 定时任务日志 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class JobLogBaseVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "1024")
    @NotNull(message = "任务编号不能为空")
    private Long jobId;

    @ApiModelProperty(value = "处理器的名字", required = true, example = "sysUserSessionTimeoutJob")
    @NotNull(message = "处理器的名字不能为空")
    private String handlerName;

    @ApiModelProperty(value = "处理器的参数", example = "yudao")
    private String handlerParam;

    @ApiModelProperty(value = "第几次执行", required = true, example = "1")
    @NotNull(message = "第几次执行不能为空")
    private Integer executeIndex;

    @ApiModelProperty(value = "开始执行时间", required = true)
    @NotNull(message = "开始执行时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "结束执行时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @ApiModelProperty(value = "执行时长", example = "123")
    private Integer duration;

    @ApiModelProperty(value = "任务状态", required = true, example = "1", notes = "参见 JobLogStatusEnum 枚举")
    @NotNull(message = "任务状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "结果数据", example = "执行成功")
    private String result;

}
