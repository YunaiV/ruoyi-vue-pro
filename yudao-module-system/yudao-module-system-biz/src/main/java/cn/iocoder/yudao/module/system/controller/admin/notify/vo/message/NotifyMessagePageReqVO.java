package cn.iocoder.yudao.module.system.controller.admin.notify.vo.message;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 站内信分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyMessagePageReqVO extends PageParam {

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "是否已读 0-未读  1-已读")
    private Boolean readStatus;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;


    @ApiModelProperty(value = "用户编号", hidden = true)
    private Long userId;

    @ApiModelProperty(value = "用户类型", hidden = true)
    private Integer userType;
}
