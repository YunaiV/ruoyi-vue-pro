package cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("动态表单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfFormPageReqVO extends PageParam {

    @ApiModelProperty(value = "表单名称")
    private String name;

    @ApiModelProperty(value = "商户状态")
    private Integer status;

    @ApiModelProperty(value = "表单JSON")
    private String formJson;

    @ApiModelProperty(value = "备注")
    private String remark;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
