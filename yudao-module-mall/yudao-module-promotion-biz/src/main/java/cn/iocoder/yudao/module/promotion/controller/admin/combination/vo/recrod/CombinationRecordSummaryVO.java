package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 拼团记录信息统计 Response VO")
@Data
public class CombinationRecordSummaryVO {

    @Schema(description = "所有拼团记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userCount;

    @Schema(description = "成团记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long successCount;

    @Schema(description = "虚拟成团记录", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long virtualGroupCount;

}
