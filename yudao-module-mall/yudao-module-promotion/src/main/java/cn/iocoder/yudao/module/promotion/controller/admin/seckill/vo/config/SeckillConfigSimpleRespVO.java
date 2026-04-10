package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 秒杀时段配置精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillConfigSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "秒杀时段名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "早上场")
    @NotNull(message = "秒杀时段名称不能为空")
    private String name;

    @Schema(description = "开始时间点", requiredMode = Schema.RequiredMode.REQUIRED, example = "09:00:00")
    private String startTime;

    @Schema(description = "结束时间点", requiredMode = Schema.RequiredMode.REQUIRED, example = "16:00:00")
    private String endTime;

}
