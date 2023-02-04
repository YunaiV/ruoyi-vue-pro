package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Schema(description = "管理后台 - 秒杀时段分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimePageReqVO extends PageParam {

    @Schema(description = "秒杀时段名称", example = "上午场")
    private String name;

    @Schema(description = "开始时间点", example = "16:30:40")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Schema(description = "结束时间点", example = "16:30:40")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

}
