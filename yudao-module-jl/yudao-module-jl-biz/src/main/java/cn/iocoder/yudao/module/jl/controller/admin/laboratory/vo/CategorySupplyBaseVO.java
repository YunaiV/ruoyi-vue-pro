package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.*;

/**
 * 实验名目的物资 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CategorySupplyBaseVO {

    @Schema(description = "物资 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "20280")
    @NotNull(message = "物资 id不能为空")
    private Long supplyId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "物品名字")
    @NotNull(message = "名称不能为空")
    private String name;

    @Schema(description = "规则/单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "规则/单位不能为空")
    private String feeStandard;

    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单价不能为空")
    private String unitFee;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @Schema(description = "备注")
    private String mark;

    @Schema(description = "实验名目 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27140")
    @NotNull(message = "实验名目 id不能为空")
    private Long categoryId;

    @Schema(description = "实验单量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单量")
    private Long unitAmount = 1L;

}
