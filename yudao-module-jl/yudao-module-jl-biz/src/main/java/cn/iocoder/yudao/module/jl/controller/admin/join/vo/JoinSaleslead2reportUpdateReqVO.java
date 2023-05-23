package cn.iocoder.yudao.module.jl.controller.admin.join.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 销售线索中的方案更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JoinSaleslead2reportUpdateReqVO extends JoinSaleslead2reportBaseVO {

    @Schema(description = "岗位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23416")
    @NotNull(message = "岗位ID不能为空")
    private Long id;

}
