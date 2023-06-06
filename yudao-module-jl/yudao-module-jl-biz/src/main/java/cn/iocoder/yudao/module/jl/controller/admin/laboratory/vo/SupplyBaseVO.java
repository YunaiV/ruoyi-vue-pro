package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 实验物资 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SupplyBaseVO {

    @Schema(description = "物资名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "物资名称不能为空")
    private String name;

    @Schema(description = "物资类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物资类型不能为空")
    private String type;

    @Schema(description = "备注")
    private String mark;

}
