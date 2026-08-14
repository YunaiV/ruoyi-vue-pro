package cn.iocoder.yudao.module.fms.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 首页 Response VO")
@Data
public class FmsHomeRespVO {

    @Schema(description = "当前会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    private String currentMonth;

    @Schema(description = "当期财务指标数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Metric> metrics;

    @Schema(description = "财务指标趋势数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Trend> trends;

    @Schema(description = "管理后台 - FMS 首页财务指标 Response VO")
    @Data
    public static class Metric {

        @Schema(description = "指标标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "income")
        private String key;

        @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "收入")
        private String name;

        @Schema(description = "指标金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000.00")
        private BigDecimal amount;

    }

    @Schema(description = "管理后台 - FMS 首页财务指标趋势 Response VO")
    @Data
    public static class Trend {

        @Schema(description = "会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
        private String month;

        @Schema(description = "动态财务指标数组", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Metric> metrics;

        @Schema(description = "收入", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal income;

        @Schema(description = "成本", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal operatingCost;

        @Schema(description = "利润", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal profit;

        @Schema(description = "费用", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal expense;

        @Schema(description = "其他", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal other;

    }

}
