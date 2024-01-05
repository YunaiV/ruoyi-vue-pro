package cn.iocoder.yudao.module.promotion.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 营销活动 Response VO")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppActivityRespVO {

    @Schema(description = "活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "活动类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type; // 对应 PromotionTypeEnum 枚举

    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "618 大促")
    private String name;

    @Schema(description = "spu 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "618")
    private Long spuId;

    @Schema(description = "活动开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

}
