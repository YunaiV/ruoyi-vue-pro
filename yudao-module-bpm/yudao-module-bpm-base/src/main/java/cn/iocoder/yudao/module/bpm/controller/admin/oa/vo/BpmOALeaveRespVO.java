package cn.iocoder.yudao.module.bpm.controller.admin.oa.vo;

import lombok.*;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 请假申请 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmOALeaveRespVO extends BpmOALeaveBaseVO {

    @ApiModelProperty(value = "请假表单主键", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "参见 bpm_process_instance_result 枚举")
    private Integer result;

    @ApiModelProperty(value = "申请时间", required = true)
    @NotNull(message = "申请时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date createTime;

    @ApiModelProperty(value = "流程id")
    private String processInstanceId;

}
