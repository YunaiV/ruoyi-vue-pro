package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 实验收费项更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChargeItemUpdateReqVO extends ChargeItemBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2468")
    @NotNull(message = "ID不能为空")
    private Long id;

}
