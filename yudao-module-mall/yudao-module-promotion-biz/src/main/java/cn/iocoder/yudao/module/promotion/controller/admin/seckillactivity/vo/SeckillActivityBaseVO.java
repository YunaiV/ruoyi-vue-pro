package cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 秒杀活动 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SeckillActivityBaseVO {

    @ApiModelProperty(value = "秒杀活动名称", required = true, example = "晚九点限时秒杀")
    @NotNull(message = "秒杀活动名称不能为空")
    private String name;

    @ApiModelProperty(value = "活动状态", required = true, example = "进行中")
    @NotNull(message = "活动状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "活动开始时间", required = true)
    @NotNull(message = "活动开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间", required = true)
    @NotNull(message = "活动结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Date endTime;

    @ApiModelProperty(value = "订单实付金额，单位：分", required = true)
    @NotNull(message = "订单实付金额，单位：分不能为空")
    private BigDecimal totalPrice;

}
