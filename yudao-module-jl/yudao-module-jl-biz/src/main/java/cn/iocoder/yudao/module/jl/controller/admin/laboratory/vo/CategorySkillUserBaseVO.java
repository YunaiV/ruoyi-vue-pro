package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 实验名目的擅长人员 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CategorySkillUserBaseVO {

    @Schema(description = "实验名目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14006")
    @NotNull(message = "实验名目 id不能为空")
    private Long categoryId;

    @Schema(description = "实验人员 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30727")
    @NotNull(message = "实验人员 id不能为空")
    private Long userId;

}
