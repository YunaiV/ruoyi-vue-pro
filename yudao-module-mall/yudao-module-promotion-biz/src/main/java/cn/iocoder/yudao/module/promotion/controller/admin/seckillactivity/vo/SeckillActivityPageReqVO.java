package cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 秒杀活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityPageReqVO extends PageParam {

    @ApiModelProperty(value = "秒杀活动名称", example = "晚九点限时秒杀")
    private String name;

    @ApiModelProperty(value = "活动状态", example = "进行中")
    private Integer status;

    @ApiModelProperty(value = "秒杀时段id")
    private Long timeId;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date[] createTime;

}
