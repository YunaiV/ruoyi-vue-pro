package cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("请假申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaLeavePageReqVO extends PageParam {

    @ApiModelProperty(value = "流程id")
    private String processInstanceId;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "申请人id")
    private String userId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始开始时间")
    private Date beginStartTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束开始时间")
    private Date endStartTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始结束时间")
    private Date beginEndTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束结束时间")
    private Date endEndTime;

    @ApiModelProperty(value = "请假类型")
    private String leaveType;

    @ApiModelProperty(value = "原因")
    private String reason;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始申请时间")
    private Date beginApplyTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束申请时间")
    private Date endApplyTime;

}
