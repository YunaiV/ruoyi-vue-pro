package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 实验名目的收费项 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CategoryChargeitemBaseVO {

    @Schema(description = "收费项 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "4226")
    @NotNull(message = "收费项 id不能为空")
    private Long chargeItemId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotNull(message = "名称不能为空")
    private String name;

    @Schema(description = "收费的标准/规则", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收费的标准/规则不能为空")
    private String feeStandard;

    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单价不能为空")
    private String unitFee;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @Schema(description = "备注")
    private String mark;

    @Schema(description = "实验名目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18528")
    @NotNull(message = "实验名目 id不能为空")
    private Long categoryId;

    @Schema(description = "单量")
    private Integer unitAmount;

}
