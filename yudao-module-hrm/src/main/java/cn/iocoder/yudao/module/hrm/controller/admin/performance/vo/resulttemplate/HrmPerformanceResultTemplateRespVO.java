package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 绩效结果模板 Response VO")
@Data
public class HrmPerformanceResultTemplateRespVO {

    @Schema(description = "结果模板编号", example = "1024")
    private Long id;

    @Schema(description = "结果模板名称", example = "季度绩效结果")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "结果等级列表")
    private List<Level> levels;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "管理后台 - HRM 绩效结果模板等级")
    @Data
    public static class Level {

        @Schema(description = "等级名称", example = "A")
        private String name;

        @Schema(description = "最低分数", example = "90")
        private BigDecimal minScore;

        @Schema(description = "最高分数", example = "100")
        private BigDecimal maxScore;

        @Schema(description = "绩效系数", example = "1.2")
        private BigDecimal coefficient;
    }

}
