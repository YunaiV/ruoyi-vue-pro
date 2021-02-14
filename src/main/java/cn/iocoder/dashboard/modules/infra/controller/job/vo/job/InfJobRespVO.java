package cn.iocoder.dashboard.modules.infra.controller.job.vo.job;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;

@ApiModel("定时任务 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InfJobRespVO extends InfJobBaseVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "任务状态", required = true, example = "1")
    private Integer status;

    @ApiModelProperty(value = "处理器的名字", required = true, example = "sysUserSessionTimeoutJob")
    @NotNull(message = "处理器的名字不能为空")
    private String handlerName;

    @ApiModelProperty(value = "最后一次执行的开始时间")
    private Date executeBeginTime;

    @ApiModelProperty(value = "最后一次执行的结束时间")
    private Date executeEndTime;

    @ApiModelProperty(value = "上一次触发时间")
    private Date firePrevTime;

    @ApiModelProperty(value = "下一次触发时间")
    private Date fireNextTime;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
