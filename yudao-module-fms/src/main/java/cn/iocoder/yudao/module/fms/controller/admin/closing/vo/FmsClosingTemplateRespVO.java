package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - FMS 结账模板 Response VO")
@Data
public class FmsClosingTemplateRespVO {

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long accountSetId;

    @Schema(description = "系统预置编码", example = "daily-travel-reimbursement")
    private String presetCode;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "报销差旅费")
    private String name;

    @Schema(description = "模板分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer category;

    @Schema(description = "是否期末结转", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean periodEnd;

    @Schema(description = "来源科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "取数规则", example = "0")
    private Integer formulaRule;

    @Schema(description = "取数时间类型", example = "1")
    private Integer timeType;

    @Schema(description = "结转科目规则数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SubjectRule> subjects;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "管理后台 - FMS 结账模板科目规则")
    @Data
    public static class SubjectRule {

        @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long subjectId;

        @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "560107")
        private String subjectCode;

        @Schema(description = "摘要", requiredMode = Schema.RequiredMode.REQUIRED, example = "报销差旅费")
        private String digest;

        @Schema(description = "借贷方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer direction;

        @Schema(description = "金额比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private BigDecimal amountRatio;
    }

}
