package cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 秒杀时间段 Response VO")
@Data
public class AppSeckillConfigRespVO {

    @Schema(description = "秒杀时间段编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "开始时间点", required = true, example = "09:00")
    private String startTime;
    @Schema(description = "结束时间点", required = true, example = "09:59")
    private String endTime;

}
