package cn.iocoder.yudao.module.infra.controller.admin.job.vo.log;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 定时任务日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobLogPageReqVO extends PageParam {

    @ApiModelProperty(value = "任务编号", example = "10")
    private Long jobId;

    @ApiModelProperty(value = "处理器的名字", notes = "模糊匹配")
    private String handlerName;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始执行时间")
    private Date beginTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束执行时间")
    private Date endTime;

    @ApiModelProperty(value = "任务状态", notes = "参见 JobLogStatusEnum 枚举")
    private Integer status;

}
