package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 实验名目的参考资料 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CategoryReferenceBaseVO {

    @Schema(description = "实验名目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "6339")
    @NotNull(message = "实验名目 id不能为空")
    private Long categoryId;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "文件名不能为空")
    private String name;

    @Schema(description = "操作步骤的内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotNull(message = "操作步骤的内容不能为空")
    private String url;

    @Schema(description = "类型(文献、结果参考、交付标准)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "类型(文献、结果参考、交付标准)不能为空")
    private String type;

}
