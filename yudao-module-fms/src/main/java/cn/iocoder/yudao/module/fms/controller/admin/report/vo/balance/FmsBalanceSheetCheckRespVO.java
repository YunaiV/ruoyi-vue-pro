package cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 资产负债表检查 Response VO")
@Data
public class FmsBalanceSheetCheckRespVO {

    @Schema(description = "报表是否平衡", example = "true")
    private Boolean balanced;

    @Schema(description = "初始余额是否平衡", example = "true")
    private Boolean initialBalanceBalanced;

    @Schema(description = "损益是否已结转", example = "true")
    private Boolean profitLossTransferred;

    @Schema(description = "年初余额差额", example = "0.00")
    private BigDecimal openingDifferenceAmount;

    @Schema(description = "期末余额差额", example = "0.00")
    private BigDecimal closingDifferenceAmount;

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
