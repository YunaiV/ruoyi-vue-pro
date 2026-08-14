package cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 初始余额 Response VO")
@Data
public class FmsInitialBalanceRespVO {

    @Schema(description = "初始余额编号", example = "1024")
    private Long id;

    @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long subjectId;

    @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subjectCode;

    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subjectName;

    @Schema(description = "上级科目编号", example = "1024")
    private Long parentId;

    @Schema(description = "科目类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "余额方向", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer balanceDirection;

    @Schema(description = "是否启用数量核算", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean quantityAccounting;

    @Schema(description = "数量单位")
    private String quantityUnit;

    @Schema(description = "是否启用辅助核算", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean auxiliaryAccounting;

    @Schema(description = "期初金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    private BigDecimal openingAmount;

    @Schema(description = "期初数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.0000")
    private BigDecimal openingQuantity;

    @Schema(description = "本年累计借方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal yearDebitAmount;

    @Schema(description = "本年累计借方数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0000")
    private BigDecimal yearDebitQuantity;

    @Schema(description = "本年累计贷方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.00")
    private BigDecimal yearCreditAmount;

    @Schema(description = "本年累计贷方数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0000")
    private BigDecimal yearCreditQuantity;

    @Schema(description = "年初金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "950.00")
    private BigDecimal yearOpeningAmount;

    @Schema(description = "年初数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.0000")
    private BigDecimal yearOpeningQuantity;

    @Schema(description = "实际损益发生额", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.00")
    private BigDecimal profitLossAmount;

    @Schema(description = "实际损益发生数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0000")
    private BigDecimal profitLossQuantity;

    @Schema(description = "辅助核算配置数组")
    private List<AuxiliaryConfig> auxiliaryConfigs;

    @Schema(description = "辅助核算余额数组")
    private List<AssistBalance> assistBalances;

    @Schema(description = "管理后台 - FMS 初始余额辅助核算配置")
    @Data
    public static class AuxiliaryConfig {

        @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long auxiliaryTypeId;

        @Schema(description = "辅助核算类型", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer type;

        @Schema(description = "辅助核算类别名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

    }

    @Schema(description = "管理后台 - FMS 初始余额辅助核算余额")
    @Data
    public static class AssistBalance {

        @Schema(description = "辅助核算组合编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long assistCombinationId;

        @Schema(description = "辅助核算项目数组", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<AuxiliaryItem> auxiliaries;

        @Schema(description = "期初金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
        private BigDecimal openingAmount;

        @Schema(description = "期初数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.0000")
        private BigDecimal openingQuantity;

        @Schema(description = "本年累计借方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal yearDebitAmount;

        @Schema(description = "本年累计借方数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0000")
        private BigDecimal yearDebitQuantity;

        @Schema(description = "本年累计贷方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.00")
        private BigDecimal yearCreditAmount;

        @Schema(description = "本年累计贷方数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0000")
        private BigDecimal yearCreditQuantity;

        @Schema(description = "年初金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "950.00")
        private BigDecimal yearOpeningAmount;

        @Schema(description = "年初数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.0000")
        private BigDecimal yearOpeningQuantity;

        @Schema(description = "实际损益发生额", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.00")
        private BigDecimal profitLossAmount;

        @Schema(description = "实际损益发生数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0000")
        private BigDecimal profitLossQuantity;

    }

    @Schema(description = "管理后台 - FMS 初始余额辅助核算项目")
    @Data
    public static class AuxiliaryItem {

        @Schema(description = "辅助核算类型", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer type;

        @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long typeId;

        @Schema(description = "辅助核算项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long itemId;

        @Schema(description = "辅助核算项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

    }

}
