package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - HRM 员工绩效指标保存 Request VO")
@Data
public class HrmPortalPerformanceQuotaSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "员工绩效维度编号", example = "2048")
    private Long dimensionId;

    @Schema(description = "指标名称", example = "销售目标达成率")
    @Size(max = 255, message = "指标名称不能超过 255 个字符")
    private String name;

    @Schema(description = "指标说明", example = "按季度销售目标计算")
    @Size(max = 1000, message = "指标说明不能超过 1000 个字符")
    private String description;

    @Schema(description = "标准值", example = "完成率达到 100% 得满分")
    @Size(max = 1000, message = "标准值不能超过 1000 个字符")
    private String standard;

    @Schema(description = "指标权重", example = "50")
    private BigDecimal weight;

    @Schema(description = "分数类型", example = "1")
    private Integer scoreType;

    @Schema(description = "目标值", example = "100%")
    @Size(max = 1000, message = "目标值不能超过 1000 个字符")
    private String targetValue;

    @Schema(description = "实际值", example = "95%")
    @Size(max = 1000, message = "实际值不能超过 1000 个字符")
    private String actualValue;

    @Schema(description = "自评分数", example = "90")
    private BigDecimal selfScore;

    @Schema(description = "评分人得分", example = "88")
    private BigDecimal reviewerScore;

    @Schema(description = "最终得分", example = "89")
    private BigDecimal finalScore;

    @Schema(description = "说明", example = "目标基本达成")
    @Size(max = 1000, message = "说明不能超过 1000 个字符")
    private String comment;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
