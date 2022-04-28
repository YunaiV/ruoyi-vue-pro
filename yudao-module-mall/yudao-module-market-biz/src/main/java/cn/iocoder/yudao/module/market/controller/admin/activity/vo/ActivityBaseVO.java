package cn.iocoder.yudao.module.market.controller.admin.activity.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.market.enums.activity.MarketActivityStatusEnum;
import cn.iocoder.yudao.module.market.enums.activity.MarketActivityTypeEnum;
import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 促销活动 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ActivityBaseVO {

    @ApiModelProperty(value = "活动标题", required = true)
    @NotNull(message = "活动标题不能为空")
    private String title;

    @ApiModelProperty(value = "活动类型", required = true)
    @NotNull(message = "活动类型不能为空")
    @InEnum(MarketActivityTypeEnum.class)
    private Integer activityType;

    @ApiModelProperty(value = "活动状态", required = true)
    @NotNull(message = "活动状态不能为空")
    @InEnum(MarketActivityStatusEnum.class)
    private Integer status;

    @ApiModelProperty(value = "开始时间", required = true)
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date startTime;

    @ApiModelProperty(value = "结束时间", required = true)
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date endTime;

    @ApiModelProperty(value = "失效时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date invalidTime;

    @ApiModelProperty(value = "删除时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date deleteTime;

    @ApiModelProperty(value = "限制折扣字符串，使用 JSON 序列化成字符串存储")
    private String timeLimitedDiscount;

    @ApiModelProperty(value = "限制折扣字符串，使用 JSON 序列化成字符串存储")
    private String fullPrivilege;

}
