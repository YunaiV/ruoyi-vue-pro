package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - HRM 绩效结果模板保存 Request VO")
@Data
public class HrmPerformanceResultTemplateSaveReqVO {

    private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_SCORE = BigDecimal.valueOf(100);
    private static final BigDecimal SCORE_STEP = new BigDecimal("0.01");

    @Schema(description = "结果模板编号", example = "1024")
    private Long id;

    @Schema(description = "结果模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "季度绩效结果")
    @NotBlank(message = "结果模板名称不能为空")
    @Size(max = 255, message = "结果模板名称不能超过 255 个字符")
    private String name;

    @Schema(description = "结果等级列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "至少需要一个结果等级")
    private List<@NotNull(message = "结果等级不能为空") Level> levels;

    /**
     * 校验结果等级名称唯一
     */
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

    /**
     * 校验结果等级分数区间连续覆盖 0 到 100 分
     */
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

    @Schema(description = "管理后台 - HRM 绩效结果模板等级")
    @Data
    public static class Level {

        @Schema(description = "等级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A")
        @NotBlank(message = "等级名称不能为空")
        @Size(max = 255, message = "等级名称不能超过 255 个字符")
        private String name;

        @Schema(description = "最低分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "90")
        @NotNull(message = "最低分数不能为空")
        @DecimalMin(value = "0", message = "最低分数不能小于 0")
        @DecimalMax(value = "100", message = "最低分数不能大于 100")
        @Digits(integer = 3, fraction = 2, message = "最低分数最多保留两位小数")
        private BigDecimal minScore;

        @Schema(description = "最高分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "最高分数不能为空")
        @DecimalMin(value = "0", message = "最高分数不能小于 0")
        @DecimalMax(value = "100", message = "最高分数不能大于 100")
        @Digits(integer = 3, fraction = 2, message = "最高分数最多保留两位小数")
        private BigDecimal maxScore;

        @Schema(description = "绩效系数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.2")
        @NotNull(message = "绩效系数不能为空")
        @DecimalMin(value = "0", message = "绩效系数不能小于 0")
        @Digits(integer = 8, fraction = 2, message = "绩效系数最多保留两位小数")
        private BigDecimal coefficient;

        @AssertTrue(message = "最低分数不能大于最高分数")
        @JsonIgnore
        public boolean isScoreRangeValid() {
            return minScore == null || maxScore == null || minScore.compareTo(maxScore) <= 0;
        }
    }

}
