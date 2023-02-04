package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * 秒杀时段 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SeckillTimeBaseVO {

    @Schema(description = "秒杀时段名称", required = true, example = "上午场")
    @NotNull(message = "秒杀时段名称不能为空")
    private String name;

    @Schema(description = "开始时间点", required = true, example = "16:30:40")
    @NotNull(message = "开始时间点不能为空")
    private LocalTime startTime;

    @Schema(description = "结束时间点", required = true, example = "16:30:40")
    @NotNull(message = "结束时间点不能为空")
    private LocalTime endTime;

}
