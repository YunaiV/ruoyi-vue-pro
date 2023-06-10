package cn.iocoder.yudao.module.promotion.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 营销活动 Response VO")
@Data
public class AppActivityRespVO {

    @Schema(description = "活动编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "活动类型", required = true, example = "1") // 对应 PromotionTypeEnum 枚举
    private Integer type;

    @Schema(description = "活动名称", required = true, example = "618 大促")
    private String name;

    @Schema(description = "活动开始时间", required = true)
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", required = true)
    private LocalDateTime endTime;

}
