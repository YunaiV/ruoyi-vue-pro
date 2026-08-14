package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 结账方案 Response VO")
@Data
public class FmsClosingSchemeRespVO {

    @Schema(description = "方案编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", example = "1")
    private Long accountSetId;

    @Schema(description = "方案名称", example = "结转本月房租")
    private String name;

    @Schema(description = "是否期末结转")
    private Boolean periodEnd;

    @Schema(description = "来源科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "取数规则", example = "0")
    private Integer formulaRule;

    @Schema(description = "取数时间类型", example = "1")
    private Integer timeType;

    @Schema(description = "凭证字编号", example = "11")
    private Long voucherWordId;

    @Schema(description = "凭证摘要", example = "结转损益")
    private String digest;

    @Schema(description = "结转凭证类型", example = "2")
    private Integer voucherType;

    @Schema(description = "以前年度损益调整科目编号", example = "1024")
    private Long priorYearAdjustmentSubjectId;

    @Schema(description = "以前年度损益调整结转科目编号", example = "1025")
    private Long adjustmentClosingSubjectId;

    @Schema(description = "其他损益结转科目编号", example = "1026")
    private Long otherClosingSubjectId;

    @Schema(description = "是否按余额反向结转")
    private Boolean reverseBalance;

    @Schema(description = "结转日期", example = "31")
    private Integer closingDay;

    @Schema(description = "类型", example = "1")
    private Integer type;

    @Schema(description = "结转科目规则数组")
    private List<SubjectRule> subjects;

    @Schema(description = "待结转金额", example = "12000.00")
    private BigDecimal balance;

    @Schema(description = "当前期间已生成凭证编号数组")
    private List<Long> voucherIds;

    @Schema(description = "管理后台 - FMS 结账方案科目规则")
    @Data
    public static class SubjectRule {

        @Schema(description = "科目编号", example = "1024")
        private Long subjectId;

        @Schema(description = "科目编码快照", example = "6602")
        private String subjectCode;

        @Schema(description = "摘要", example = "结转本月房租")
        private String digest;

        @Schema(description = "借贷方向", example = "1")
        private Integer direction;

        @Schema(description = "金额比例", example = "100")
        private BigDecimal amountRatio;
    }

}
