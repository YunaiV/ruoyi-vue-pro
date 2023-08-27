package cn.iocoder.yudao.module.mp.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 某一天的粉丝增减数据 Response VO")
@Data
public class MpStatisticsUserSummaryRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime refDate;

    @Schema(description = "粉丝来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer userSource;

    @Schema(description = "新关注的粉丝数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer newUser;

    @Schema(description = "取消关注的粉丝数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer cancelUser;

}
