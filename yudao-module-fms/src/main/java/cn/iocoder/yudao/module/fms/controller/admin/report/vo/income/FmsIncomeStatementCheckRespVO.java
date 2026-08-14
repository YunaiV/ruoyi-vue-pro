package cn.iocoder.yudao.module.fms.controller.admin.report.vo.income;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 利润表检查 Response VO")
@Data
public class FmsIncomeStatementCheckRespVO {

    @Schema(description = "净利润与未分配利润变动是否一致", example = "true")
    private Boolean balanced;

    @Schema(description = "利润表与资产负债表勾稽差额", example = "0.00")
    private BigDecimal differenceAmount;

    @Schema(description = "未纳入报表公式的科目")
    private List<UnmappedSubject> unmappedSubjects;

    @Schema(description = "管理后台 - FMS 未纳入报表公式的科目")
    @Data
    public static class UnmappedSubject {

        @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
        private String code;

        @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "库存现金")
        private String name;

    }

}
