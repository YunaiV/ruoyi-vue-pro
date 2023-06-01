package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 实验名目的操作SOP Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CategorySopBaseVO {

    @Schema(description = "实验名目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27739")
    @NotNull(message = "实验名目 id不能为空")
    private Long categoryId;

    @Schema(description = "操作步骤的内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作步骤的内容不能为空")
    private String content;

    @Schema(description = "步骤序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "步骤序号不能为空")
    private Integer step;

    @Schema(description = "注意事项")
    private String mark;

    @Schema(description = "依赖项(json数组多个)")
    private String dependIds;

}
