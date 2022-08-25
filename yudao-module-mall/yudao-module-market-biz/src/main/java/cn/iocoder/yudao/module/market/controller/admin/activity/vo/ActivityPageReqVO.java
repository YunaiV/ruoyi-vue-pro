package cn.iocoder.yudao.module.market.controller.admin.activity.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.market.enums.activity.MarketActivityStatusEnum;
import cn.iocoder.yudao.module.market.enums.activity.MarketActivityTypeEnum;
import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 促销活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ActivityPageReqVO extends PageParam {

    @ApiModelProperty(value = "活动标题")
    private String title;

    @ApiModelProperty(value = "活动类型")
    @InEnum(MarketActivityTypeEnum.class)
    private Integer activityType;

    @ApiModelProperty(value = "活动状态")
    @InEnum(MarketActivityStatusEnum.class)
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始时间")
    private Date[] startTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束时间")
    private Date[] endTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "失效时间")
    private Date[] invalidTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "删除时间")
    private Date[] deleteTime;

    @ApiModelProperty(value = "限制折扣字符串，使用 JSON 序列化成字符串存储")
    private String timeLimitedDiscount;

    @ApiModelProperty(value = "限制折扣字符串，使用 JSON 序列化成字符串存储")
    private String fullPrivilege;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "创建时间")
    private Date[] createTime;

}
