package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 定时任务更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobUpdateReqVO extends JobBaseVO {

    @ApiModelProperty(value = "任务编号", required = true, example = "1024")
    @NotNull(message = "任务编号不能为空")
    private Long id;

}
