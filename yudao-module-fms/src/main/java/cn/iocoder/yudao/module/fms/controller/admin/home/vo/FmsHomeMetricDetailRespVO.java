package cn.iocoder.yudao.module.fms.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 首页财务指标明细 Response VO")
@Data
public class FmsHomeMetricDetailRespVO {

    @Schema(description = "指标标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "income")
    private String key;

    @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "收入")
    private String name;

    @Schema(description = "财务指标趋势数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Trend> trends;

    @Schema(description = "当期科目构成数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Structure> structure;

    @Schema(description = "管理后台 - FMS 首页财务指标趋势 Response VO")
    @Data
    public static class Trend {

        @Schema(description = "会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
        private String month;

        @Schema(description = "指标金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000.00")
        private BigDecimal amount;

    }

    @Schema(description = "管理后台 - FMS 首页财务指标科目构成 Response VO")
    @Data
    public static class Structure {

        @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long subjectId;

        @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "5001")
        private String subjectCode;

        @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "主营业务收入")
        private String subjectName;

        @Schema(description = "科目金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "8000.00")
        private BigDecimal amount;

    }

}
