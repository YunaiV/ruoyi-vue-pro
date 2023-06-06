package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 实验名目的操作SOP Base VO，没有 ID
 */
@Data
public class CategorySopBaseWithoutIDVO {

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
