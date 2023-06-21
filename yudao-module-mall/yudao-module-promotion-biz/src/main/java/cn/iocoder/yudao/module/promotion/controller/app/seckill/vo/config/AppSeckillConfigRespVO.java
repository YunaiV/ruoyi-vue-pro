package cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 秒杀时间段 Response VO")
@Data
public class AppSeckillConfigRespVO {

    @Schema(description = "秒杀时间段编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "开始时间点", requiredMode = Schema.RequiredMode.REQUIRED, example = "09:00")
    private String startTime;
    @Schema(description = "结束时间点", requiredMode = Schema.RequiredMode.REQUIRED, example = "09:59")
    private String endTime;

    @Schema(description = "轮播图", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> sliderPicUrls;

}
