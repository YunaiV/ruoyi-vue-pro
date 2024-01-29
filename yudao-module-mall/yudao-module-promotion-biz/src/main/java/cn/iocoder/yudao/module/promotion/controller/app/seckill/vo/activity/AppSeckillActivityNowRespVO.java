package cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.config.AppSeckillConfigRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 当前秒杀活动 Response VO")
@Data
public class AppSeckillActivityNowRespVO {

    @Schema(description = "秒杀时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private AppSeckillConfigRespVO config;

    @Schema(description = "秒杀活动数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AppSeckillActivityRespVO> activities;

}
