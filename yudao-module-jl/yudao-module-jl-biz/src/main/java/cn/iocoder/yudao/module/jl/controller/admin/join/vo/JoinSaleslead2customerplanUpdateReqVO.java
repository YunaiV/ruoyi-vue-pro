package cn.iocoder.yudao.module.jl.controller.admin.join.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 销售线索中的客户方案更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JoinSaleslead2customerplanUpdateReqVO extends JoinSaleslead2customerplanBaseVO {

    @Schema(description = "岗位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22988")
    @NotNull(message = "岗位ID不能为空")
    private Long id;

}
