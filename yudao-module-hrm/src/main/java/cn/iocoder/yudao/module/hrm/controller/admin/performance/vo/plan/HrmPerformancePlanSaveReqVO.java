package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplateSaveReqVO.Dimension;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplateSaveReqVO.Quota;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplateSaveReqVO.Level;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealTimeoutActionEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanScopeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuarterEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaSettingTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewScoringTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewVisibleContentEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceScoreCalculationEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceUpperLimitTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.PERCENT_100;

@Schema(description = "管理后台 - HRM 绩效计划保存 Request VO")
@Data
public class HrmPerformancePlanSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "绩效计划名称", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026 年第三季度绩效计划")
    @NotBlank(message = "计划名称不能为空")
    @Size(max = 50, message = "计划名称不能超过 50 个字符")
    private String name;

    @Schema(description = "考核周期类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "考核周期类型不能为空")
    @InEnum(value = HrmPerformanceCycleTypeEnum.class, message = "考核周期类型必须是 {value}")
    private Integer cycleType;

    @Schema(description = "考核周期", example = "2026-08")
    @Size(max = 255, message = "考核周期不能超过 255 个字符")
    private String cycle;

    @Schema(description = "季度", example = "3")
    @InEnum(value = HrmPerformanceQuarterEnum.class, message = "季度必须是 {value}")
    private Integer quarter;

    @Schema(description = "开始日期")
    private LocalDateTime startTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

    @Schema(description = "考核说明", example = "2026 年第三季度绩效考核")
    @Size(max = 200, message = "考核说明不能超过 200 个字符")
    private String description;

    @Schema(description = "考评范围列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "考评范围不能为空")
    @Size(max = 3, message = "考评范围不能超过 3 个")
    private List<@NotNull(message = "考评范围不能为空") Scope> scopes;

    @Schema(description = "考核模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "考核模板不能为空")
    private Long assessmentTemplateId;

    @Schema(description = "考核配置快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "考核配置不能为空")
    private AssessmentConfig assessmentConfig;

    @Schema(description = "指标设置类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "指标设置类型不能为空")
    @InEnum(value = HrmPerformanceQuotaSettingTypeEnum.class, message = "指标设置类型必须是 {value}")
    private Integer quotaSettingType;

    @Schema(description = "是否需要目标确认", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否需要目标确认不能为空")
    private Boolean targetConfirmation;

    @Schema(description = "目标确认节点")
    @Valid
    private HandlerStage targetConfirmationStage;

    @Schema(description = "评分阶段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "评分阶段不能为空")
    private List<@NotNull(message = "评分阶段不能为空") ReviewStage> reviewStages;

    @Schema(description = "是否需要结果审核", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否需要结果审核不能为空")
    private Boolean resultAudit;

    @Schema(description = "结果审核节点列表")
    @Valid
    private List<@NotNull(message = "结果审核节点不能为空") HandlerStage> resultAuditStages;

    @Schema(description = "是否需要结果确认", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否需要结果确认不能为空")
    private Boolean resultConfirmation;

    @Schema(description = "申诉处理节点列表")
    @Valid
    private List<@NotNull(message = "申诉处理节点不能为空") HandlerStage> appealStages;

    @Schema(description = "申诉超期天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @Min(value = 1, message = "申诉超期天数不能小于 1 天")
    @Max(value = 100, message = "申诉超期天数不能超过 100 天")
    @NotNull(message = "申诉超期天数不能为空")
    private Integer appealTimeoutDays;

    @Schema(description = "申诉超期处理方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(HrmPerformanceAppealTimeoutActionEnum.class)
    @NotNull(message = "申诉超期处理方式不能为空")
    private Integer appealTimeoutAction;

    @Schema(description = "结果模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "结果模板不能为空")
    private Long resultTemplateId;

    @Schema(description = "结果配置快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "结果配置不能为空")
    private ResultConfig resultConfig;

    @Schema(description = "是否同步薪资", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否同步薪资不能为空")
    private Boolean syncToSalary;

    @Schema(description = "计薪月份", example = "2026-09")
    @Size(max = 20, message = "计薪月份不能超过 20 个字符")
    private String paidForMonth;

    @Schema(description = "管理后台 - HRM 绩效计划考评范围")
    @Data
    public static class Scope {

        @Schema(description = "范围类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "考评范围类型不能为空")
        @InEnum(value = HrmPerformancePlanScopeTypeEnum.class, message = "考评范围类型必须是 {value}")
        private Integer type;

        @Schema(description = "员工编号列表", example = "[1024, 1025]")
        private List<@NotNull(message = "员工编号不能为空") Long> employeeIds;

        @Schema(description = "部门编号列表", example = "[100, 101]")
        private List<@NotNull(message = "部门编号不能为空") Long> deptIds;

        @Schema(description = "聘用形式", example = "1")
        @InEnum(value = HrmEmployeeTypeEnum.class, message = "聘用形式必须是 {value}")
        private Integer employeeType;

        @Schema(description = "员工状态列表", example = "[1, 2]")
        @InEnum(value = HrmEmployeeStatusEnum.class, message = "员工状态必须是 {value}")
        private List<Integer> employeeStatuses;
    }

    @Schema(description = "管理后台 - HRM 绩效计划处理节点")
    @Data
    public static class HandlerStage {

        @Schema(description = "处理人类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "处理人类型不能为空")
        @InEnum(value = HrmPerformanceRaterTypeEnum.class, message = "处理人类型必须是 {value}")
        private Integer type;

        @Schema(description = "上级或部门层级", example = "1")
        @Min(value = 1, message = "处理人层级不能小于 1")
        @Max(value = 10, message = "处理人层级不能大于 10")
        private Integer level;

        @Schema(description = "指定处理员工编号", example = "1024")
        private Long employeeId;
    }

    @Schema(description = "管理后台 - HRM 绩效计划评分阶段")
    @Data
    public static class ReviewStage {

        @Schema(description = "评分阶段名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "直属上级评分")
        @NotBlank(message = "评分阶段名称不能为空")
        @Size(max = 50, message = "评分阶段名称不能超过 50 个字符")
        private String name;

        @Schema(description = "评分人", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "评分人不能为空")
        private HandlerStage rater;

        @Schema(description = "评分权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        @NotNull(message = "评分权重不能为空")
        @DecimalMin(value = "0.01", message = "评分权重必须大于 0")
        @DecimalMax(value = "100", message = "评分权重不能大于 100")
        private BigDecimal weight;

        @Schema(description = "评分方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "评分方式不能为空")
        @InEnum(value = HrmPerformanceReviewScoringTypeEnum.class, message = "评分方式必须是 {value}")
        private Integer scoringType;

        @Schema(description = "评分内容可见范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "评分内容可见范围不能为空")
        @InEnum(value = HrmPerformanceReviewVisibleContentEnum.class, message = "评分内容可见范围必须是 {value}")
        private Integer visibleContent;

        @Schema(description = "评语是否必填", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "评语是否必填不能为空")
        private Boolean requiredSetting;

        @Schema(description = "是否允许驳回", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "是否允许驳回不能为空")
        private Boolean rejectAuthority;
    }

    @Schema(description = "管理后台 - HRM 绩效考核配置快照")
    @Data
    public static class AssessmentConfig {

        @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "季度绩效模板")
        @NotBlank(message = "模板名称不能为空")
        @Size(max = 50, message = "模板名称不能超过 50 个字符")
        private String name;

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

        @Schema(description = "考核维度配置", requiredMode = Schema.RequiredMode.REQUIRED)
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
    }

    @Schema(description = "管理后台 - HRM 绩效结果配置快照")
    @Data
    public static class ResultConfig {

        private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
        private static final BigDecimal MAX_SCORE = BigDecimal.valueOf(100);
        private static final BigDecimal SCORE_STEP = new BigDecimal("0.01");

        @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "绩效结果模板")
        @NotBlank(message = "结果模板名称不能为空")
        @Size(max = 255, message = "结果模板名称不能超过 255 个字符")
        private String name;

        @Schema(description = "结果等级配置", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotEmpty(message = "至少需要一个结果等级")
        private List<@NotNull(message = "结果等级不能为空") Level> levels;

        @AssertTrue(message = "结果等级名称不能重复")
        @JsonIgnore
        public boolean isLevelNamesUnique() {
            if (CollUtil.isEmpty(levels)) {
                return true;
            }
            Set<String> names = new HashSet<>();
            for (Level level : levels) {
                if (level != null && StrUtil.isNotBlank(level.getName())
                        && !names.add(level.getName().trim())) {
                    return false;
                }
            }
            return true;
        }

        @AssertTrue(message = "结果等级分数区间必须连续覆盖 0 到 100 分")
        @JsonIgnore
        public boolean isScoreRangeContinuous() {
            if (CollUtil.isEmpty(levels)) {
                return true;
            }
            List<Level> sortedLevels = new ArrayList<>(levels);
            if (sortedLevels.stream().anyMatch(level -> level == null
                    || level.getMinScore() == null || level.getMaxScore() == null)) {
                return true;
            }
            sortedLevels.sort(Comparator.comparing(Level::getMinScore));
            if (CollUtil.getFirst(sortedLevels).getMinScore().compareTo(MIN_SCORE) != 0) {
                return false;
            }
            for (int i = 1; i < sortedLevels.size(); i++) {
                Level previous = sortedLevels.get(i - 1);
                Level current = sortedLevels.get(i);
                if (current.getMinScore().compareTo(previous.getMaxScore().add(SCORE_STEP)) != 0) {
                    return false;
                }
            }
            return CollUtil.getLast(sortedLevels).getMaxScore().compareTo(MAX_SCORE) == 0;
        }
    }

    // Service 只校验模板、员工和部门等数据引用。
    @AssertTrue(message = "考评范围配置不正确")
    @JsonIgnore
    public boolean isScopeConfigValid() {
        if (CollUtil.isEmpty(scopes)) {
            return true;
        }
        for (Scope scope : scopes) {
            if (scope == null || scope.getType() == null) {
                continue;
            }
            if (Objects.equals(scope.getType(), HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType())) {
                if (CollUtil.isEmpty(scope.getEmployeeIds()) && CollUtil.isEmpty(scope.getDeptIds())) {
                    return false;
                }
            } else if (Objects.equals(scope.getType(), HrmPerformancePlanScopeTypeEnum.EMPLOYMENT.getType())) {
                if (scope.getEmployeeType() == null || CollUtil.isEmpty(scope.getEmployeeStatuses())) {
                    return false;
                }
            } else if (Objects.equals(scope.getType(),
                    HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType())) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "评分人配置不正确")
    @JsonIgnore
    public boolean isReviewStageConfigValid() {
        if (CollUtil.isEmpty(reviewStages)) {
            return true;
        }
        for (ReviewStage reviewStage : reviewStages) {
            if (reviewStage == null || reviewStage.getRater() == null) {
                continue;
            }
            if (!isHandlerStageValid(reviewStage.getRater(), true)
                    || Objects.equals(reviewStage.getRater().getType(), HrmPerformanceRaterTypeEnum.SELF.getType())
                    && Boolean.TRUE.equals(reviewStage.getRejectAuthority())) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "评分权重总和必须等于 100%")
    @JsonIgnore
    public boolean isReviewStageWeightTotalValid() {
        if (CollUtil.isEmpty(reviewStages)) {
            return true;
        }
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (ReviewStage reviewStage : reviewStages) {
            if (reviewStage == null || reviewStage.getWeight() == null) {
                return true;
            }
            totalWeight = totalWeight.add(reviewStage.getWeight());
        }
        return totalWeight.compareTo(PERCENT_100) == 0;
    }

    @AssertTrue(message = "评分人不能重复")
    @JsonIgnore
    public boolean isReviewStageRaterUnique() {
        if (CollUtil.isEmpty(reviewStages)) {
            return true;
        }
        List<HandlerStage> raters = new ArrayList<>();
        for (ReviewStage reviewStage : reviewStages) {
            if (reviewStage != null) {
                raters.add(reviewStage.getRater());
            }
        }
        return isHandlerStageUnique(raters);
    }

    @AssertTrue(message = "目标确认配置不正确")
    @JsonIgnore
    public boolean isTargetConfirmationConfigValid() {
        if (quotaSettingType == null || targetConfirmation == null) {
            return true;
        }
        if (Objects.equals(quotaSettingType, HrmPerformanceQuotaSettingTypeEnum.SYSTEM.getType())) {
            return !targetConfirmation;
        }
        return !targetConfirmation || targetConfirmationStage != null
                && isHandlerStageValid(targetConfirmationStage, true);
    }

    @AssertTrue(message = "结果审核节点配置不正确")
    @JsonIgnore
    public boolean isResultAuditConfigValid() {
        return ObjUtil.notEqual(Boolean.TRUE, resultAudit) || isHandlerStagesValid(resultAuditStages);
    }

    @AssertTrue(message = "申诉节点配置不正确")
    @JsonIgnore
    public boolean isResultConfirmationConfigValid() {
        return ObjUtil.notEqual(Boolean.TRUE, resultConfirmation) || isHandlerStagesValid(appealStages);
    }

    @AssertTrue(message = "计薪月份配置不正确")
    @JsonIgnore
    public boolean isSalaryConfigValid() {
        if (!Boolean.TRUE.equals(syncToSalary)) {
            return true;
        }
        if (StrUtil.isBlank(paidForMonth)) {
            return false;
        }
        try {
            YearMonth.parse(paidForMonth);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    @AssertTrue(message = "考核周期、季度和起止时间配置不正确")
    @JsonIgnore
    public boolean isCycleConfigValid() {
        if (cycleType == null) {
            return true;
        }
        if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
            return false;
        }
        if (Objects.equals(cycleType, HrmPerformanceCycleTypeEnum.OTHER.getType())) {
            return StrUtil.isNotBlank(cycle) && quarter == null;
        }
        try {
            if (Objects.equals(cycleType, HrmPerformanceCycleTypeEnum.MONTH.getType())) {
                YearMonth month = YearMonth.parse(cycle);
                return quarter == null && startTime.toLocalDate().equals(month.atDay(1))
                        && endTime.toLocalDate().equals(month.atEndOfMonth());
            }
            Year year = Year.parse(cycle);
            int beginMonth = 1;
            int endMonth = 12;
            if (Objects.equals(cycleType, HrmPerformanceCycleTypeEnum.QUARTER.getType())) {
                if (HrmPerformanceQuarterEnum.valueOf(quarter) == null) {
                    return false;
                }
                beginMonth = (quarter - 1) * 3 + 1;
                endMonth = beginMonth + 2;
            } else if (Objects.equals(cycleType,
                    HrmPerformanceCycleTypeEnum.FIRST_HALF_YEAR.getType())) {
                endMonth = 6;
            } else if (Objects.equals(cycleType,
                    HrmPerformanceCycleTypeEnum.SECOND_HALF_YEAR.getType())) {
                beginMonth = 7;
            }
            if (!Objects.equals(cycleType, HrmPerformanceCycleTypeEnum.QUARTER.getType())
                    && quarter != null) {
                return false;
            }
            YearMonth beginYearMonth = year.atMonth(beginMonth);
            YearMonth endYearMonth = year.atMonth(endMonth);
            return startTime.toLocalDate().equals(beginYearMonth.atDay(1))
                    && endTime.toLocalDate().equals(endYearMonth.atEndOfMonth());
        } catch (DateTimeParseException | NullPointerException ignored) {
            return false;
        }
    }

    private boolean isHandlerStagesValid(List<HandlerStage> stages) {
        if (CollUtil.isEmpty(stages) || stages.size() > 3) {
            return false;
        }
        for (HandlerStage stage : stages) {
            if (stage != null && !isHandlerStageValid(stage, false)) {
                return false;
            }
        }
        return isHandlerStageUnique(stages);
    }

    private boolean isHandlerStageValid(HandlerStage stage, boolean allowSelf) {
        if (stage == null || stage.getType() == null) {
            return true;
        }
        if (Objects.equals(stage.getType(), HrmPerformanceRaterTypeEnum.SELF.getType())) {
            return allowSelf;
        }
        if (ObjectUtils.equalsAny(stage.getType(), HrmPerformanceRaterTypeEnum.SUPERIOR.getType(),
                HrmPerformanceRaterTypeEnum.DEPT_LEADER.getType())) {
            return stage.getLevel() != null && stage.getLevel() >= 1 && stage.getLevel() <= 10;
        }
        if (Objects.equals(stage.getType(), HrmPerformanceRaterTypeEnum.SPECIFIED.getType())) {
            return stage.getEmployeeId() != null;
        }
        return true;
    }

    private boolean isHandlerStageUnique(List<HandlerStage> stages) {
        if (CollUtil.isEmpty(stages)) {
            return true;
        }
        Set<String> handlerKeys = new HashSet<>();
        for (HandlerStage stage : stages) {
            String handlerKey = buildHandlerKey(stage);
            if (handlerKey != null && !handlerKeys.add(handlerKey)) {
                return false;
            }
        }
        return true;
    }

    private String buildHandlerKey(HandlerStage stage) {
        if (stage == null || stage.getType() == null) {
            return null;
        }
        if (Objects.equals(stage.getType(), HrmPerformanceRaterTypeEnum.SELF.getType())) {
            return String.valueOf(stage.getType());
        }
        if (Objects.equals(stage.getType(), HrmPerformanceRaterTypeEnum.SPECIFIED.getType())) {
            return stage.getEmployeeId() == null ? null : stage.getType() + ":" + stage.getEmployeeId();
        }
        if (ObjectUtils.equalsAny(stage.getType(), HrmPerformanceRaterTypeEnum.SUPERIOR.getType(),
                HrmPerformanceRaterTypeEnum.DEPT_LEADER.getType())) {
            return stage.getLevel() == null ? null : stage.getType() + ":" + stage.getLevel();
        }
        return null;
    }

}
