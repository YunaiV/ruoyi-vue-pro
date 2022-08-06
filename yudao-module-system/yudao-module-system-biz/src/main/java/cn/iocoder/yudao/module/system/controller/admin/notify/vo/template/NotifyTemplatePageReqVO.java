package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 站内信模版分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyTemplatePageReqVO extends PageParam {

    @ApiModelProperty(value = "模版编码")
    private String code;

    @ApiModelProperty(value = "模版标题")
    private String title;

    @ApiModelProperty(value = "状态：1-启用 0-禁用")
    private String status;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;

}
