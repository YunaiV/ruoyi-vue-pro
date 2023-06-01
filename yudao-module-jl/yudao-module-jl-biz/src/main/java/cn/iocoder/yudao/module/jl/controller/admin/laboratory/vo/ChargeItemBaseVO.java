package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 实验收费项 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ChargeItemBaseVO {

    @Schema(description = "收费类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "收费类型不能为空")
    private String type;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "名称不能为空")
    private String name;

    @Schema(description = "成本价", requiredMode = Schema.RequiredMode.REQUIRED, example = "10320")
    @NotNull(message = "成本价不能为空")
    private Long costPrice;

    @Schema(description = "建议销售价", requiredMode = Schema.RequiredMode.REQUIRED, example = "5530")
    @NotNull(message = "建议销售价不能为空")
    private Long suggestedSellingPrice;

    @Schema(description = "备注")
    private String mark;

}
