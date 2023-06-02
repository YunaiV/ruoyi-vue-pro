package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 项目的实验名目 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProjectCategoryBaseVO {

    @Schema(description = "报价 id", example = "20286")
    private Long quoteId;

    @Schema(description = "安排单 id", example = "14245")
    private Long scheduleId;

    @Schema(description = "类型，报价/安排单", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "类型，报价/安排单不能为空")
    private String type;

    @Schema(description = "名目的实验类型，动物/细胞/分子等", example = "2")
    private String categoryType;

    @Schema(description = "实验名目库的名目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17935")
    @NotNull(message = "实验名目库的名目 id不能为空")
    private Long categoryId;

    @Schema(description = "实验人员", example = "17520")
    private Long operatorId;

    @Schema(description = "客户需求")
    private String demand;

    @Schema(description = "干扰项")
    private String interference;

    @Schema(description = "依赖项(json数组多个)")
    private String dependIds;

    @Schema(description = "实验名目名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "实验名目名字不能为空")
    private String name;

    @Schema(description = "备注")
    private String mark;

}
