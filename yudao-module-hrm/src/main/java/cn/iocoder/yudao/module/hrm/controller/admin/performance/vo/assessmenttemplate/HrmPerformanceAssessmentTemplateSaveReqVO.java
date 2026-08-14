package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaScoreTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceQuotaTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceScoreCalculationEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceUpperLimitTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.PERCENT_100;

@Schema(description = "管理后台 - HRM 绩效考核模板保存 Request VO")
@Data
public class HrmPerformanceAssessmentTemplateSaveReqVO {

    @Schema(description = "模板编号", example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "季度绩效模板")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 50, message = "模板名称不能超过 50 个字符")
    private String name;

    @Schema(description = "模板说明", example = "适用于季度绩效考核")
    @Size(max = 200, message = "模板说明不能超过 200 个字符")
    private String illustrate;

    @Schema(description = "计分方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计分方式不能为空")
    @InEnum(value = HrmPerformanceScoreCalculationEnum.class, message = "计分方式必须是 {value}")
    private Integer scoreCalculation;

    @Schema(description = "分数上限类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分数上限类型不能为空")
    @InEnum(value = HrmPerformanceUpperLimitTypeEnum.class, message = "分数上限类型必须是 {value}")
    private Integer upperLimitType;

    @Schema(description = "分数上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "分数上限不能为空")
    @DecimalMin(value = "0", message = "分数上限不能小于 0")
    @DecimalMax(value = "100", message = "分数上限不能大于 100")
    private BigDecimal upperLimitScore;

    @Schema(description = "考核维度列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "至少需要一个考核维度")
    private List<@NotNull(message = "考核维度不能为空") Dimension> dimensions;

    @AssertTrue(message = "考核维度名称不能重复")
    @JsonIgnore
    public boolean isDimensionNamesUnique() {
        if (CollUtil.isEmpty(dimensions)) {
            return true;
        }
        Set<String> names = new HashSet<>();
        for (Dimension dimension : dimensions) {
            if (dimension != null && StrUtil.isNotBlank(dimension.getName())
                    && !names.add(dimension.getName().trim())) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "考核指标名称不能重复")
    @JsonIgnore
    public boolean isQuotaNamesUnique() {
        if (CollUtil.isEmpty(dimensions)) {
            return true;
        }
        Set<String> names = new HashSet<>();
        for (Dimension dimension : dimensions) {
            if (dimension == null || CollUtil.isEmpty(dimension.getQuotas())) {
                continue;
            }
            for (Quota quota : dimension.getQuotas()) {
                if (quota != null && StrUtil.isNotBlank(quota.getName())
                        && !names.add(quota.getName().trim())) {
                    return false;
                }
            }
        }
        return true;
    }

    @AssertTrue(message = "维度权重总和必须等于 100%")
    @JsonIgnore
    public boolean isDimensionWeightTotalValid() {
        if (CollUtil.isEmpty(dimensions)) {
            return true;
        }
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (Dimension dimension : dimensions) {
            if (dimension == null || dimension.getWeight() == null) {
                return true;
            }
            totalWeight = totalWeight.add(dimension.getWeight());
        }
        return totalWeight.compareTo(PERCENT_100) == 0;
    }

    @AssertTrue(message = "不可编辑维度的指标权重总和必须等于 100%")
    @JsonIgnore
    public boolean isFixedQuotaWeightTotalValid() {
        if (CollUtil.isEmpty(dimensions)) {
            return true;
        }
        for (Dimension dimension : dimensions) {
            if (dimension == null || ObjUtil.notEqual(Boolean.FALSE, dimension.getAllowEdit())
                    || CollUtil.isEmpty(dimension.getQuotas())) {
                continue;
            }
            BigDecimal totalWeight = BigDecimal.ZERO;
            for (Quota quota : dimension.getQuotas()) {
                if (quota == null || quota.getWeight() == null) {
                    return true;
                }
                totalWeight = totalWeight.add(quota.getWeight());
            }
            if (totalWeight.compareTo(PERCENT_100) != 0) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "可编辑维度的指标权重总和不能大于 100%")
    @JsonIgnore
    public boolean isEditableQuotaWeightTotalValid() {
        if (CollUtil.isEmpty(dimensions)) {
            return true;
        }
        for (Dimension dimension : dimensions) {
            if (dimension == null || ObjUtil.notEqual(Boolean.TRUE, dimension.getAllowEdit())
                    || CollUtil.isEmpty(dimension.getQuotas())) {
                continue;
            }
            BigDecimal totalWeight = BigDecimal.ZERO;
            for (Quota quota : dimension.getQuotas()) {
                if (quota == null || quota.getWeight() == null) {
                    return true;
                }
                totalWeight = totalWeight.add(quota.getWeight());
            }
            if (totalWeight.compareTo(PERCENT_100) > 0) {
                return false;
            }
        }
        return true;
    }

    @Schema(description = "管理后台 - HRM 绩效考核模板维度")
    @Data
    public static class Dimension {

        @Schema(description = "维度名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工作业绩")
        @NotBlank(message = "维度名称不能为空")
        @Size(max = 50, message = "维度名称不能超过 50 个字符")
        private String name;

        @Schema(description = "指标类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "指标类型不能为空")
        @InEnum(value = HrmPerformanceQuotaTypeEnum.class, message = "指标类型必须是 {value}")
        private Integer quotaType;

        @Schema(description = "维度权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        @NotNull(message = "维度权重不能为空")
        @DecimalMin(value = "0", message = "维度权重不能小于 0")
        @DecimalMax(value = "100", message = "维度权重不能大于 100")
        private BigDecimal weight;

        @Schema(description = "备注", example = "核心业绩指标")
        @Size(max = 200, message = "维度备注不能超过 200 个字符")
        private String remark;

        @Schema(description = "是否允许员工编辑", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "是否允许员工编辑不能为空")
        private Boolean allowEdit;

        @Schema(description = "考核指标列表", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotEmpty(message = "每个考核维度至少需要一个指标")
        private List<@NotNull(message = "考核指标不能为空") Quota> quotas;
    }

    @Schema(description = "管理后台 - HRM 绩效考核模板指标")
    @Data
    public static class Quota {

        @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "销售目标达成率")
        @NotBlank(message = "指标名称不能为空")
        @Size(max = 50, message = "指标名称不能超过 50 个字符")
        private String name;

        @Schema(description = "指标说明", example = "按季度销售目标计算")
        @Size(max = 200, message = "指标说明不能超过 200 个字符")
        private String illustrate;

        @Schema(description = "评分标准", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成率达到 100% 得满分")
        @NotBlank(message = "评分标准不能为空")
        @Size(max = 200, message = "评分标准不能超过 200 个字符")
        private String standard;

        @Schema(description = "指标权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        @NotNull(message = "指标权重不能为空")
        @DecimalMin(value = "0", message = "指标权重不能小于 0")
        @DecimalMax(value = "100", message = "指标权重不能大于 100")
        private BigDecimal weight;

        @Schema(description = "评分类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "评分类型不能为空")
        @InEnum(value = HrmPerformanceQuotaScoreTypeEnum.class, message = "评分类型必须是 {value}")
        private Integer scoreType;
    }

}
