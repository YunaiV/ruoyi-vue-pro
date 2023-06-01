package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 实验名目的操作SOP更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CategorySopUpdateReqVO extends CategorySopBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23131")
    @NotNull(message = "ID不能为空")
    private Long id;

}
