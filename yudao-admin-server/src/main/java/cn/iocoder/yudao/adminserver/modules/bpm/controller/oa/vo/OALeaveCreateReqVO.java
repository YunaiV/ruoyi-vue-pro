package cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo;

import lombok.*;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("请假申请创建 Request VO")
@Data
public class OALeaveCreateReqVO {

    @ApiModelProperty(value = "请假的开始时间", required = true)
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date startTime;
    @ApiModelProperty(value = "请假的结束时间", required = true)
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date endTime;

    @ApiModelProperty(value = "请假类型", required = true, example = "1", notes = "参见 bpm_oa_type")
    private Integer type;

    @ApiModelProperty(value = "原因", required = true, example = "阅读芋道源码")
    private String reason;

}
