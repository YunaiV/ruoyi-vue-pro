package cn.iocoder.yudao.module.mp.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 某一天的接口分析数据 Response VO")
@Data
public class MpStatisticsInterfaceSummaryRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime refDate;

    @Schema(description = "通过服务器配置地址获得消息后，被动回复粉丝消息的次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer callbackCount;

    @Schema(description = "上述动作的失败次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer failCount;

    @Schema(description = "总耗时，除以 callback_count 即为平均耗时", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalTimeCost;

    @Schema(description = "最大耗时", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    private Integer maxTimeCost;

}
