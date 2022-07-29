package cn.iocoder.yudao.module.bpm.controller.admin.oa.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 请假申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmOALeavePageReqVO extends PageParam {

    @ApiModelProperty(value = "状态", example = "1", notes = "参见 bpm_process_instance_result 枚举")
    private Integer result;

    @ApiModelProperty(value = "请假类型", example = "1", notes = "参见 bpm_oa_type")
    private Integer type;

    @ApiModelProperty(value = "原因", example = "阅读芋道源码", notes = "模糊匹配")
    private String reason;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "申请时间")
    private Date[] createTime;

}
