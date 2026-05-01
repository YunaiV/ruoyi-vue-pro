package cn.iocoder.yudao.module.im.controller.admin.manager.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - IM 数据看板趋势 Response VO")
@Data
public class ImStatisticsManagerTrendRespVO {

    @Schema(description = "横轴日期序列（每天 00:00:00，由前端按需取日期部分）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<LocalDateTime> dates;

    @Schema(description = "数据系列：key 为系列名（如 private/group 或 register/active），value 为与 dates 等长的计数数组",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, List<Long>> series;

}
