package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 绩效考核模板 Response VO")
@Data
public class HrmPerformanceAssessmentTemplateRespVO {

    @Schema(description = "模板编号", example = "1024")
    private Long id;

    @Schema(description = "模板名称", example = "季度绩效模板")
    private String name;

    @Schema(description = "模板说明", example = "适用于季度绩效考核")
    private String illustrate;

    @Schema(description = "计分方式", example = "1")
    private Integer scoreCalculation;

    @Schema(description = "分数上限类型", example = "1")
    private Integer upperLimitType;

    @Schema(description = "分数上限", example = "100")
    private BigDecimal upperLimitScore;

    @Schema(description = "维度数量", example = "3")
    private Integer dimensionCount;

    @Schema(description = "指标数量", example = "12")
    private Integer quotaCount;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "考核维度列表")
    private List<Dimension> dimensions;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "管理后台 - HRM 绩效考核模板维度")
    @Data
    public static class Dimension {

        @Schema(description = "维度名称", example = "工作业绩")
        private String name;

        @Schema(description = "指标类型", example = "1")
        private Integer quotaType;

        @Schema(description = "维度权重", example = "60")
        private BigDecimal weight;

        @Schema(description = "备注", example = "核心业绩指标")
        private String remark;

        @Schema(description = "是否允许员工编辑", example = "true")
        private Boolean allowEdit;

        @Schema(description = "考核指标列表")
        private List<Quota> quotas;
    }

    @Schema(description = "管理后台 - HRM 绩效考核模板指标")
    @Data
    public static class Quota {

        @Schema(description = "指标名称", example = "销售目标达成率")
        private String name;

        @Schema(description = "指标说明", example = "按季度销售目标计算")
        private String illustrate;

        @Schema(description = "评分标准", example = "完成率达到 100% 得满分")
        private String standard;

        @Schema(description = "指标权重", example = "50")
        private BigDecimal weight;

        @Schema(description = "评分类型", example = "1")
        private Integer scoreType;
    }

}
