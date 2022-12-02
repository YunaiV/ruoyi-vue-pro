package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import lombok.*;

import java.time.LocalTime;

import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 秒杀时段 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SeckillTimeBaseVO {

    @ApiModelProperty(value = "秒杀时段名称", required = true)
    @NotNull(message = "秒杀时段名称不能为空")
    private String name;

    @ApiModelProperty(value = "开始时间点", required = true)
    @NotNull(message = "开始时间点不能为空")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间点", required = true)
    @NotNull(message = "结束时间点不能为空")
    private LocalTime endTime;

}
